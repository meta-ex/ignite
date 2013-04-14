(ns meta-ex.keyboard
  (:use [overtone.live]
        [overtone.synth.sampled-piano]
        [meta-ex.kit.mixer]))

(on-event [:midi-device "KORG INC." "KEYBOARD" "nanoKEY2 KEYBOARD" 0 :note-on]
          (fn [msg]
            (sampled-piano (:note msg) :out-bus (nkmx :r0) :amp 2))
          ::keyboard)
