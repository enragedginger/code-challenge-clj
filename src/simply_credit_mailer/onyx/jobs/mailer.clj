(ns simply-credit-mailer.onyx.jobs.mailer
  (require [schema.core :as s]
           [clj-http.client :as http]
           [onyx.job :refer [add-task register-job]]
           [onyx.tasks.core-async :as core-async-task]
           [onyx.plugin.kafka]
           [simply-credit-mailer.onyx.tasks [mailer :as mailer] [kafka :as kafka] [message-wrapping :as wrap-messages]]))

(defn build-base-job [batch-settings]
  (let [valid-template-req-fn :simply-credit-mailer.mailer/valid-template-req?
        valid-body-req-fn :simply-credit-mailer.mailer/valid-body-req?
        base-job {:workflow [[:in :build-from-template]
                             [:in :wrap-invalid-request]
                             [:wrap-invalid-request :invalid-request-out]
                             [:build-from-template :send]
                             [:in :send]]
                  :catalog []
                  :lifecycles []
                  :windows []
                  :triggers []
                  :flow-conditions [{:flow/from      :in
                                     :flow/to        [:build-from-template]
                                     :flow/predicate valid-template-req-fn}
                                    {:flow/from      :in
                                     :flow/to        [:send]
                                     :flow/predicate valid-body-req-fn}
                                    {:flow/from      :in
                                     :flow/to        [:wrap-invalid-request]
                                     :flow/predicate [:not [:or valid-template-req-fn valid-body-req-fn]]}]
                  :task-scheduler  :onyx.task-scheduler/balanced}]
    base-job))

(defn build-mailer-job [app-config zk batch-settings]
  (let [base-job (build-base-job batch-settings)
        {:keys [kafka-topic-in kafka-group-id kafka-topic-invalid-out kafka-partition]} app-config]
    (-> base-job
        (add-task (kafka/input :in kafka-topic-in kafka-group-id zk batch-settings))
        (add-task (mailer/build-from-template-task :build-from-template batch-settings))
        (add-task (mailer/send-message-task :send batch-settings))
        (add-task (wrap-messages/wrap-messages :wrap-invalid-request kafka-topic-invalid-out kafka-partition batch-settings))
        (add-task (kafka/output :invalid-request-out kafka-topic-invalid-out zk batch-settings))
        )))

(defmethod register-job "mailer"
  [job-name config]
  (let [batch-settings {:onyx/batch-size 100 :onyx/batch-timeout 1000}
        zk (-> config :env-config :zookeeper/address)
        app-config (-> config :app-config)]
    (build-mailer-job app-config zk batch-settings)))