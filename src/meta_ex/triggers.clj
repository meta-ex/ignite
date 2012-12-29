(ns meta-ex.triggers
  (:use [overtone.live]))

(defsynth trigger [rate 100 out-bus 0]
  (out:kr out-bus (impulse:kr rate)))

(defsynth counter [in-trg-bus 0 out-cnt-bus 0]
  (out:kr out-cnt-bus (pulse-count:kr (in:kr in-trg-bus))))

(defsynth divider [div 32 in-trg-bus 0 out-trg-bus 0]
  (out:kr out-trg-bus (pulse-divider (in:kr in-trg-bus) div)))

(defonce triggers-g (group "M-x triggers" :tgt (foundation-safe-pre-default-group)))

(defonce trg-b (control-bus))
(defonce beat-b (control-bus))
(defonce cnt-b (control-bus))

(defonce trigger-s (trigger :tgt triggers-g :out-bus trg-b))
(defonce divider-s (divider :tgt triggers-g :in-trg-bus trg-b :out-trg-bus beat-b))
(defonce counter-s (counter :tgt triggers-g :in-trg-bus beat-b :out-cnt-bus cnt-b))
