(ns meta-ex.server.nrepl
  (:require [clojure.tools.nrepl.server :as nrepl]))

(defonce server (agent nil))

(defn start []
  (send server (fn [s]
                 (when s (nrepl/stop-server s))
                 (nrepl/start-server :port 4889))))

(defn stop []
  (send server (fn [s]
                 (when s (nrepl/stop-server s))
                 nil)))

(defonce __start-server__ (start))
