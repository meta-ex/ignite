(ns meta-ex.touch
  (:use [overtone.core]))

(defonce server (osc-server 44100 "osc-clj"))
(defonce client (osc-client "192.168.20.1" 44101))

(osc-listen server (fn [msg]
                     (event [:touch-osc (:path msg)]
                            (assoc msg
                              :server server))

                     (event [:touch-osc]
                            (assoc msg
                              :server server)))
            ::touch-osc-incoming)

(on-event [:touch-osc]
          (fn [m]
            (println (:path m) (:args m)))
          ::foo)

;;(osc-send client "/1/pad/visible" 1)
