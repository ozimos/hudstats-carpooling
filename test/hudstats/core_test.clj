(ns hudstats.core-test
  (:require [hudstats.core :as hud]
            [hudstats.mocks :as mock-data]
            [clj-http.fake :refer [with-fake-routes]]
            [jsonista.core :as j]
            [ring.mock.request :as mock]
            [clojure.test :refer [deftest are is]]))

(deftest process-page
  (is (= {"Name" "Keflaví­k"
          "Phone" "0124568"
          "Seats" "2"}
         (hud/process-page mock-data/page-tree))))

(deftest aggregate
  (is (= {"From" "Keflaví­k", "Requesting" "Passengers", "To" "Selfoss"} (reduce hud/seq->map {} mock-data/processed-page))))

(deftest server
  (with-fake-routes (merge mock-data/http-mocks {hud/drivers-url (fn [_req] {:status 200 :headers {} :body (j/write-value-as-string mock-data/driver-results)})
                                                 hud/passengers-url (fn [_req] {:status 200 :headers {} :body (j/write-value-as-string mock-data/passenger-results)})})

    (are [result method path] (= result (-> (mock/request method path) hud/app (update :body #(j/read-value % j/default-object-mapper))))

      {:status 200 :body  mock-data/driver-results :headers {"Content-Type" "application/json; charset=utf-8"}} :get "/drivers"
      {:status 200 :body  mock-data/passenger-results :headers {"Content-Type" "application/json; charset=utf-8"}} :get "/passengers")))

