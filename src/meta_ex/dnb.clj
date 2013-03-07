(ns meta-ex.dnb
  (:use [overtone.live]
        [meta-ex.mixer]))

(do
  (defonce dnb-g (group))
  (defonce oceanwavescrushing (sample (freesound-path 48412)))
  (defonce tibetanchant (sample (freesound-path 15488)))
  (defonce notresponsible (sample (freesound-path 33711)))
  (defonce alienwhisper (sample (freesound-path 9665)))
  (defonce drumnbass (sample (freesound-path 40106)))
  (defonce intro (sample (freesound-path 9690)))
  (defonce grenade (sample (freesound-path 33245)))

  (def oc (oceanwavescrushing :tgt dnb-g :out-bus 10 :loop? true :vol 1))
  (def nr (notresponsible :tgt dnb-g :rate 1 :vol 0 :out-bus 10 :loop? true))
  (def aw (alienwhisper :tgt dnb-g :rate 1 :out-bus 10 :loop? true :vol 0)))

(def dnb (drumnbass :tgt dnb-g :loop? true :out-bus 0))

(def tibet2 (tibetanchant :tgt dnb-g :loop? 1 :out-bus 0 :rate (/ 4 3) :out-bus (nkmx :m0)))

(def tibet1 (tibetanchant :tgt dnb-g :loop? 1 :out-bus 0 :rate 1 :out-bus (nkmx :m0)))
(def nr (notresponsible :tgt dnb-g :rate 1 :vol 1 :out-bus (nkmx :s1) :loop? true))
(def oc (oceanwavescrushing :tgt dnb-g :out-bus (nkmx :s0) :loop? true :vol 1))
(kill tibet1)
(ctl tibet2  :rate 0.5)
(ctl tibet2  :rate 1)
(ctl tibet1 :rate 0.5)

(ctl tibet2 :rate (/ 3 2))
(ctl tibet2 :rate (/ 4 3))
(ctl tibet2 :rate 2)
(ctl tibet2 :rate 0.5)

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
(def alien (alienwhisper :tgt dnb-g :rate ))
(ctl alien :out-bus (mx :grumbles))
(grenade :amp 2 :rate 0.25 :out-bus (mx :grumbles))

(def i (intro :rate 1))
(grenade :vol 2 :rate 0.2)
