(ns meta-ex.client-fn
  (:use overtone.live))

(defn do-action [msg]
  (event [:vote] :name (:voter msg) :choice (:choice msg)))
(comment
  (event [:vote] :name "fred" :choice "PINK")
  (event [:vote] :name "fred" :choice "GREEN")
  (event [:vote] :name "fred" :choice "BLUE"))
