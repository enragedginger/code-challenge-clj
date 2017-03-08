(defproject simply-credit-mailer "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [aero "1.1.2"]
                 [ring "1.5.1"]
                 [clj-http "3.4.1"]
                 [org.clojure/tools.cli "0.3.5"]
                 [selmer "1.10.6"]
                 [compojure "1.5.2"]
                 [ring/ring-json "0.4.0"]]
  :plugins [[lein-ring "0.11.0"]]
  :ring {:handler simply-credit-mailer.core/app}
  ;:main ^:skip-aot simply-credit-mailer.core
  :target-path "target/%s"
  :aliases {
            "send-message" ["run" "-m" "simply-credit-mailer.mailer/-main"]
            }
  :profiles {:uberjar {:aot :all}})
