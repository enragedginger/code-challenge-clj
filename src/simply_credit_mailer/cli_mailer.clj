(ns simply-credit-mailer.cli-mailer
  (:gen-class)
  (:require [simply-credit-mailer.template-parser :as template-parser]
            [simply-credit-mailer.mailer :as mailer]))

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
    (condp = (first args)
      "send-message" (apply mailer/send-message (drop 1 args))
      "send-template" (apply mailer/send-template (drop 1 args))
      (println (usage))))