(ns meta-ex.triggers
  (:use [overtone.live]
        [overtone.synth.timing]
        [overtone.libs.counters]
        [meta-ex.monomes :as mon]
        [polynome.core :as poly]))

(defonce triggers-g (group "M-x triggers" :tgt (foundation-safe-pre-default-group)))

(defonce trg-b (control-bus))
(defonce beat-b (control-bus))
(defonce cnt-b (control-bus))

(defonce trigger-s (trigger :tgt triggers-g :out-bus trg-b))
(defonce divider-s (divider :tgt triggers-g :in-bus trg-b :out-bus beat-b))
(defonce counter-s (counter :tgt triggers-g :in-bus beat-b :out-bus cnt-b))

(defonce count-id (next-id ::foo))

;;(demo 10 (send-trig (in:kr beat-b) 100 (in:kr cnt-b)))

 (on-event "/tr" (fn [msg]
                   (when (= 100 (second (:args msg)))
                     (let [beat (nth (:args msg) 2)]
                       (poly/col (first (mon/monomes)) 7 [0 0 0 0 0 0 0 0])
                       ;;(poly/led-on (first (mon/monomes)) 0 (mod beat 8))
                       (println (mod beat 8))
                       ))) ::foo)

;; ()
;;(poly/led-off (first (mon/monomes)) 1 7)

;; (poly/col (first (mon/monomes)) 7 [1 1 1 1 1 1 1 1])


;; (count (mon/monomes))

;; (poly/disconnect (second (mon/monomes)))
