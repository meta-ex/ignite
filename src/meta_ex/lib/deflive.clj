(ns meta-ex.lib.deflive)

(def references (agent {}))

(defmacro deflive
  [name expr]
  `(let [v-fn# (fn [] ~expr)
         res# (promise)]
     (send references
           (fn [cur#]
             (let [k# [*ns* '~name]
                   r# (get-in cur# k#)]
               (if r#
                 (do (deliver res# cur#)
                     cur#)
                 (let [v#       (v-fn#)
                       new-cur# (assoc-in cur# k# v#)]
                   (def ~name v#)
                   (deliver res# v#)
                   new-cur#)))))
     @res))

(def 'b 1)

(use 'clojure.pprint)

(def references (agent {}))


(print (agent-error references))
(deflive b 5)

b
