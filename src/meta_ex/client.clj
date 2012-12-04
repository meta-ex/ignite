(ns meta-ex.client
  (:use compojure.core
        compojure.route
        aleph.tcp
        gloss.core
        lamina.core
        clojure.data.json
        clojure.pprint
        meta-ex.client-fn))

(def client (tcp-client {:host "sam.aaron.name", :port 9901, :frame (string :utf-8 :delimiters ["\n"])}))

(defn received [msg]
  (do-action (second (read-string msg)))
  )


(def __LISTEN__
  (let [out *out*]
    (receive-all @client (fn [msg]
                           (binding [*out* out]
                             (apply #'received [msg])
                             )))))
