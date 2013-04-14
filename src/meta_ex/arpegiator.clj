(ns meta-ex.arpegiator
  (:use [overtone.core]
        [meta-ex.synths.synths]
        [meta-ex.kit.mixer]))

(defonce arp-g (group))
(defonce wo (woah :tgt arp-g :note (note :a2) :rate 0.1 :depth 1))

(ctl wo :depth 1)
(ctl wo :rate 0.1)

(def freq (atom 0))

(on-event [:midi :note-on]
          (fn [msg]
            (let [note (- (:note msg ) 24)]
              (println note)
              (reset! freq note)
              (ctl wo :note note))

            )
          ::control-wo)
