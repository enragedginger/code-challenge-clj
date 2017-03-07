(ns simply-credit-mailer.mailer
  (:gen-class)
  (:require [aero.core :refer [read-config]]
            [clojure.java.io :as io]
            [clj-http.client :as http]
            [clojure.tools.cli :refer [parse-opts]]))

(defn contains-all? [m keys]
  (every? #(contains? m %) keys))

(defn cli-options []
  [["-t" "--to-address ADDRESS" "The recipient's email address"
    :parse-fn clojure.string/trim]

   ["-s" "--subject SUBJECT" "The subject line of the email"
    :parse-fn clojure.string/trim]

   ["-m" "--message BODY" "The body of the email"
    :parse-fn clojure.string/trim]])

(defn usage [options-summary]
  (->> ["Simply Credit Mailer Utility"
        " Sends arbitrary email messages."
        "Usage: [options]"
        ""
        options-summary]
       (clojure.string/join \newline)))

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
  (let [{:keys [options arguments errors summary] :as pargs} (parse-opts args (cli-options))]
    (if (not (contains-all? options [:to-address :subject :message]))
      (println (usage summary))
      (send-message (-> options :to-address)
                    (-> options :subject)
                    (-> options :message)))))
