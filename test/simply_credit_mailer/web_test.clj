(ns simply-credit-mailer.web-test
  (:require
    [clojure.test :refer :all]
    [clj-http.client :as http]))

(deftest send-basic-email
  (testing "Sending basic email messages to the web server"
    (http/post "http://localhost:3000/send-message"
               {:content-type :json
                :form-params {:to "stephenmhopper@gmail.com"
                              :subject "Email via post send-message route"
                              :body "This is a sample email"}})))

(deftest send-welcome-template-email
  (testing "Sending a welcome template message via the web server"
    (http/post "http://localhost:3000/send-template"
               {:content-type :json
                :form-params {:to "stephenmhopper@gmail.com"
                              :subject "Email via post send-template route"
                              :template "welcome"
                              :args {
                                     :name "Stephen"
                                     }}})))

(deftest send-password-reset-template-email
  (testing "Sending a password_reset template message via the web server"
    (http/post "http://localhost:3000/send-template"
               {:content-type :json
                :form-params {:to "stephenmhopper@gmail.com"
                              :subject "Email via post send-template route"
                              :template "password_reset"
                              :args {
                                     :name "Stephen"
                                     :url "http://www.google.com"
                                     }}})))
