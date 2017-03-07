(ns simply-credit-mailer.mailer
  (:gen-class)
  (:require [aero.core :refer [read-config]]
            [clojure.java.io :as io]
            [clj-http.client :as http]
            [clojure.tools.cli :refer [parse-opts]]
            [simply-credit-mailer.template-parser :as template-parser]))

(defn contains-all? [m keys]
  (every? #(contains? m %) keys))

(defn cli-options []
  [["-t" "--to-address ADDRESS" "The recipient's email address"
    :parse-fn clojure.string/trim]

   ["-s" "--subject SUBJECT" "The subject line of the email"
    :parse-fn clojure.string/trim]

   ["-m" "--message BODY" "The body of the email"
    :parse-fn clojure.string/trim]])

(defn usage []
  (->> ["Simply Credit Mailer Utility"
        " Sends arbitrary email messages."
        "Usage: action [args]"
        ""
        "Actions:"
        "  send-message [to-address] [subject] [body]"
        "  send-template [template-name] [to-address] [subject] [arg-pairs]"]
       (clojure.string/join \newline)))

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

(defn send-template [template-name to-address subject & args]
  (let [conf (build-mail-config)
        arg-map (apply args-to-map args)
        body (template-parser/parse-template template-name arg-map)]
    (send-mail-http conf to-address subject body)))

;(send-message "stephenmhopper@gmail.com" "First email" "Text goes here")
;(send-template "welcome" "stephenmhopper@gmail.com" "First email" "name" "Stephen")
;(send-template "welcome" "stephenmhopper@gmail.com" "First email" "name" "Stephen" "url" "http://www.google.com")

(defn -main [& args]
  (let [{:keys [options arguments errors summary] :as pargs} (parse-opts args (cli-options))]
    (condp = (first arguments)
      "send-message" (apply send-message (drop 1 arguments))
      "send-template" (apply send-template (drop 1 arguments))
      (println (usage)))))
