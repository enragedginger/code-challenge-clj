(ns simply-credit-mailer.template-parser
  (:gen-class)
  (:require [selmer.parser :as parser]))

(defn parse-template [template-name arg-map]
  (let [filename (str template-name ".html")]
    (parser/render-file filename arg-map)))
