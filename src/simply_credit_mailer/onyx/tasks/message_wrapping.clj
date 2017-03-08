(ns simply-credit-mailer.onyx.tasks.message-wrapping
  (require [schema.core :as s]
           [onyx.plugin.seq]))

(defn wrap-it [topic partition message]
  (println "Message is:" message)
  {:message message
   :topic topic
   :key nil
   :partition partition})

(s/defn wrap-messages
  [task-name :- s/Keyword
   wrapping-topic :- s/Str
   wrapping-partition :- s/Int
   opts]
  {:task {:task-map (merge {:onyx/name task-name
                            :onyx/type :function
                            :onyx/fn :simply-credit-mailer.onyx.tasks.message-wrapping/wrap-it
                            :onyx/params [:wrapping-topic :wrapping-partition]
                            :wrapping-topic wrapping-topic
                            :wrapping-partition wrapping-partition}
                           opts)
          :lifecycles []}})
