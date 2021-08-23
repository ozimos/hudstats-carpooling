(ns hudstats.core
  (:gen-class)
  (:require [reitit.ring :as ring]
            [muuntaja.core :as m]
            [simple-cors.ring.middleware :as cors]
            [net.cgrand.enlive-html :as html]
            [reitit.ring.coercion :as rrc]
            [clj-http.client :as http]
            [reitit.coercion.malli]
            [jsonista.core :as j]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.exception :as exception]
            [ring.adapter.jetty :as jetty]))

(defn get-api-data [url]
  (some-> (http/get url)
          :body
          (j/read-value j/keyword-keys-object-mapper)))

(def drivers-url "http://apis.is/rides/samferda-drivers/")

(def passengers-url "http://apis.is/rides/samferda-passengers/")

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(defn extract-links [results] (map :link results))

(def app
  (ring/ring-handler
   (ring/router
    [["/drivers" {:get {:handler    (fn [_]
                                      {:status 200
                                       :body  (get-api-data drivers-url)})}}]
     ["/passengers" {:get {:handler    (fn [_]
                                         {:status 200
                                          :body  (get-api-data passengers-url)})}}]]
    {
     :data {:coercion reitit.coercion.malli/coercion
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
  "I don't do a whole lot ... yet."
  [& args]
  (start {}))

(comment
  (def server (start {}))
  (require '[ring.mock.request :as mock])
  (require '[jsonista.core :as j])
  (def api-response (-> (http/get drivers-url)
                        :body
                        (j/read-value j/keyword-keys-object-mapper)))

  (def html-resp (-> (http/get "http://www.samferda.net/en/detail/129568")
                     :body))


  (slurp "http://www.samferda.net/en/detail/129568")
  (def html-tree (fetch-url "http://www.samferda.net/en/detail/129568"))
  (map :content (html/select html-tree [:tr]))
  {:tag :td, :attrs {:align "right"}, :content '({:tag :b, :attrs nil, :content ("Name:")})}
  {:tag :td, :attrs nil, :content '("Luca")}
  (map :link (:results
              api-response))


  (-> (mock/request :get "/drivers")
      app
      (update :body (fn [v] (when v (j/read-value v j/keyword-keys-object-mapper)))))

  "")


