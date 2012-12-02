(ns meta-ex.client-fn
  (:use overtone.live))

(defn do-action [msg]
  (event [:vote] :name (:voter msg) :choice (:choice msg)))
