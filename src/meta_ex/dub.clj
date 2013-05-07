(ns meta-ex.dub
  (:use [overtone.live]
        [meta-ex.kit.mixer]
        [meta-ex.synths.synths])
  (:require [meta-ex.kit.monome-sequencer :as ms]
            [meta-ex.kit.timing :as tim]
            [meta-ex.kit.sequencer :as seq]))

(defonce dub-g (group))

;;(stop)
;;(ms/stop-sequencer sequencer)
;;(kill dub-g)
(defsynth dubstep [note 40 wobble 32 hi-man 0 lo-man 0 sweep-man 0 deci-man 0 tan-man 0 shape 0 sweep-max-freq 3000 hi-man-max 1000 lo-man-max 500 beat-vol 0 lag-delay 0.5 amp 1 out-bus 0]
  (let [bpm   300
        wob   (in:kr tim/sin-b)
        sweep (lin-lin:kr (lag-ud wob 0.01 lag-delay) -1 1 400 sweep-max-freq)
        snd   (mix (saw (* (midicps note) [0.99 1.01])))
        snd   (lpf snd sweep)
        snd   (normalizer snd)

        snd   (bpf snd 1500 2)
        ;;special flavours
        ;;hi manster
        snd   (select (> hi-man 0.05) [snd (* 4 (hpf snd hi-man-max))])

        ;;sweep manster
        snd   (select (> sweep-man 0.05) [snd (* 4 (hpf snd sweep))])

        ;;lo manster
        snd   (select (> lo-man 0.05) [snd (lpf snd lo-man-max)])

        ;;decimate
        snd   (select (> deci-man 0.05) [snd (round snd 0.1)])

        ;;crunch
        snd   (select (> tan-man 0.05) [snd (tanh (* snd 5))])

        snd   (* 0.5 (+ (* 0.8 snd) (* 0.3 (g-verb snd 100 0.7 0.7))))
        ]
    (out out-bus (* amp (normalizer snd)))))



(def ssaw-rq 0.5)
(def ssaw-fil-mul 3)

;; get the dubstep bass involved
(dubstep [:head dub-g]
         :note 28
         :lo-man 1
         :hi-man 1
         :amp 0.5
         :out-bus (nkmx :s1))

;;(ctl dubstep :out-bus 10)
;;(kill dub-g)
;; go crazy - especially with the deci-man

(ctl dub-g
     :note 28
     :hi-man  0
     :lo-man 0
     :deci-man 0
     :amp 0.4
     :out-bus (nkmx :s1))
;;(kill dub-g)

;; Bring in the supersaws!

(def ssaw-rq 0.4)
(def ssaw-fil-mul 2)


(supersaw2 (midi->hz (note :c2)) :amp 3 :fil-mul ssaw-fil-mul :rq ssaw-rqbbb)


;; Fire at will...
(supersaw2 (midi->hz 28) :amp 2 :fil-mul ssaw-fil-mul :rq ssaw-rq :out-bus (nkmx :m0))
(supersaw2 (midi->hz 40) :amp 2 :fil-mul ssaw-fil-mul :rq ssaw-rq :out-bus (nkmx :m0))
(supersaw2 (midi->hz 45) :amp 2 :fil-mul ssaw-fil-mul :rq ssaw-rq :out-bus (nkmx :m0))
(supersaw2 (midi->hz 48) :amp 2 :fil-mul ssaw-fil-mul :rq ssaw-rq :out-bus (nkmx :m0))
(supersaw2 (midi->hz 52) :amp 2 :fil-mul ssaw-fil-mul :rq ssaw-rq :out-bus (nkmx :m0))

(supersaw2 (midi->hz 57) :amp 1 :fil-mul ssaw-fil-mul :rq ssaw-rq :out-bus (nkmx :m0))
(supersaw2 (midi->hz 60) :amp 1 :fil-mul ssaw-fil-mul :rq ssaw-rq :out-bus (nkmx :m0))
(supersaw2 (midi->hz 64) :amp 1 :fil-mul ssaw-fil-mul :rq ssaw-rq :out-bus (nkmx :m0))

(supersaw2 (midi->hz 67) :amp 1 :fil-mul ssaw-fil-mul :rq ssaw-rq)
(supersaw2 (midi->hz 69) :amp 1 :fil-mul ssaw-fil-mul :rq ssaw-rq)
(supersaw2 (midi->hz 91) :amp 0.4 :fil-mul ssaw-fil-mul :rq ssaw-rq)

;; modify saw params on the fly too...
(ctl supersaw2 :fil-mul 4 :rq 0.8)
