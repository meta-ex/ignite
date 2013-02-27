(ns meta-ex.arpegiator
  (:use [overtone.core]
        [meta-ex.synths]
         [meta-ex.mixer]))

(defonce arp-g (group))
(kill wo)
(def wo (woah :tgt arp-g))
(def wo2 (woah :tgt arp-g))
(ctl wo :out-bus (mx :grumbles))
(ctl wo :depth 0.5)
(ctl wo :rate 0.5)
(ctl wo :note 40)

(ctl wo :x 1000)
(kill wo)

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
