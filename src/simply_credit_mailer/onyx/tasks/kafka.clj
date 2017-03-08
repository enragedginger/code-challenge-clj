(ns simply-credit-mailer.onyx.tasks.kafka
  (require [schema.core :as s]
           [onyx.plugin.kafka]))

(s/defn input
  [task-name :- s/Keyword
   kafka-topic :- s/Str
   kafka-group-id :- s/Str
   kafka-zk :- s/Str
   opts]
  {:task {:task-map (merge {:onyx/name task-name
                            :onyx/plugin :onyx.plugin.kafka/read-messages
                            :onyx/type :input
                            :onyx/medium :kafka
                            :onyx/min-peers 1
                            :onyx/max-peers 1
                            :kafka/topic kafka-topic
                            :kafka/group-id kafka-group-id
                            :kafka/receive-buffer-bytes 65536
                            :kafka/zookeeper kafka-zk
                            :kafka/offset-reset :latest
                            :kafka/force-reset? false
                            :kafka/commit-interval 500
                            :kafka/deserializer-fn :onyx.tasks.kafka/deserialize-message-edn
                            :kafka/wrap-with-metadata? false
                            }
                           opts)
          :lifecycles [{:lifecycle/task task-name
                        :lifecycle/calls :onyx.plugin.kafka/read-messages-calls}]}})

(s/defn output
  [task-name :- s/Keyword
   kafka-topic :- s/Str
   kafka-zk :- s/Str
   opts]
  {:task {:task-map (merge {:onyx/name task-name
                            :onyx/plugin :onyx.plugin.kafka/write-messages
                            :onyx/type :output
                            :onyx/medium :kafka
                            :kafka/no-seal? true
                            :onyx/min-peers 1
                            :onyx/max-peers 1
                            :kafka/topic kafka-topic
                            :kafka/zookeeper kafka-zk
                            :kafka/request-size 307200
                            :kafka/serializer-fn :onyx.tasks.kafka/serialize-message-edn}
                           opts)
          :lifecycles [{:lifecycle/task task-name
                        :lifecycle/calls :onyx.plugin.kafka/write-messages-calls}]}})
