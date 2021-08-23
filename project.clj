(defproject hudstats "0.1.0-SNAPSHOT"
  :description "service should provide a JSON API that extends Icelandic car pooling API
http://docs.apis.is/#endpoint-rides which will show passengers and drivers along w"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [metosin/reitit "0.5.15"]
                 [ring/ring-jetty-adapter "1.9.4"]
                 [clj-http "3.12.3"]
                 [zerg000000/simple-cors "0.0.8"]
                 [enlive "1.1.6"]]
  :main ^:skip-aot hudstats.core
  :target-path "target/%s"
  :aliases {"kaocha" ["run" "-m" "kaocha.runner"]}
  :profiles {:uberjar {:aot :all}
             :dev [:testing]
             :test [:testing]
             :testing {:dependencies [[ring/ring-mock "0.4.0"]
                                      [clj-http-fake "1.0.3"]
                                      [lambdaisland/kaocha "1.0.861"]]
                       :jvm-opts ["-Dsnoop.enabled"]}})
