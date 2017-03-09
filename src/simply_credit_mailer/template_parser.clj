(ns simply-credit-mailer.template-parser
  (:gen-class)
  (:require [selmer.parser :as parser]
            [clojure.java.io :as io]))

(defn template-exists?
  "Returns true if a template with the given name exists; else false."
  [template-name]
  (-> (str template-name ".html")
      io/resource
      nil?
      not))

(defn parse-template
  "Parses and renders the given template from file with the specified argument map."
  [template-name arg-map]
  (let [filename (str template-name ".html")]
    (parser/render-file filename arg-map)))
