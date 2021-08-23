(ns hudstats.core-test
  (:require [hudstats.core :as hud]
            [clj-http.fake :refer [with-fake-routes]]
            [jsonista.core :as j]
            [ring.mock.request :as mock]
            [clojure.test :refer [deftest are]]))


(def driver-results {:results [{:date "2021-08-23"
                                :time "11:00"
                                :from "Reykjaví­k"
                                :link "http://www.samferda.net/en/detail/127774"
                                :to "Keflaví­k"}
                               {:date "2021-08-23"
                                :time "flexible"
                                :from "Reykjaví­k"
                                :link "http://www.samferda.net/en/detail/128126"
                                :to "Ísafjörður"}
                               {:date "2021-08-23"
                                :time "6:00"
                                :from "Reykjaví­k"
                                :link "http://www.samferda.net/en/detail/128188"
                                :to "Landmannalaugar"}]})

(def passenger-results {:results [{:date "2021-08-23"
                                   :time "11:00"
                                   :from "Reykjaví­k"
                                   :link "http://www.samferda.net/en/detail/127774"
                                   :to "Keflaví­k"}
                                  {:date "2021-08-23"
                                   :time "flexible"
                                   :from "Reykjaví­k"
                                   :link "http://www.samferda.net/en/detail/128126"
                                   :to "Ísafjörður"}
                                  {:date "2021-08-23"
                                   :time "6:00"
                                   :from "Reykjaví­k"
                                   :link "http://www.samferda.net/en/detail/128188"
                                   :to "Landmannalaugar"}]})

(deftest server
  (with-fake-routes {hud/drivers-url (fn [_req] {:status 200 :headers {} :body (j/write-value-as-string driver-results)})
                     hud/passengers-url (fn [_req] {:status 200 :headers {} :body (j/write-value-as-string passenger-results)})}

    (are [result method path] (= result (-> (mock/request method path) hud/app (update :body #(j/read-value % j/keyword-keys-object-mapper))))

      {:status 200 :body  driver-results :headers {"Content-Type" "application/json; charset=utf-8"}} :get "/drivers"
      {:status 200 :body  passenger-results :headers {"Content-Type" "application/json; charset=utf-8"}} :get "/passengers")))

