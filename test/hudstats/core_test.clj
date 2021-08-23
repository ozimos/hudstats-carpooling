(ns hudstats.core-test
  (:require [hudstats.core :as hud]
            [clj-http.fake :refer [with-fake-routes]]
            [jsonista.core :as j]
            [ring.mock.request :as mock]
            [clojure.test :refer [deftest are is]]))


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

(def page-tree [{:tag :tr
                 :attrs nil
                 :content
                 ["\n"
                  {:tag :td, :attrs {:align "right"}, :content [{:tag :b, :attrs nil, :content ["Requesting:"]}]}
                  "\n"
                  {:tag :td, :attrs nil, :content ["Passengers"]}
                  "\n"]}
                {:tag :tr
                 :attrs nil
                 :content
                 ["\n"
                  {:tag :td, :attrs {:align "right"}, :content [{:tag :b, :attrs nil, :content ["From:"]}]}
                  "\n"
                  {:tag :td, :attrs nil, :content ["Keflaví­k"]}
                  "\n"]}
                {:tag :tr
                 :attrs nil
                 :content
                 ["\n"
                  {:tag :td, :attrs {:align "right"}, :content [{:tag :b, :attrs nil, :content ["To:"]}]}
                  "\n"
                  {:tag :td, :attrs nil, :content ["Selfoss"]}
                  "\n"]}])

(def processed-page [[{:tag :td, :attrs {:align "right"}, :content [{:tag :b, :attrs nil, :content ["Requesting:"]}]}
           {:tag :td, :attrs nil, :content ["Passengers"]}]
          [{:tag :td, :attrs {:align "right"}, :content [{:tag :b, :attrs nil, :content ["From:"]}]}
           {:tag :td, :attrs nil, :content ["Keflaví­k"]}]
          [{:tag :td, :attrs {:align "right"}, :content [{:tag :b, :attrs nil, :content ["To:"]}]}
           {:tag :td, :attrs nil, :content ["Selfoss"]}]])

(deftest process-page
  (is (= processed-page
         (hud/process-page page-tree))))

(deftest aggregate
  (is (= {"From" "Keflaví­k", "Requesting" "Passengers", "To" "Selfoss"} (reduce hud/seq->map {} processed-page))))

(deftest server
  (with-fake-routes {hud/drivers-url (fn [_req] {:status 200 :headers {} :body (j/write-value-as-string driver-results)})
                     hud/passengers-url (fn [_req] {:status 200 :headers {} :body (j/write-value-as-string passenger-results)})}

    (are [result method path] (= result (-> (mock/request method path) hud/app (update :body #(j/read-value % j/keyword-keys-object-mapper))))

      {:status 200 :body  driver-results :headers {"Content-Type" "application/json; charset=utf-8"}} :get "/drivers"
      {:status 200 :body  passenger-results :headers {"Content-Type" "application/json; charset=utf-8"}} :get "/passengers")))

