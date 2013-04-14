(ns meta-ex.ambient
  (:use [overtone.live]
        [meta-ex.sets.ignite]
        [meta-ex.kit.mixer]))

(def stream ((:stream-under-bridge atmossy) :loop? true))
(def bird ((:birdsong atmossy) :loop? true))
(def bubbles1  ((:bubbles1  atmossy) :loop? true))
(def bubbles2  ((:bubbles2  atmossy) :loop? true))

(ctl stream :rate 0.3 :out-bus (nkmx :s1))
(ctl bird :rate 1 :out-bus (nkmx :s1))
(ctl bubbles1 :vol 0.5 :rate 0.3 :out-bus (nkmx :m0))
(ctl bubbles2 :vol 0.5 :rate 0.5 :out-bus (nkmx :m1))

(kill bird)
(kill stream)
(kill bubbles1)
(kill bubbles2)
