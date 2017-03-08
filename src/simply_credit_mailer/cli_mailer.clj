(ns simply-credit-mailer.cli-mailer
  (:gen-class)
  (:require [clojure.tools.cli :refer [parse-opts]]
            [simply-credit-mailer.template-parser :as template-parser]
            [simply-credit-mailer.mailer :as mailer]))

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

(defn -main [& args]
  (let [{:keys [options arguments errors summary] :as pargs} (parse-opts args (cli-options))]
    (condp = (first arguments)
      "send-message" (apply mailer/send-message (drop 1 arguments))
      "send-template" (apply mailer/send-template (drop 1 arguments))
      (println (usage)))))