(ns simply-credit-mailer.kafka-helper
  (require [clojure.string :as str]
           [franzy.admin.zookeeper.client :as client]
           [franzy.admin.cluster :as k-cluster]
           [franzy.admin.topics :as k-topics]
           [franzy.serialization.serializers :refer [byte-array-serializer]]
           [franzy.clients.producer.client :as producer]
           [franzy.clients.producer.protocols :refer [send-sync! send-async!]])
  (:import [franzy.clients.producer.types ProducerRecord]))

(defn create-if-not-exists [zk-utils topic-name]
  (when (not (k-topics/topic-exists? zk-utils topic-name))
    (k-topics/create-topic! zk-utils topic-name 1)))

(defn create-topic [zk topic-name]
  (let [zk-utils (client/make-zk-utils {:servers [zk]} false)]
    (create-if-not-exists zk-utils topic-name)))

(defn gen-broker-khan [broker]
  (let [host (-> broker :endpoints :plaintext :host)
        port (-> broker :endpoints :plaintext :port)]
    (str host ":" port)))

(defn gen-broker-list [zk]
  (let [zk-utils (client/make-zk-utils {:servers [zk]} false)
        brokers (k-cluster/all-brokers zk-utils)
        broker-khans (map gen-broker-khan brokers)]
    broker-khans))

(defn gen-broker-str [zk]
  (let [brokers (gen-broker-list zk)]
    (clojure.string/join "," brokers)))

(defn write-message [zk topic message]
  (with-open [producer1 (producer/make-producer {:bootstrap.servers (gen-broker-list zk)} (byte-array-serializer) (byte-array-serializer))]
    (send-sync! producer1 (ProducerRecord. topic 0 nil (.getBytes (pr-str message))))))
