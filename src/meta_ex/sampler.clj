(ns meta-ex.sampler
  (:use [overtone.live]
        [meta-ex.kit.timing]
        [meta-ex.kit.mixer])
  (:require [meta-ex.kit.timing :as tim] ))

(defsynth sample-resetter [sample-buf 0 bb-trigger 0 num-chans 1 amp 1 out-bus 0 pos-perc 0]
  (let [rate     (mouse-y 0.1 2)
        trigger  (in:kr bb-trigger)
        pos-perc (+ pos-perc (mouse-x 0 0.5))
        pos      (* pos-perc (buf-frames sample-buf))
        snd      (play-buf 1 sample-buf  rate trigger pos )]
    (out out-bus (* amp snd))))

(defsynth sample-resetter [sample-buf 0 bb-trigger 0 num-chans 1 amp 1 out-bus 0 pos-perc 0 attack 0.05]
  (let [rate     (mouse-y 0.1 2)
        trigger  (impulse 4)
        env      (env-gen (perc 0.05 :release 100000000) :gate trigger)
        pos-perc (+ pos-perc (mouse-x 0 0.5))
        pos      (* pos-perc (buf-frames sample-buf))
        snd      (* (play-buf 1 sample-buf  rate trigger pos )
                    env)]
    (out out-bus (* amp snd))))

(defonce transitional-whoosh (freesound 135566))

(defonce drumnbass (freesound 40106))

(def s (sample-resetter transitional-whoosh (:beat tim/beat-12th) :out-bus (nkmx :s1)))

(ctl s :bb-trigger (:beat tim/beat-32th) :pos-perc 0 :rate 1 :amp 0.3)

(kill hs)

(defonce hickey-he-man (sample "~/Dropbox/jon-n-sam/audio-files/hickey-he-man.wav"))
(defonce flute (freesound 35809))

(def h (hickey-he-man))




(def hs (sample-resetter hickey-he-man :amp 3))
(def fs (sample-resetter flute :amp 3))
(kill fs)

(kill sample-resetter)
(ctl hs :bb-trigger (:beat tim/beat-3th) :pos-perc 0.233 :rate 1 :amp 2)


(def ds (sample-resetter drumnbass (:beat tim/beat-1th) :out-bus (nkmx :s1)))
(ctl ds :bb-trigger (:beat tim/beat-2th) :pos-perc 0.233 :rate 1 :amp 0.5)
(stop)o
(kill ds)
(kill sample-resetter)




(def mono-flute (overtone.sc.buffer/buffer-as-mono flute))

(type mono-flute)

(overtone.sc.sample/mono-player mono-flute)


;; (
;; {
;; 	var b = 10, trate, dur, rate;
;; 	trate = MouseY.kr(2,200,1);
;; 	dur = 4 / trate;
;; 	rate = Dseq([10, 1, 1, 0.5, 0.5, 0.2, 0.1], inf);
;; 	TGrains.ar(2, Impulse.ar(trate), b, rate, MouseX.kr(0,BufDur.kr(b)), dur, Dseq([-1, 1], inf), 0.1, 2);
;; }.scope(zoom: 4);
;; )

(defsynth foograins [buf 0 grain-dur 0.1]
  (let [trate   (mouse-y 2 200 1)
        dur     (/ 4 trate)
        trigger (impulse:ar trate)
        pan     (demand  trigger 0 [(dseq [-0.95 0.5 0 -0.5 0.95] INF)])
        ]
    (out 0 (t-grains 2 trigger buf 1 (mouse-x 0 (buf-dur buf)) grain-dur pan))))

(foograins mono-flute)

(kill 101)

(def mono-hickey (overtone.sc.buffer/buffer-as-mono hickey-he-man))
(def mono-whoosh (overtone.sc.buffer/buffer-as-mono transitional-whoosh))
(def f (foograins mono-hickey))
(def f (foograins mono-flute))

(def f (foograins mono-whoosh))

(ctl f :grain-dur 0.1)

(kill f)
