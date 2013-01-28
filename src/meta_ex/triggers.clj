(ns meta-ex.triggers
  (:use [overtone.live]
        [overtone.synth.timing]))

(defonce triggers-g (group "M-x triggers" :tgt (foundation-safe-pre-default-group)))

(defonce trg-b (control-bus))
(defonce beat-b (control-bus))
(defonce cnt-b (control-bus))

(defonce trigger-s (trigger :tgt triggers-g :out-bus trg-b))
(defonce divider-s (divider :tgt triggers-g :in-bus trg-b :out-bus beat-b))
(defonce counter-s (counter :tgt triggers-g :in-bus beat-b :out-bus cnt-b))
