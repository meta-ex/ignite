(ns meta-ex.mixer
  (:use [overtone.live])
  (:require [meta-ex.nk :as nk]))

(defcgen wobble
  "wobble an input src"
  [src {:doc "input source"}
   wobble-factor {:doc "num wobbles per second"}]
  (:ar
   (let [sweep (lin-exp (lf-tri wobble-factor) -1 1 40 3000)
         wob   (lpf src sweep)
         wob   (* 0.8  wob)
         wob   (+ wob (bpf wob 1500 2))]
     (+ wob (* 0.2 (g-verb wob 9 0.7 0.7))))))

(defsynth meta-mix [amp 1
                    pan 0
                    in-bus 10
                    rev-mix 0
                    delay-decay 0
                    dec-mix 0
                    wobble-mix 0
                    delay-mix 0
                    hpf-mix 0
                    wobble-factor 0
                    rev-damp 0
                    rev-room 0
                    samp-rate 0
                    bit-rate 0
                    delay-rate 0
                    hpf-freq 1000
                    hpf-rq 0
                    delay-reset-trig [0 :kr]
                    lpf-mix 0
                    lpf-freq 100
                    delay-buf 0
                    out-bus 0]
  (let [src       (in:ar in-bus 2)
        samps     (buf-frames:kr delay-buf)
        pos       (phasor:ar delay-reset-trig 1 0 (* delay-rate samps))
        old       (buf-rd:ar 1 delay-buf pos :loop true)
        delay-sig (+ src (* delay-decay old))

        src       (+ (* (- 1 delay-mix) src)
                     (* delay-mix delay-sig))

        src       (+ (* (- 1 rev-mix) src)
                     (* rev-mix (free-verb src :mix 1 :room rev-room :damp rev-damp)))

        src       (+ (* (- 1 dec-mix) src)
                     (* dec-mix (decimator src samp-rate bit-rate)))

        src       (+ (* (- 1 hpf-mix) src)
                     (* hpf-mix (normalizer (rhpf src hpf-freq hpf-rq))))

        src       (+ (* (- 1 lpf-mix) src)
                     (* lpf-mix (lpf src lpf-freq)))

        src       (+ (* (- 1 wobble-mix) src)
                     (* wobble-mix (wobble src wobble-factor)))

        ]

    (buf-wr:ar [(mix src)] delay-buf pos :loop true)
    (out out-bus (pan2 (* amp src) pan))))

(def nano2-key
  {0 :slider0
   1 :slider1
   2 :slider2
   3 :slider3
   4 :slider4
   5 :slider5
   6 :slider6
   7 :slider7

   16 :pot0
   17 :pot1
   18 :pot2
   19 :pot3
   20 :pot4
   21 :pot5
   22 :pot6
   23 :pot7})

(def nano2-fns {:slider0 (fn [v mixer-g] (ctl mixer-g :rev-mix (/ v 127)))
                :slider1 (fn [v mixer-g] (ctl mixer-g :delay-decay (/ v 127)))
                :slider2 (fn [v mixer-g] (ctl mixer-g :dec-mix (/ v 127)))
                :slider3 (fn [v mixer-g] (ctl mixer-g :wobble-mix (/ v 127)))
                :slider4 (fn [v mixer-g] (ctl mixer-g :delay-mix (/ v 127)))
                :slider5 (fn [v mixer-g] (ctl mixer-g :hpf-mix (/ v 127)))
                :slider6 (fn [v mixer-g] (ctl mixer-g :wobble-factor (scale-range v 0 127 0 15)))
                :slider7 (fn [v mixer-g] (ctl mixer-g :amp (scale-range v 0 127 0 3)))
                :pot7    (fn [v mixer-g] (ctl mixer-g :pan (scale-range v 0 127 -1 1)))

                :pot0    (fn [v mixer-g] (ctl mixer-g :rev-damp (/ v 127)))
                :pot1    (fn [v mixer-g] (ctl mixer-g :rev-room (/ v 127)))
                :pot2    (fn [v mixer-g] (ctl mixer-g :samp-rate (* 22000 (/ v 127))))
                :pot3    (fn [v mixer-g] (ctl mixer-g :bit-rate (* 32 (/ v 127))))
                :pot4    (fn [v mixer-g] (ctl mixer-g :delay-rate (/ v 127)))
                :pot5    (fn [v mixer-g] (ctl mixer-g :hpf-freq (+ 60 (* 2000 (/ v 127)))))
                :pot6    (fn [v mixer-g] (ctl mixer-g :hpf-rq (/ v 127)))})

(defn mk-mixer [nk-rcv]
  (let [midi-device (:out nk-rcv)
        bufff       (buffer (* 2 44100))
        in-bus      (audio-bus 2)
        mixer-g     (group "m-x-synths" :tgt (foundation-safe-post-default-group))
        mixer       (meta-mix :target mixer-g :in-bus in-bus :delay-buf bufff)
        event-key   (str "m-x mixer - "(midi-mk-full-device-key midi-device))]
    (on-latest-event (midi-mk-full-device-event-key midi-device :control-change)
                     (fn [msg]
                       (let [note (:note msg)
                             val  (:data2 msg)]
                         (if-let [f (get nano2-fns (nano2-key note))]
                           (f val mixer-g)
                           (println "unbound: " note))))
                     event-key)
    {:bufff     bufff
     :mixer-g   mixer-g
     :mixer     mixer
     :event-key event-key
     :in-bus in-bus}))

(def korg-nano-kontrol-mixers
  (doall (map mk-mixer nk/nano-k-devs)))

(defn mx
  "Returns the group of the mixer at idx. Tries to be smart when idx is
   out of range, resorting to out-bus = 0 where necessary."
  ([] (mx 0))
  ([idx]
     (cond
      (= 0 (count korg-nano-kontrol-mixers)) 0
      (< idx 0) (mx 0)
      (>= idx (count korg-nano-kontrol-mixers)) (:in-bus (last korg-nano-kontrol-mixers))
      :else (:in-bus (nth korg-nano-kontrol-mixers idx)))))
