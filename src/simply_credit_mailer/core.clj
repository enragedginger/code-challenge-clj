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

(defn send-message [request]
  (let [{:keys [to subject body]} (-> request :params)]
    (mailer/send-message to subject body)))

(defn send-template [request]
  (let [{:keys [template to subject args]} (-> request :params)]
    (mailer/send-template-with-arg-map template to subject args)))

(defroutes app
  (POST "/send-message" request (-> send-message keyword-params/wrap-keyword-params json/wrap-json-params))
  (POST "/send-template" request (-> send-template keyword-params/wrap-keyword-params json/wrap-json-params))
  (route/not-found "<h1>Page not found</h1>"))

(defn handler [request]
  {:status  200
   :headers {"Content-Type" "text/plain"}
   :body    "Hello Clojure, Hello Ring!"})

(defn -main []
  (jetty/run-jetty app {:port 3000 :join? false}))

;(http/post "http://localhost:3000/send-message"
;           {:content-type :json
;            :form-params {:to "stephenmhopper@gmail.com"
;                          :subject "Email via post send-message route"
;                          :body "This is a sample email"}})
;
;(http/post "http://localhost:3000/send-template"
;           {:content-type :json
;            :form-params {:to "stephenmhopper@gmail.com"
;                          :subject "Email via post send-template route"
;                          :template "welcome"
;                          :args {
;                                 :name "Stephen"
;                                 }}})
;
;(http/post "http://localhost:3000/send-template"
;           {:content-type :json
;            :form-params {:to "stephenmhopper@gmail.com"
;                          :subject "Email via post send-template route"
;                          :template "password_reset"
;                          :args {
;                                 :name "Stephen"
;                                 :url "http://www.google.com"
;                                 }}})

;(-main)