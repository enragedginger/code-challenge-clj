(ns simply-credit-mailer.onyx.tasks.mailer
  (require [schema.core :as s]
           [simply-credit-mailer.template-parser :as template-parser]
           [simply-credit-mailer.mailer :as mailer]))

(defn build-from-template [segment]
  (let [body (template-parser/parse-template (-> segment :template) (-> segment :args))]
    {:to (-> segment :to)
     :subject (-> segment :subject)
     :body (-> segment :body)}))

(s/defn build-from-template-task
  [task-name :- s/Keyword
   opts]
  {:task {:task-map (merge {:onyx/name task-name
                            :onyx/type :function
                            :onyx/fn :simply-credit-mailer.onyx.tasks.mailer/build-from-template
                            :onyx/doc "Transforms a segment into an email based on its template property"}
                           opts)
          :lifecycles []}})

(defn send-message [segment]
  (mailer/send-message (-> segment :to)
                       (-> segment :subject)
                       (-> segment :body)))

(s/defn send-message-task
  [task-name :- s/Keyword
   opts]
  {:task {:task-map (merge {:onyx/name task-name
                            :onyx/type :output
                            :onyx/fn :simply-credit-mailer.onyx.tasks.mailer/send-message
                            :onyx/doc "Transforms a segment into an email based on its template property"}
                           opts)
          :lifecycles []}})