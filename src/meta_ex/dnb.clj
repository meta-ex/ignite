(ns meta-ex.dnb
  (:use [overtone.live]
        [meta-ex.kit.mixer]
        [meta-ex.kit.sched-sampler])
  (:require [meta-ex.kit.timing :as tim]
            [meta-ex.sets.samples :as samps]))

(do
  (def dnb-g (group))
  (defonce trance-kick (freesound 193025))
  (defonce oceanwavescrushing (freesound 48412))
  (defonce tibetanchant (freesound 15488))
  (defonce notresponsible (freesound 33711))
  (defonce alienwhisper (freesound 9665))
  (defonce drumnbass (freesound 40106))
  (defonce intro (freesound 9690))
  (defonce grenade (freesound 33245))
  (defonce bunga (freesound 139090))
  (defonce crazy-bpm-noise (freesound 193751))
  (defonce wobble-bass (freesound 202384))
  (defonce shizzle5 (freesound 43165))
  (defonce lfobass (freesound 19878))
  (defonce bass-loop (freesound 115269))
  (defonce rave (freesound 152840))
  (defonce warpbas (freesound 43072))
  (defonce chordstab (freesound 87144))
  (defonce kiteflyer (freesound 190829))

  (def oc (oceanwavescrushing [:head dnb-g] :out-bus 10 :loop? true :amp 1))
  (def nr (notresponsible [:head dnb-g] :rate 1 :amp 0 :out-bus 10 :loop? true))
  (def aw (alienwhisper [:head dnb-g] :rate 1 :out-bus 10 :loop? true :amp 0)))




(def foo (drumnbass :loop? true))
(ctl foo :rate 0.5)
(stop)
(def dnb (drumnbass [:head dnb-g] :loop? true :out-bus (nkmx :s2) :rate 0.5))
(ctl dnb :rate 1)
(def tibet2 (tibetanchant [:head dnb-g] :loop? 1 :out-bus 0 :rate (/ 4 3) :out-bus (nkmx :m1)))

(ctl tibet2 :rate 0.5)
(kill tibet2)
(kill dnb-g)
(kill foo)
(stop)

(def tibet1 (tibetanchant [:head dnb-g] :loop? 1 :out-bus 0 :rate 1 :out-bus (nkmx :m0)))
(def nr (notresponsible [:head dnb-g] :rate 1 :amp 4 :out-bus (nkmx :s0) :loop? true))
(def oc (oceanwavescrushing [:head dnb-g] :out-bus (nkmx :m0) :loop? true :amp 1))

(def b (bunga))



(def shiz (schedule-sample shizzle5 tim/beat-main :rate 0.5 :loop? 1 :out-bus (nkmx :s1)))
(def lfob (schedule-sample lfobass tim/beat-main :loop? 1 :out-bus (nkmx :s0)))
(def baslop (schedule-sample bass-loop tim/beat-main :loop? 1 :out-bus (nkmx :s0)))
(def wrpbs (schedule-sample warpbas tim/beat-main :loop? 1))
(def ravv (schedule-sample rave tim/beat-main :loop? 1 :rate 0.5 :out-bus (nkmx :s2)))
(def ravv2 (schedule-sample rave tim/beat-main :loop? 1 :rate 1  :out-bus (nkmx :s2)))
(def bung (schedule-sample bunga tim/beat-main :loop? 1))
(def wobbs (schedule-sample wobble-bass tim/beat-main :loop? 1))
(def kf (schedule-sample kiteflyer tim/beat-main :loop? 1))
(def kf2 (schedule-sample kiteflyer tim/beat-main :loop? 1 :rate 0.5))
(def dev (schedule-sample samps/devoxx tim/beat-main :loop? 1 :rate 0.7 :out-bus (nkmx :s0)))

(def dram (schedule-sample samps/dramatic-loop tim/beat-main :loop? 0 :rate 1 :out-bus (nkmx :s0)))

(ctl shiz :loop? 0)
(ctl ravv :loop? 0)
(ctl wobbs :loop? 0)
(ctl lfob :loop? 0)
(ctl dev :rate 0.5 :loop? 0)
(ctl baslop :rate 0.5 :loop? 0 :out-bus (nkmx :s0))
(ctl kf :loop? 1 :amp 2 :out-bus (nkmx :s0))
(ctl kf2 :loop? 1 :amp 0)
(ctl dev :rate 0.5 :loop? 0)

(do
  (ctl kf :loop? 0)
  (ctl kf2 :loop? 0))
(do
  (ctl ravv :loop? 0)
  (ctl ravv2 :loop? 0))

(do
  (kill ravv)
  (kill ravv2))
(kill baslop)
()
(kill kf)
(ctl bung :loop? 0 :amp 1)
(ctl wrpbs :loop? 0 :amp 1.5 :out-bus (nkmx :s0))



(kiteflyer [:head dnb-g] :rate 0.5)

(warpbas [:head dnb-g])

(ctl rave-s :out-bus (nkmx :s0))
()
(kill lfob)
(kill dnb-g)
(kill 674)
(kill shizzle5)
(kill tibet2)
(kill nr)
(kill oc)
(ctl tibet2  :rate 0.5 :out-bus (nkmx :m1))
(ctl tibet2  :rate 1)
(ctl tibet1 :rate 0.5)

(ctl tibet1 :rate (/ 3 2))
(ctl tibet1 :rate (/ 4 3))
(ctl tibet1 :rate 2)
(ctl tibet1 :rate 1)

(kill 545)
(defn honour-vote [colour]
  (cond
   (= "GREEN" colour) (do (ctl oc :amp 4)
                          (ctl nr :amp 0)
                          (ctl aw :amp 0))
   (= "PINK" colour) (do (ctl oc :amp 0)
                        (ctl nr :amp 6)
                        (ctl aw :amp 0))
   (= "BLUE" colour) (do (ctl oc :amp 0)
                         (ctl nr :amp 0)
                         (ctl aw :amp 3))))

(defn honour-vote [colour]
  (cond
   (= "GREEN" colour) (do (ctl tibet2 :rate (/ 3 2)))
   (= "PINK" colour) (do (ctl tibet2 :rate (/ 4 3)))
   (= "BLUE" colour) (do (ctl tibet2 :rate 2))))


(on-event [:vote :new-lead] (fn [msg]
                              (honour-vote (:new-lead msg)))
          ::honour-vote)

(honour-vote "GREEN")
(honour-vote "PINK")
(honour-vote "BLUE")

(ctl tibet2 :out-bus 0)
(ctl tibet2 :out-bus 10)

(ctl tibet2 :rate 0.5)
(ctl dnb :amp 0)
(kill dnb)
(def alien (alienwhisper [:head dnb-g] :rate 2 :out-bus (nkmx :s2)))
(ctl alien :out-bus (nkmx :s0))
(grenade :amp 2 :rate 1 :out-bus (nkmx :s0))

(def i (intro :rate 1))
(grenade :amp 0.5 :rate 0.4)

(stop)
