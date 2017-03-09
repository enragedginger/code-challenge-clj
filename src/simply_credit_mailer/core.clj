(ns simply-credit-mailer.core
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [clj-http.client :as http]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [simply-credit-mailer.mailer :as mailer]
            [ring.middleware.json :as json]
            [ring.middleware.keyword-params :as keyword-params]
            [ring.util.response :as response]))

(defn send-message
  "Handler for sending basic email requests. Parameters should be on the :params property."
  [request]
  (let [{:keys [to subject body]} (-> request :params)]
    (mailer/send-message to subject body)))

(defn send-template
  "Handler for sending email requests based on templates. Parameters should be on the :params property."
  [request]
  (let [{:keys [template to subject args]} (-> request :params)]
    (mailer/send-template-with-arg-map template to subject args)))

(defroutes app
  (POST "/send-message" request (-> send-message keyword-params/wrap-keyword-params json/wrap-json-params))
  (POST "/send-template" request (-> send-template keyword-params/wrap-keyword-params json/wrap-json-params))
  (route/not-found "<h1>Page not found</h1>"))

(defn -main
  "Starts the web application server on port 3000 for development mode. Plays nicely within a REPL."
  []
  (jetty/run-jetty app {:port 3000 :join? false}))
