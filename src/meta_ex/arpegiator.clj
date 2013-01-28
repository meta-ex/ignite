(ns meta-ex.arpegiator
  (:use [overtone.core]
        [meta-ex.synths]
         [meta-ex.mixer]))

(defonce arp-g (group))
(kill wo)
(def wo (woah :tgt arp-g))
(def wo2 (woah :tgt arp-g))
(ctl wo :out-bus (mx ))
(ctl wo :amp 2)
(ctl wo :range 0.5)
(ctl wo2 :x 1000)
(kill wo2)

(def freq (atom 0))


(demo (pan2 (square [(* 1 @freq)
                  (+ (* 1 @freq) 0.01)])))



(on-event [:midi :note-on]
          (fn [msg]
            (let [note (:note msg )]
              (println note)
              (reset! freq note)
              (ctl wo :note note))

            )
          ::control-wo)
