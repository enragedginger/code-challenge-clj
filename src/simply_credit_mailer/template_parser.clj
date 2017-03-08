(ns simply-credit-mailer.template-parser
  (:gen-class)
  (:require [selmer.parser :as parser]
            [clojure.java.io :as io]))

(defn template-exists? [template-name]
  (-> (str template-name ".html")
      io/resource
      nil?
      not))

(defn parse-template [template-name arg-map]
  (let [filename (str template-name ".html")]
    (parser/render-file filename arg-map)))
