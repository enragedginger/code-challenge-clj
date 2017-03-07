(ns simply-credit-mailer.mailer
  (:gen-class)
  (:require [aero.core :refer [read-config]]
            [clojure.java.io :as io]
            [clj-http.client :as http]))

(defn build-mail-config []
  (-> (io/resource "config.edn")
      read-config
      :mailgun))

(defn send-mail-http [conf to-address subject message]
  (let [{:keys [api-key from-address endpoint]} conf]
    (http/post endpoint {:form-params {
                                  :from from-address
                                  :to to-address
                                  :subject subject
                                  :text message
                                  }
                    :basic-auth ["api" api-key]})))

(defn send-message [to-address subject message]
  (let [conf (build-mail-config)]
    (send-mail-http conf to-address subject message)))

;(send-message "stephenmhopper@gmail.com" "First email" "Text goes here")

(defn -main [& args]
  (let [[to-address subject message] args]
    (send-message to-address subject message)))
