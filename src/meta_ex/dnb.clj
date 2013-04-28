(ns meta-ex.dnb
  (:use [overtone.live]
        [meta-ex.kit.mixer]))

(do
  (defonce dnb-g (group))
  (defonce oceanwavescrushing (sample (freesound-path 48412)))
  (defonce tibetanchant (sample (freesound-path 15488)))
  (defonce notresponsible (sample (freesound-path 33711)))
  (defonce alienwhisper (sample (freesound-path 9665)))
  (defonce drumnbass (sample (freesound-path 40106)))
  (defonce intro (sample (freesound-path 9690)))
  (defonce grenade (sample (freesound-path 33245)))

  (def oc (oceanwavescrushing [:head dnb-g] :out-bus 10 :loop? true :vol 1))
  (def nr (notresponsible [:head dnb-g] :rate 1 :vol 0 :out-bus 10 :loop? true))
  (def aw (alienwhisper [:head dnb-g] :rate 1 :out-bus 10 :loop? true :vol 0)))

(def foo (drumnbass :loop? true))
(ctl foo :rate 1)
(stop)
(def dnb (drumnbass [:head dnb-g] :loop? true :out-bus (nkmx :s0) :rate 0.1))

(def tibet2 (tibetanchant [:head dnb-g] :loop? 1 :out-bus 0 :rate (/ 4 3) :out-bus (nkmx :m0)))

(def tibet1 (tibetanchant [:head dnb-g] :loop? 1 :out-bus 0 :rate 1 :out-bus (nkmx :m1)))
(def nr (notresponsible [:head dnb-g] :rate 1 :vol 1 :out-bus (nkmx :s1) :loop? true))`
(def oc (oceanwavescrushing [:head dnb-g] :out-bus (nkmx :m0) :loop? true :vol 1))

(kill tibet2)
(kill nr)
(ctl tibet2  :rate 0.5)
(ctl tibet2  :rate 1)
(ctl tibet1 :rate 0.5)

(ctl tibet1 :rate (/ 3 2))
(ctl tibet1 :rate (/ 4 3))
(ctl tibet1 :rate 2)
(ctl tibet1 :rate 1)


(defn honour-vote [colour]
  (cond
   (= "GREEN" colour) (do (ctl oc :vol 4)
                          (ctl nr :vol 0)
                          (ctl aw :vol 0))
   (= "PINK" colour) (do (ctl oc :vol 0)
                        (ctl nr :vol 6)
                        (ctl aw :vol 0))
   (= "BLUE" colour) (do (ctl oc :vol 0)
                         (ctl nr :vol 0)
                         (ctl aw :vol 3))))

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
(ctl dnb :vol 0)
(kill dnb)
(def alien (alienwhisper [:head dnb-g] :rate 1 :out-bus (nkmx :s1)))
(ctl alien :out-bus (nkmx :s0))
(grenade :amp 2 :rate 0.25 :out-bus (nkmx :s0))

(def i (intro :rate 1))
(grenade :vol 0.5 :rate 0.4)

(stop)
