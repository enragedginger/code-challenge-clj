(ns simply-credit-mailer.onyx.core
  (:gen-class)
  (:require [aero.core :refer [read-config]]
            [clojure.java.io :as io]
            [clojure.tools.cli :refer [parse-opts]]
            [lib-onyx.peer :as peer]
            [onyx.job]
            [onyx.api]
            [onyx.test-helper]
            [simply-credit-mailer.kafka-helper :as kafka-helper]
            [simply-credit-mailer.mailer :as mailer]
    ;; Load plugin classes on peer start
            [onyx.plugin [core-async]]
    ;; Load our tasks
            [simply-credit-mailer.onyx.tasks [kafka] [mailer] [message-wrapping]]
    ;; Load our jobs
            [simply-credit-mailer.onyx.jobs [mailer]]))

(defn file-exists?
  "Check both the file system and the resources/ directory
  on the classpath for the existence of a file"
  [file]
  (let [f (clojure.string/trim file)
        classf (io/resource file)
        relf (when (.exists (io/as-file f)) (io/as-file f))]
    (or classf relf)))

(defn cli-options []
  [["-c" "--config FILE" "Aero/EDN config file"
    :default (io/resource "config.edn")
    :default-desc "resources/config.edn"
    :parse-fn file-exists?
    :validate [identity "File does not exist relative to the workdir or on the classpath"
               read-config "Not a valid Aero or EDN file"]]

   ["-p" "--profile PROFILE" "Aero profile"
    :parse-fn (fn [profile] (clojure.edn/read-string (clojure.string/trim profile)))]

   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["Onyx Peer and Job Launcher"
        ""
        "Usage: [options] action [arg]"
        ""
        "Options:"
        options-summary
        ""
        "Actions:"
        "  start-peers [npeers]            Start Onyx peers."
        "  submit-job  [job-name]          Submit a registered job to an Onyx cluster."
        "  create-topics                   Creates Kafka topics for this job to use."
        "  submit-message [to-address] [subject] [body]"
        "  submit-template [template-name] [to-address] [subject] [arg-pairs]"
        ""]
       (clojure.string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (clojure.string/join \newline errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn assert-job-exists [job-name]
  (let [jobs (methods onyx.job/register-job)]
    (when-not (contains? jobs job-name)
      (exit 1 (error-msg (into [(str "There is no job registered under the name " job-name "\n")
                                "Available jobs: "] (keys jobs)))))))

(defn create-topics [config]
  (let [{:keys [app-config env-config]} config
        zk (-> env-config :zookeeper/address)
        topic-name-in (-> app-config :kafka-topic-in)
        topic-name-invalid (-> app-config :kafka-topic-invalid-out)]
    (kafka-helper/create-topic zk topic-name-in)
    (kafka-helper/create-topic zk topic-name-invalid)))

(defn submit-message [config [to subject body]]
  (let [{:keys [app-config env-config]} config
        zk (-> env-config :zookeeper/address)
        topic-name (-> app-config :kafka-topic-in)
        message {:to to
                 :subject subject
                 :body body}]
    (kafka-helper/write-message zk topic-name message)))

(defn submit-template [config [template to subject & arg-pairs]]
  (let [{:keys [app-config env-config]} config
        zk (-> env-config :zookeeper/address)
        topic-name (-> app-config :kafka-topic-in)
        arg-map (apply mailer/args-to-map arg-pairs)
        message {:to to
                 :subject subject
                 :template template
                 :args arg-map}]
    (kafka-helper/write-message zk topic-name message)))

(defn -main [& args]
  (let [{:keys [options arguments errors summary] :as pargs} (parse-opts args (cli-options))
        action (first args)]
    (cond (:help options) (exit 0 (usage summary))
          (= (count arguments) 0) (exit 1 (usage summary))
          errors (exit 1 (error-msg errors)))
    (case action
      "start-peers" (let [{:keys [env-config peer-config] :as config}
                          (read-config (:config options) {:profile (:profile options)})
                          n-peers (clojure.edn/read-string (second args))]
                      (peer/start-peer n-peers peer-config env-config))

      "submit-job" (let [{:keys [peer-config] :as config}
                         (read-config (:config options) {:profile (:profile options)})
                         argument (clojure.edn/read-string (second args))
                         job-name (if (keyword? argument) argument (str argument))]
                     (assert-job-exists job-name)
                     (let [job-id (:job-id
                                    (onyx.api/submit-job peer-config
                                                         (onyx.job/register-job job-name config)))]
                       (println "Successfully submitted job: " job-id)
                       (println "Blocking on job completion...")
                       (onyx.test-helper/feedback-exception! peer-config job-id)
                       (exit 0 "Job Completed")))
      "create-topics" (create-topics (read-config (:config options) {:profile (:profile options)}))
      "submit-message" (submit-message (read-config (:config options) {:profile (:profile options)}) (drop 1 arguments))
      "submit-template" (submit-template (read-config (:config options) {:profile (:profile options)}) (drop 1 arguments))
      )))
