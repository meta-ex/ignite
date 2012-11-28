(ns meta-ex.dnb
  (:use [overtone.live])
  (:require [meta-ex.mixer])
  )

(do
  (defonce dnb-g (group))
  (defonce oceanwavescrushing (sample (freesound-path 48412)))
  (defonce tibetanchant (sample (freesound-path 15488)))
  (defonce notresponsible (sample (freesound-path 33711)))
  (defonce alienwhisper (sample (freesound-path 9665)))
  (defonce drumnbass (sample (freesound-path 40106)))
  (defonce intro (sample (freesound-path 9690)))
  (defonce grenade (sample (freesound-path 33245))))

(def oc (oceanwavescrushing :tgt dnb-g :out-bus 0 :loop? true))
(def nr (notresponsible :tgt dnb-g :rate 1 :vol 8 :out-bus 0))
(def dnb (drumnbass :tgt dnb-g :loop? true :out-bus 10))

(def tibet1 (tibetanchant :tgt dnb-g :loop? 0 :out-bus 0 :rate 0.))
(stop)
(ctl tibet2 :out-bus 0)
(ctl tibet2 :out-bus 10)

(demo (sin-osc 80))
(ctl dnb :vol 1)

(def alien (alienwhisper :tgt dnb-g :rate 1))
(grenade :amp 2)
(def i (intro :rate 2))
(stop)
(grenade :vol 2)
(kill dnb)
