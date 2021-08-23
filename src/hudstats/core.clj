(ns hudstats.core
  (:gen-class)
  (:require [reitit.ring :as ring]
            [muuntaja.core :as m]
            [simple-cors.ring.middleware :as cors]
            [net.cgrand.enlive-html :as html]
            [reitit.ring.coercion :as rrc]
            [clj-http.client :as http]
            [clojure.string :as str]
            [reitit.coercion.malli]
            [jsonista.core :as j]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.exception :as exception]
            [ring.adapter.jetty :as jetty]))

(defn get-base-data [url]
  (some-> (http/get url)
          :body
          (j/read-value j/keyword-keys-object-mapper)))

(def drivers-url "http://apis.is/rides/samferda-drivers/")

(def passengers-url "http://apis.is/rides/samferda-passengers/")

(def extra-keys ["Seats" "Name" "Phone" "Mobile" "E-mail" "Non-smoke car"])

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(defn extract-links [results] (map :link results))

(defn extract-key [v]
  (-> v :content first :content first (str/replace ":" "")))

(defn extract-val [v]
  (-> v :content first))

(defn seq->map [acc a]
  (assoc acc (extract-key (first a)) (extract-val (second a))))

(defn process-page [page-tree]
  (as-> page-tree $ (map  (comp #(filter map? %)  :content) $)
       (reduce seq->map {} $)
       (select-keys $ extra-keys)))

(defn enhance-api [url]
  (let [results (:results (get-base-data url))
        link-pages (doall (pmap (fn [{link :link}] (fetch-url link)) results))]
    (map-indexed (fn [index page] (->> (html/select page [:tr]) process-page (merge (nth results index)) )) link-pages)))

(defn make-handler [url]
  (fn [_]
    {:status 200
     :body  {:results (enhance-api url)}}))

(def drivers-handler (make-handler drivers-url))
(def passengers-handler (make-handler passengers-url))

(def app
  (ring/ring-handler
   (ring/router
    [["/drivers" {:get {:handler  drivers-handler}}]
     ["/passengers" {:get {:handler passengers-handler}}]]
    {:data {:coercion reitit.coercion.malli/coercion
            :muuntaja   m/instance
            :middleware [[cors/wrap {:cors-config {:origins "*"}}]
                         muuntaja/format-negotiate-middleware
                         muuntaja/format-request-middleware
                         muuntaja/format-response-middleware
                         exception/exception-middleware
                         rrc/coerce-response-middleware]}})
   (ring/create-default-handler)))

(defn start [{:keys [port] :or {port 3000}}]
  (let [server (jetty/run-jetty #'app {:port port, :join? false})]
    (println "server running on port" port)
    server))

(defn -main
  [& args]
  (start {}))

(comment
  (def server (start {}))
  (.stop server)
  (require '[ring.mock.request :as mock])
  (require '[jsonista.core :as j])
  (def api-response (-> (http/get drivers-url)
                        :body
                        (j/read-value j/keyword-keys-object-mapper)))


  "")


