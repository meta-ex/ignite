(ns meta-ex.scratch
  (:use [overtone.live]
        [meta-ex.sets.ignite]
        [meta-ex.kit.mixer]
        [meta-ex.synths.synths]))

(defonce s (spacey :out-bus (nkmx :s0)))

(kill s)

(defonce cs (cs80 :out-bus (nkmx :s0) :freq (midi->hz (note :g1))))

(ctl cs :out-bus (nkmx :s0) :freq (midi->hz (note :g1)))
(kill cs)

(on-event [:midi :note-on]
          (fn [msg]
            (let [note (- (:note msg )0)]
              (ctl cs :freq (midi->hz note)))

            )
          ::control-cs)
