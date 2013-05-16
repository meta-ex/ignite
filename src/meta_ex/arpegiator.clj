(ns meta-ex.arpegiator
  (:use [overtone.core]
        [meta-ex.synths.synths]
        [meta-ex.kit.mixer]))

(defonce arp-g (group))
(defonce wo (woah [:head arp-g] :note (note :a2) :rate 0.1 :depth 1 :out-bus (nkmx :m0)))

(ctl wo :depth 1)
(ctl wo :rate 0.1)
(ctl wo :amp 2)



(def freq (atom 0))

(on-event [:midi :note-on]
          (fn [msg]
            (let [note (- (:note msg ) 0)]
              (reset! freq note)
              (ctl wo :note note))

            )
          ::control-wo)


(kill wo)
