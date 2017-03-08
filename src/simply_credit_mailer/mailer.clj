(ns simply-credit-mailer.mailer
  (:gen-class)
  (:require [aero.core :refer [read-config]]
            [clojure.java.io :as io]
            [clj-http.client :as http]
            [simply-credit-mailer.template-parser :as template-parser]))

(defn contains-all? [m keys]
  (every? #(contains? m %) keys))

(defn valid-body-req? [segment]
  (contains-all? segment [:to :subject :body]))

(defn valid-template-req? [segment]
  (and (contains-all? segment [:to :subject :template :args])
       (template-parser/template-exists? (-> segment :template))))

;(valid-template-req? {:to "stephenmhopper@gmail.com" :subject "send a thing" :template "welcome" :args {:name "Stephen"}})
;(valid-body-req? {:to "stephenmhopper@gmail.com" :subject "send a thing" :body "welcome"})

(defn build-mail-config []
  (-> (io/resource "config.edn")
      read-config
      :mailgun))

(defn send-mail-http [conf to-address subject body]
  (let [{:keys [api-key from-address endpoint]} conf]
    (http/post endpoint {:form-params {
                                  :from from-address
                                  :to to-address
                                  :subject subject
                                  :text body
                                  }
                    :basic-auth ["api" api-key]})))

(defn send-message [to-address subject body]
  (let [conf (build-mail-config)]
    (send-mail-http conf to-address subject body)))

(defn args-to-map [& args]
  (let [arg-map (apply array-map args)]
    (into {} (map (fn [[k v]] [(keyword k) v]) arg-map))))

(defn send-template-with-arg-map [template-name to-address subject arg-map]
  (let [conf (build-mail-config)
        body (template-parser/parse-template template-name arg-map)]
    (send-mail-http conf to-address subject body)))

(defn send-template [template-name to-address subject & args]
  (let [conf (build-mail-config)
        arg-map (apply args-to-map args)]
    (send-template-with-arg-map template-name to-address subject arg-map)))

;(send-message "stephenmhopper@gmail.com" "First email" "Text goes here")
;(send-template "welcome" "stephenmhopper@gmail.com" "First email" "name" "Stephen")
;(send-template "welcome" "stephenmhopper@gmail.com" "First email" "name" "Stephen" "url" "http://www.google.com")
