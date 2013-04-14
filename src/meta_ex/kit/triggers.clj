(ns meta-ex.kit.triggers
  (:use [overtone.live]
        [overtone.synth.timing]
        [overtone.libs.counters]))

(defonce triggers-g (group "M-x triggers" :tgt (foundation-safe-pre-default-group)))

(defonce trg-b (control-bus))
(defonce beat-b (control-bus))
(defonce cnt-b (control-bus))

(defonce count-trig-id (trig-id))

(defonce trigger-s (trigger :tgt triggers-g :out-bus trg-b))
(defonce divider-s (divider :tgt triggers-g :in-bus trg-b :out-bus beat-b))
(defonce counter-s (counter :tgt triggers-g :in-bus beat-b :out-bus cnt-b))

(defsynth get-beat [] (send-trig (in:kr beat-b) count-trig-id (in:kr cnt-b)))
(defonce get-beat-s (get-beat))

(defn set-rate
  "Set the rate of the global trigger synth. Vals betwen 50 - 300 work well."
  [r]
  (ctl trigger-s :rate r))

(set-rate 200)

(comment
  (set-rate 20)

  (doseq [x (range 20 250)]
    (set-rate x)
    (Thread/sleep 30))

  (doseq [x (range 250 20 -1)]
    (set-rate x)
    (Thread/sleep 1)))
