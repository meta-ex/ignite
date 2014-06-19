(ns meta-ex.jscratch
  (:use [overtone.core]
        [meta-ex.sets.ignite]
        [meta-ex.kit.mixer]
        [meta-ex.kit.sched-sampler]
        [meta-ex.jon])
  (:require [meta-ex.drums]
            [meta-ex.sets.samples :as samps]
            [meta-ex.kit.timing :as tim]))

(recording-start "~/Desktop/clojure-exchange2013.wav")
(recording-stop)

(def bb (tim/beat-bus 1))
(def bb2 (tim/beat-bus 0.5))
(def bb3 (tim/beat-bus 2))

(dotimes [x 100]
  (tim/beat-bus 1))

(stop)
(scope (:beat bb))
(scope (:beat bb2))
(scope (:beat bb3))

(defsynth beeper [freq 200 bb 0]
  (out 0 (* (env-gen (perc 0.001 0.1) :gate (in:kr bb))
            (sin-osc freq))))

(def b1 (beeper 800 :bb (:beat bb)))
(def b2 (beeper 800 :bb (:beat bb2)))
(def b3 (beeper 800 :bb (:beat bb3)))


(kill beeper)

(def c2-ac (schedule-sample samps/c2-acid tim/beat-main :amp 5 :loop? 0))
(def s-perl (schedule-sample samps/pearly tim/beat-main :amp 0.5 :loop? 0 :rate 1))
(def s-beeps (schedule-sample samps/beeps-120 tim/beat-main :amp 0.5 :loop? 1))
(def s-guit (schedule-sample samps/guitar-bass tim/beat-main :amp 1 :loop? 1))
(ctl s-guit :amp 4 :rate 1 :loop? 0)
(def s-dwob (schedule-sample samps/dub-wob tim/beat-main :amp 1 :loop? 0))

(def s-spacb (schedule-sample samps/space-bass tim/beat-main :amp 1 :loop? 1))

(def s-lbass (schedule-sample samps/loop-1-bass tim/beat-main :amp 1 :loop? 1)
  )




(def s-engr (schedule-sample samps/energy-drum  tim/beat-main :amp 1 :rate 2 :loop? 1))


(def s-dram (schedule-sample samps/dramatic-loop  tim/beat-main :amp 1 :loop? 0))


(def s-scrm (schedule-sample samps/scream-wobble  tim/beat-main :amp 1 :loop? 1 :rate 2 :out-bus (nkmx :s0)))

(def s-subb (schedule-sample samps/subbass-wobble  tim/beat-main :amp 1 :rate 1 :loop? 0))

(def s-plng (schedule-sample samps/plingers-delight  tim/beat-main :amp 1 :loop? 0))

(def s-dpb (schedule-sample samps/deep-bass  tim/beat-main :amp 1 :loop? 0 :rate 2))

(def s-spcb2 (schedule-sample samps/space-bass2  tim/beat-main :amp 1 :rate 1 :loop? 1))

(def s-spcb2 (schedule-sample samps/space-bass2  tim/beat-main :amp 1 :rate 2 :loop? 0))

(def s-whoosh (schedule-sample samps/whoosh06  tim/beat-main :amp 1 :loop? 0))
(def s-whoosh2 (schedule-sample samps/transitional-whoosh  tim/beat-main :amp 1 :loop? 0))




(ctl s-perl :rate 0.5 :loop? 0 :out-bus (nkmx :s0))
(ctl s-plng :rate 1 :loop? 0 :out-bus (nkmx :s0))
(ctl s-beeps :rate 1 :loop? 0 :out-bus (nkmx :s0))
(ctl s-guit  :loop? 0 :amp 3 :rate 1)
(ctl s-dwob :rate 1 :loop? 0 :amp 1 :rate 0.5 :out-bus (nkmx :s0))
(ctl s-spacb :rate 1 :loop? 0 :amp 1 :rate 1 :out-bus (nkmx :s0))
(ctl s-spcb2 :rate 1 :loop? 0 :amp 1 :rate 1 :out-bus (nkmx :s0))
(ctl s-lbass :rate 1 :loop? 1 :amp 2 :rate 0.1 :out-bus (nkmx :s0))
(ctl s-engr :rate 1 :loop? 0 :amp 2 :rate 2 :out-bus (nkmx :s0))
(ctl s-dram :rate 1 :loop? 0 :amp 1 :rate 1 :out-bus (nkmx :s))
(ctl s-scrm :rate 0.5 :loop? 0 :amp 1 :rate 2 :out-bus (nkmx :s0))
(ctl s-subb  :loop? 0 :amp 0 :rate 1 :out-bus (nkmx :s2))

(kill s-dpb)
(kill s-spcb2)
(kill s-plng)
(kill s-engr)
(kill s-subb)
(kill s-spacb)
(kill s-bl)
(kill s-perl)
(kill s-guit)
(kill s-beeps)
(kill s-lbass)
(kill c2-ac)
(kill s-lbass

      )

(load-sequencer :m128 :jonseq :off)
(load-sequencer :m128 :jonseq :origbeat)
(load-sequencer :m128 :jonseq :origbeatclaps)
(load-sequencer :m128 :jonseq :origoffbeat)
(load-sequencer :m128 :jonseq :beats)
(load-sequencer :m128 :jonseq :2ndrow)
(load-sequencer :m128 :jonseq :ambientdrums)
(load-sequencer :m128 :jonseq :bleeps)
(load-sequencer :m128 :jonseq :bleeps1)
(load-sequencer :m128 :jonseq :bleeps2)
(load-sequencer :m128 :jonseq :bleeps3)
(load-sequencer :m128 :jonseq :darkorig)
(load-sequencer :m128 :jonseq :african)
(load-sequencer :m128 :jonseq :chilledafrican)
(load-sequencer :m128 :jonseq :metaex)
(load-sequencer :m128 :jonseq :clapclick1)
(load-sequencer :m128 :jonseq :bleeps5)
(load-sequencer :m128 :jonseq :subkick)

(save-sequencer :m128 :jonseq :beatsorig)

(load-sequencer :m64 :jonseq :m64-1)

(save-sequencer :m64 :jonseq :m64-3)


(data-riff-load-bass (flatten (concat (repeat 16 :c5) (repeat 16 :e5) (repeat 16 :c5) (repeat 8 :e5) (repeat 5 :g5) [:f5 :e5 :d5]))-0)
(data-riff-load-mid-hi (flatten (concat (repeat 5 [(repeat 16 :c5) (repeat 16 :e5)]) (repeat 1 [(repeat 16 :g5) (repeat 16 :e5)]) (repeat 4 [(repeat 4 :e5) (repeat 4 :g5)])))-24)
(data-riff-load-bass (flatten (concat (repeat 5 [(repeat 16 :c5) (repeat 16 :e5)]) (repeat 1 [(repeat 16 :g5) (repeat 16 :e5)]) (repeat 4 [(repeat 4 :e5) (repeat 4 :g5)])))-24)

(data-riff-load-bass (flatten (concat (repeat 4 [(repeat 16 :c5) (repeat 16 :e5)]) (repeat 4 [(repeat 16 :c5) (repeat 16 :g5)])))-24)
(data-riff-load-mid-hi (flatten (concat (repeat 4 [(repeat 16 :c5) (repeat 16 :e5)]) (repeat 4 [(repeat 16 :c5) (repeat 16 :g5)])))-24)

(data-riff-load-bass [:C4 :E4 :G4 :E4 :C6 :E4 :G4 :E4 :e6 :G4 :E6 :c4])
(data-riff-load-bass [:C4 :E4 :G4 :E4 :C6 :E4 :G4 :E4 :e6 :G4 :E6 :c6])
(data-riff-load-bass [:C4 :E4 :G4 :E4 :C4 :E6 :G4 :E4 :e6 :G4 :E6 :c4])
(data-riff-load-bass [:C4 :E4 :G6 :E6 :C4 :E6 :G4 :E4 :e6 :G4 :E4 :c4])
(data-riff-load-bass [:C4 :E4 :G4 :E4 :C4 :E4 :G4 :E4 :e4 :G4 :E4 :c4])
(data-riff-load-bass [:C4 :E4 :G4 :E4 :C6 :E4 :G4 :E4 :e6 :G4 :E6 :c4 :C5])
(data-riff-load-bass [:C4 :E4 :G4 :E6 :C4 :E6 :G4 :E4 :e4 :G6 :E4 :c4 :C5])
(data-riff-load-bass [:C4 :E6 :G6 :c4 :c6 :E6 :c4 :E4 :e4 :c4 :E4 :c4 :C5 :c4])
(data-riff-load-mid-hi [:C4 :E4 :G4 :E4 :C4 :E4 :G4 :E4 :e6 :G4 :E4 :c4 :e4 :c4])
(data-riff-load-mid-hi [:C4 :E4 :G4 :E4 :C4 :E4 :G4 :E4 :e6 :G4 :E4 :c4 :e4 :c6])
(data-riff-load-mid-hi [:C4 :E6 :G6 :E4 :C4 :E4 :G4 :E4 :e6 :G4 :E4 :c4 :e4 :c4])
(data-riff-load-mid-hi [:C4 :E6 :G6 :E4 :C6 :E4 :G4 :E4 :e6 :G4 :E4 :c4 :e4 :c4])
(data-riff-load-mid-hi [:C4 :E4 :G4 :E6 :C6 :E4 :G4 :E4 :e4 :G6 :E4 :c4 :e4 :c6])
(data-riff-load-mid-hi [:C4 :E4 :G4 :E4 :C4 :E4 :G4 :E4 :e4 :G6 :E4 :c4 :e4 :c6])

(data-riff-load-bass [:C4 :E4 :G4 :C5 :C5 :E4 :G4 :E4])
 (data-riff-load-mid-hi (flatten (concat (repeat 800 [:c4 :c5 :c6]) [:d6 :c6 :d6 :c6] (repeat 0 [:d4]) [:c6] (repeat 20 [:C4 :E4 :G4 :E4 :C6 :E4 :G4 :E4])))-12)
(data-riff-load-bass (flatten (concat (repeat 800 [:c4 :c5 :c6]) [:d6 :c6 :d6 :c6] (repeat 0 [:d4]) [:c6] (repeat 20 [:C4 :E4 :G4 :E4 :C6 :E4 :G4 :E4])))-17)
(data-riff-load-bass (flatten (concat (repeat 80 [:c5 :c#5 :d5 :c#5]) (repeat 20 [:c5 :c#5]) (repeat 20 [:c#5 :d5]) (repeat 1 [:c5 :b4 :a4 :g#4]) [:c3] (repeat 16 [:C4 :E4 :G4 :E4 :C3 :E4 :G4 :E4])))-0)
(data-riff-load-mid-hi (flatten (concat (repeat 10 [:c5 :c#5 :d5 :c#5]) (repeat 1 [:c5 :c#5]) (repeat 1 [:c#5 :d5]) (repeat 0 [:c5 :b4 :a4 :g#4]) [:c3] (repeat 30 [:C6 :E4 :G4 :E4 :C3 :E4 :G4 :E4])))-12)
(bass-rate 1/256)
(bass-wob-rate 1)

(bass-amp 1)
(hi-amp 0)
(mid-amp 1)

(mid-hi-rate 1/512)

(mid-hi-rate 1/1024)

(data-riff-load-mid-hi [:a5 :a4])
(data-riff-load-mid-hi [:a6 :a5])
(data-riff-load-mid-hi [:e5 :e4])
(data-riff-load-mid-hi [:e4 :e3])
(data-riff-load-mid-hi [:a4 :a3])
(data-riff-load-mid-hi [:c4 :c3])
(data-riff-load-mid-hi [:C4 :E4 :G4 :E4 :C5 :E4 :G4 :D5 :C5 :G4 :C5 :D5 :C5 :E4 :G4 :D4]-12)
(data-riff-load-mid-hi [:C4 :C4 :C4 :C4 :C5 :E4 :G4 :D5 :C5 :G4 :C5 :D5 :C5 :E4 :G4 :D4]-12)
(data-riff-load-mid-hi [:C4 :C4 :C4 :C4 :C5 :E4 :G4 :D5 :C5 :C5 :C5 :D5 :C5 :E4 :G4 :D4]-12)
(data-riff-load-mid-hi [:C4 :C4 :E4 :E4 :G4 :G4 :E4 :E4 :C5 :C5 :E4 :E4 :G4 :G4 :D4 :D4]-12)

(do
  (mid-hi-rate 1/512)
  (giorgio 0)
  (bass-amp 1))


(mid-hi-rate 1/2560)
(data-riff-load-mid-hi [:C4 :E4 :G4 :E4 :C5 :E4 :G4 :D5 :C5 :G4 :C5 :D5 :C5 :E4 :G4 :D4])
(data-riff-load-mid-hi [:C4 :C4 :C4 :C4 :C5 :E4 :G4 :D5 :C5 :G4 :C5 :D5 :C5 :E4 :G4 :D4])
(data-riff-load-mid-hi [:C4 :C4 :C4 :C4 :C5 :E4 :G4 :D5 :C5 :C5 :C5 :D5 :C5 :E4 :G4 :D4]-12)
(data-riff-load-mid-hi [:C4 :C4 :E4 :E4 :G4 :G4 :E4 :E4 :C5 :C5 :E4 :E4 :G4 :G4 :D4 :D4])
(giorgio 5)

(bass-map-keyboard-on)
(bass-map-keyboard-off)

(do

  (hi-amp 2)
  (mid-amp 1)
  (bass-amp 0)
  (giorgio 0)
  )
(giorgio 0)
(status)
(bass-amp 1)
(def bleep3-samples
  [(freesound 64072)
   (freesound 25882)
   (freesound 74233)
   (freesound 70106)
   (freesound 86101)])

(def bleep4-samples
  [(freesound 86101)
   (freesound 64072)
   (freesound 25882)
   (freesound 74233)
   (freesound 70106)])


((freesound 25882))
((freesound 74233))

  (hi-amp 0)
  (mid-amp 0)
  (bass-amp 0)








(status)
(do
  (rate (/ 120 7.5));;rate in bpm first, based on 1/4 beats

(def bleep0-samples
   [(freesound 70106);;replace 64072
    (freesound 25882)
    (freesound 777)
    (freesound 70106)
    (freesound 86101)])

 (def bleep3-samples
   [(freesound 64072)
    (freesound 25882)
    (freesound 74233)
    (freesound 70106)
    (freesound 86101)])

 (def bleep4-samples
   [(freesound 86101)
    (freesound 64072)
    (freesound 25882)
    (freesound 74233)
    (freesound 70106)])

 (def bleep5-samples
   [(freesound 86101)
    (freesound 64072)
    (freesound 25882)
    (freesound 74233)
    (freesound 25649)])

 (def subkicks-samples
   [(freesound 25649)
    (freesound 147482)
    (freesound 147480)
    (freesound 147479)
    (freesound 147478)])

 (def trigger1-samples [(sample (freesound-path 86773))
                        (sample (freesound-path 102720))
                        (sample (freesound-path 46092))
                        (sample (freesound-path 135117))
                        (sample (freesound-path 57143))
                        (sample (freesound-path 85487))
                        ])

 (def m64-1-samples
   [(sample (freesound-path 25649)) ;;subby
    (sample (freesound-path 47450))
    (sample (freesound-path 47452))
    (sample (freesound-path 47454))
    (sample (freesound-path 406))
    ])

 (def m64-2-samples
   [(sample (freesound-path 25649)) ;;subby
    (sample (freesound-path 47450))
    (sample (freesound-path 47452))
    (sample (freesound-path 406))
    ])

 (def m64-3-samples
   [(sample (freesound-path 25649)) ;;subby
    (sample (freesound-path 47450))
    (sample (freesound-path 47453))
    (sample (freesound-path 99844))
 ])

 (def m64-4-samples
   [(sample (freesound-path 25649)) ;;subby
    (sample (freesound-path 47450))
    (sample (freesound-path 34272));;cow bells
    (sample (freesound-path 70058))
    ])




 (def spooky-samples
   [(sample (freesound-path 197547))
    (sample (freesound-path 170174))
    (sample (freesound-path 147881))
    (sample (freesound-path 204421))
    (sample (freesound-path 169752))
    (sample (freesound-path 167918))])

 (def metaexwoosh-samples [(sample (freesound-path 88532))   ;;woosh
                           (sample (freesound-path 90143))   ;;steam
                           (sample "~/Dropbox/jon-n-sam/audio-files/live.wav");;live
                           (sample "~/Dropbox/jon-n-sam/audio-files/Meta-ex.wav");;Meta-ex
                           (sample "~/Dropbox/jon-n-sam/audio-files/livefromAntwerp.wav");;live from Antwerp we're Meta-eX
                           (sample "~/Dropbox/jon-n-sam/audio-files/Meta-ex live.wav");;Meta-ex live
                           ])

 (def africantrigger-samples
   [(sample (freesound-path 48403))
    (sample (freesound-path 62876))
    (sample (freesound-path 175934))
    (sample (freesound-path 35871))
    (sample (freesound-path 7207))
    (sample (freesound-path 76606))])

 (def mouthtrigger-samples
   [(sample (freesound-path 58784))
    (sample (freesound-path 4237))
    (sample (freesound-path 34667))
    (sample (freesound-path 27568))
    (sample (freesound-path 3176))
    (sample (freesound-path 104396))])

 (def bells-samples
   [(sample (freesound-path 25649));;subby
    (sample (freesound-path 53401));;cow bell
    (sample (freesound-path 34272));;cow bell
    (sample (freesound-path 70058));;cow bell
    (sample (freesound-path 99844));;shaker
    ])

 (def origshaker-samples [(sample (freesound-path 777))   ;;kick
                          (sample (freesound-path 406))   ;;click
                          (sample (freesound-path 25649)) ;;subby
                          (sample (freesound-path 85291));;wop
                          (sample (freesound-path 99844));;shaker
                          ]))






(def moretrigger-samples [(sample (freesound-path 17130));;rifle
                          (sample (freesound-path 2160));;cassette fast forward
                          (sample (freesound-path 50650));;
                          ]
  )





;;............................................................
(defonce orig-samples [(sample (freesound-path 777))   ;;kick
                       (sample (freesound-path 406))   ;;click
                       (sample (freesound-path 25649)) ;;subby
                       (sample (freesound-path 85291));;wop
                       ])

(defonce clapkick1-samples
  [(freesound 47452)
   (freesound 47453)
   (freesound 47454)
   (freesound 47450)
   (freesound 47451)])

(defonce clapkick2-samples
  [(freesound 47457)
   (freesound 47456)
   (freesound 47455)
   (freesound 47449)
   (freesound 47448)])

;;........................................................

(recording-start "~/Dropbox/jon-n-sam/audio-files/clicking.wav")
(recording-stop)

(sample "~/Dropbox/jon-n-sam/audio-files/clicking.wav")




(set-m64-beat-bus :half)
(set-m64-beat-bus :double)
(set-m64-beat-bus :main)
(set-m64-beat-bus :quarter)
(set-m64-beat-bus :eigth)

(set-m128-beat-bus :main)
(set-m128-beat-bus :half)
(set-m128-beat-bus :quarter)
(set-m128-beat-bus :eigth)



(rate (/ 120 7.5));;rate in bpm first, based on 1/4 beats

(on-event [:midi :note-on]
          (fn [msg]
            (sampled-piano (:note msg) :out-bus (nkmx :m0));;put semicolon at front of line to switch off
            )
          ::piano)
;;ONE.....................................
(do
  (swap-samples-128 origshaker-samples)
  (load-nk-bank :m128 :jon :orig)
  (load-sequencer :m128 :jonseq :beatsorig)
  (swap-trigs-128 spooky-samples)
  )

(do
  (swap-samples-128 origshaker-samples)
  (load-nk-bank :m128 :jon :safe)
  (load-sequencer :m128 :jonseq :2ndrow)
  (swap-trigs-128 spooky-samples)
  )

(load-nk-bank :m128 :jon :orig)

(do
  (swap-samples-128 origshaker-samples)
  (load-nk-bank :m128 :jon :orig)
  (load-sequencer :m128 :jonseq :2ndrow)
  (swap-trigs-128 spooky-samples)
  )

(load-sequencer :m128 :jonseq :origbeat)

(swap-trigs-128 trigger1-samples)


(do
  (swap-samples-64 m64-3-samples)
  (load-nk-bank :m64 :jon :m64-1)
  (load-sequencer :m64 :jonseq :m64-3)
  (swap-trigs-64 trigger1-samples))

(do
  (swap-samples-64 metaexwoosh-samples)
  (load-nk-bank :m64 :jon :metaexwoosh)
  (load-sequencer :m64 :jonseq :m64-1)
  (swap-trigs-64 trigger1-samples))

;;TWO.....................................
(do
  (swap-samples-128 clapkick1-samples)
  (load-nk-bank :m128 :jon :clapclick1)
  (load-sequencer :m128 :jonseq :clapclick1)
  (swap-trigs-128 trigger1-samples)
  )

;;THREE...................................
(do
  (swap-samples-128 bleep0-samples)
  (load-nk-bank :m128 :jon :bleep0)
  (load-sequencer :m128 :jonseq :bleep0)
  (swap-trigs-128 bleep0-samples)
  )

(do
  (swap-samples-128 bleep1-samples)
  (load-nk-bank :m128 :jon :bleeps5)
  (load-sequencer :m128 :jonseq :bleeps5)
  (swap-trigs-128 spooky-samples)
  )


(swap-samples-128 bleep5-samples)
(load-nk-bank :m128 :jon :bleep)
(load-sequencer :m128 :jonseq :bleep)

;;FOUR....................................
(do
  (swap-samples-128 subkicks-samples)
  (load-nk-bank :m128 :jon :subkick)
  (load-sequencer :m128 :jonseq :subkick)
  (swap-trigs-128 metaexwoosh-samples)
  )

(do
  (swap-samples-64 m64-2-samples)
  (load-nk-bank :m64 :jon :m64-2)
  (load-sequencer :m64 :jonseq :m64-2)
  (swap-trigs-64 trigger1-samples))

;;FIVE....................................
(do
  (swap-samples-128 africantrigger-samples)
  (load-nk-bank :m128 :jon :africantrigger)
  (load-sequencer :m128 :jonseq :africantrigger)
  (swap-trigs-128 african-samples)
  )


(do
  (swap-samples-128 african-samples)
  (load-nk-bank :m128 :jon :subkick)
  (load-sequencer :m128 :jonseq :african)
  (swap-trigs-128 africantrigger-samples)
  )

(do
  (load-nk-bank :m128 :jon :african)
  (load-sequencer :m128 :jonseq :african1)
  (swap-trigs-128 trigger1-samples))

(swap-trigs-128 africantrigger-samples)
(swap-trigs-128 spooky-samples)
;;SIX......................................
(do
  (swap-samples-128 mouth-samples)
  (load-nk-bank :m128 :jon :mouth)
  (load-sequencer :m128 :jonseq :mouth)
  (swap-trigs-128 mouthtrigger-samples)
  )

(do
  (swap-samples-128 bells-samples)
  (load-nk-bank :m128 :jon :mouth)
  (load-sequencer :m128 :jonseq :mouth)
  (swap-trigs-128 mouthtrigger-samples)
  )

(do
  (swap-samples-64 m64-1-samples)
  (load-nk-bank :m64 :jon :m64-3)
  (load-sequencer :m64 :jonseq :m64-3)
  (swap-trigs-64 trigger1-samples))

;;SEVEN..................................
(do
  (swap-samples-128 origshaker-samples)
  (load-nk-bank :m128 :jon :orig)
  (load-sequencer :m128 :jonseq :beats)
  (swap-trigs-128 trigger1-samples)
  )

(do
  (swap-samples-64 bells-samples)
  (load-nk-bank :m64 :jon :bells)
  (load-sequencer :m64 :jonseq :bells)
  (swap-trigs-64 trigger1-samples))

;;EIGHT...................................



(do
  (data-riff-load-mid-hi (flatten (concat (repeat 16 [:c4 :c5]) (repeat 16 [:C4 :E4]) (repeat 16 [:C4 :E3])))-0)
  (data-riff-load-bass (flatten (concat (repeat 4 [:c4 :e4 :a3 :c4]) (repeat 4 [:C4 :E4 :a4 :C4]) (repeat 4 [:C4 :a4 :e4 :C4])))+0)
  (bass-rate 1/4096)
  (mid-hi-rate 1/2048)
  (bass-wob-rate 1)
  (load-nk-bank :riffs :jon :bassmidhi1)
  (hi-amp 0)
  (mid-amp 0)
  (bass-amp 0))

(do
  (data-riff-load-mid-hi (flatten (concat (repeat 1000 [:c4 :c5 :c6]) [:d6 :c6 :d6 :c6] (repeat 0 [:d4]) [:c6] (repeat 20 [:C4 :E4 :G4 :E4 :C6 :E4 :G4 :E4])))-5)
  (data-riff-load-bass (flatten (concat (repeat 2000 [:c4 :c5 :c6]) [:d6 :c6 :d6 :c6] (repeat 0 [:d4]) [:c6] (repeat 20 [:C4 :E4 :G4 :E4 :C6 :E4 :G4 :E4])))-24)
  (bass-rate 1/102400)
  (mid-hi-rate 1/204800)
  (bass-wob-rate 0.1)
  (load-nk-bank :riffs :jon :bassmidhi)
  (hi-amp 0.1)
  (mid-amp 0.1)
  (bass-amp 0.1))

(do
  (hi-amp 0)
  (mid-amp 0)
  (bass-amp 0)
  )


(swap-samples-128 ambient-drum-samples)
(swap-samples-128 origshaker-samples)
(swap-samples-128 african-samples)
(swap-samples-128 mouth-samples)
(swap-samples-128 bleep-samples)
(swap-samples-128 bleep0-samples)
(swap-samples-128 bleep1-samples)
(swap-samples-128 bleep2-samples)
(swap-samples-128 bleep3-samples)
(swap-samples-128 bleep4-samples)
(swap-samples-128 bleep5-samples)
(swap-samples-128 clapkick1-samples)
(swap-samples-128 clapkick2-samples)
(swap-samples-128 kicks-samples)
(swap-samples-128 metaex-samples)
(swap-samples-128 beauty-samples)
(swap-samples-128 kickbleep-samples)
(swap-samples-128 subkicks-samples)
(swap-samples-128 mouthtrigger-samples)
(swap-samples-128 africantrigger-samples)
(swap-samples-128 spooky-samples)
(swap-samples-128 atmos-samples)



(swap-samples-64 ambient-drum-samples)
(swap-samples-64 orig-samples)
(swap-samples-64 transition-samples)
(swap-samples-64 african-samples)
(swap-samples-64 mouth-samples)
(swap-samples-64 m64-1-samples)
(swap-samples-64 metaexwoosh-samples)

(swap-trigs-128 ambient-drum-samples)
(swap-trigs-128 trigger-samples)
(swap-trigs-128 atmos-samples)
(swap-trigs-128 bleep3-samples)
(swap-trigs-128 metaexwoosh-samples)
(swap-trigs-128 beauty-samples)
(swap-trigs-128 kickbleeptrigger-samples)
(swap-trigs-128 spooky-samples)
(swap-trigs-128 mouthtrigger-samples)

(swap-trigs-64 trigger-samples)
(swap-trigs-64 trigger1-samples)
(swap-trigs-64 africantrigger-samples)

(load-nk-bank :m128 :jon :off)
(load-nk-bank :m128 :jon :safe)
(load-nk-bank :m128 :jon :orig)
(load-nk-bank :m128 :jon :bleeps)
(load-nk-bank :m128 :jon :darkorig)
(load-nk-bank :m128 :jon :ambientdrums)
(load-nk-bank :m128 :jon :african)
(load-nk-bank :m128 :jon :chilledafrican)
(load-nk-bank :m128 :jon :metaex)
(load-nk-bank :m128 :jon :beauty)
(load-nk-bank :m128 :jon :kickbleep)
(load-nk-bank :m128 :jon :subkick)

(save-nk-bank :m128 :jon :bells)


(load-nk-bank :m64 :jon :m64-1)

(save-nk-bank :m64 :jon :m64-1)

(save-nk-bank :riffs :jon :bassmidhi1)
(load-nk-bank :riffs :jon :bassmidhi)
(load-nk-bank :riffs :jon :off)


(data-riff-load-bass [:e4 :e3 :c4 :c3])

(data-riff-load-bass [:a4 :a3])
(data-riff-load-bass [:c4 :e3])
(data-riff-load-bass [:e4 :e3])
(data-riff-load-bass [:e5 :e4])

(data-riff-load-bass [:a4 :a3 :a2])
(data-riff-load-bass [:c4 :c3 :c2])
(data-riff-load-bass [:e4 :e3 :e2])

(do
  (bass-rate 1/512)

  (data-riff-load-bass [:e4 :e3])
  )
(defsynth foo [] (out 0 (sin-osc)))

(defonce f (foo))
(foo)
(kill 285)
(bass-wob-rate 0.5)
