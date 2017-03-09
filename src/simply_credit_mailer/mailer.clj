(ns simply-credit-mailer.mailer
  (:gen-class)
  (:require [aero.core :refer [read-config]]
            [clojure.java.io :as io]
            [clj-http.client :as http]
            [simply-credit-mailer.template-parser :as template-parser]))

(defn contains-all? [m keys]
  (every? #(contains? m %) keys))

(defn valid-body-req?
  "Returns true iff the new-segment from Onyx is a valid email send request."
  [event old-segment new-segment all-new-segments]
  (contains-all? new-segment [:to :subject :body]))

(defn valid-template-req?
  "Returns true iff the new-segment from Onyx is a valid email template send request."
  [event old-segment new-segment all-new-segments]
  (and (contains-all? new-segment [:to :subject :template :args])
       (template-parser/template-exists? (-> new-segment :template))))

(defn build-mail-config
  "Reads the Mailgun configuration from the config.edn file on the classpath."
  []
  (-> (io/resource "config.edn")
      read-config
      :app-config
      :mailgun))

(defn send-mail-http
  "Sends an email address to the specified recipient with the given subject and body text."
  [conf to-address subject body]
  (let [{:keys [api-key from-address endpoint]} conf]
    (http/post endpoint {:form-params {
                                  :from from-address
                                  :to to-address
                                  :subject subject
                                  :text body
                                  }
                    :basic-auth ["api" api-key]})))

(defn send-message
  "Builds the mail configuration and sends a message to the specified recipient with the given subject and body text."
  [to-address subject body]
  (let [conf (build-mail-config)]
    (send-mail-http conf to-address subject body)))

(defn args-to-map
  "Converts an arbitrary number of arguments into an argument map. Plays nicely with 'apply'."
  [& args]
  (let [arg-map (apply array-map args)]
    (into {} (map (fn [[k v]] [(keyword k) v]) arg-map))))

(defn send-template-with-arg-map
  "Sends a template using the supplied template name, recipient address, subject, and template argument map."
  [template-name to-address subject arg-map]
  (let [conf (build-mail-config)
        body (template-parser/parse-template template-name arg-map)]
    (send-mail-http conf to-address subject body)))

(defn send-template
  "Sends a template using the supplied template name, recipient address, subject, and argument list"
  [template-name to-address subject & args]
  (let [conf (build-mail-config)
        arg-map (apply args-to-map args)]
    (send-template-with-arg-map template-name to-address subject arg-map)))
