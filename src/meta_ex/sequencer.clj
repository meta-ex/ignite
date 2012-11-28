(ns meta-ex.sequencer
  (:use [overtone.core]
        )
    (:require [polynome.core :as poly]
              [clojure.data :as data]
              [meta-ex.quilome]))

;; A fully server-side sample sequencer.
;; =====================================

;; This example demonstrates some of the benefits of moving all synth
;; triggers inside the server itself. For example, it allows you to
;; modify the synthesis with *immediate* effect (rather than waiting for
;; the next bar/chunk to be scheduled) and you can use a global pulse to
;; drive both the timing and to also modulate aspects of the synthesis
;; so that the modulations are sympathetic to the rhythms being played.


;; First, let's create some sequencer buffers for specifying which beat
;; to trigger a sample. This will be our core data structure for a basic
;; emulation of an 8-step sequencer. A buffer is like a Clojure vector,
;; except it lives on the server and may only contain floats. Buffers
;; are initialised to have all values be 0.0
(defonce buf-0 (buffer 8))
(defonce buf-1 (buffer 8))
(defonce buf-2 (buffer 8))
(defonce buf-3 (buffer 8))
(defonce buf-4 (buffer 8))
(defonce buf-5 (buffer 8))
(defonce buf-6 (buffer 8))

;; Next let's create some timing busses. These can be visualised as
;; 'patch cables' - wires that carry pulse signals that may be
;; arbitrarily forked and fed into any synth that wants to be aware of
;; the pulses. We have two types of information being conveyed here -
;; firstly the trg busses contain a stream of 0s with an intermittant 1
;; every time there is a tick. Secondly we have the cnt busses which
;; contain a stream of the current tick count. We then have two of each
;; type of bus - one for a high resolution global metronome, and another
;; for a division of the global metronome for our beats.
(defonce root-trg-bus (control-bus)) ;; global metronome pulse
(defonce root-cnt-bus (control-bus)) ;; global metronome count
(defonce beat-trg-bus (control-bus)) ;; beat pulse (fraction of root)
(defonce beat-cnt-bus (control-bus)) ;; beat count

(def BEAT-FRACTION "Number of global pulses per beat" 30)

;; Here we design synths that will drive our pulse busses.
(defsynth root-trg [rate 100]
  (out:kr root-trg-bus (impulse:kr rate)))

(defsynth root-cnt []
  (out:kr root-cnt-bus (pulse-count:kr (in:kr root-trg-bus))))

(defsynth beat-trg [div BEAT-FRACTION]
  (out:kr beat-trg-bus (pulse-divider (in:kr root-trg-bus) div))  )

(defsynth beat-cnt []
  (out:kr beat-cnt-bus (pulse-count (in:kr beat-trg-bus))))

;; Now we get a little close to the sounds. Here's four nice sounding
;; samples from Freesound.org
(def kick-s (sample (freesound-path 777)))
(def click-s (sample (freesound-path 406)))
(def boom-s (sample (freesound-path 33637)))
(def subby-s (sample (freesound-path 25649)))
(def cym (sample (freesound-path 436)) )
(def crash (sample (freesound-path 45102)))

;; Here's a synth for playing back the samples with a bit of modulation
;; to keep things interesting.
(defsynth mono-sequencer
  "Plays a single channel audio buffer."
  [buf 0 rate 1 out-bus 0 beat-num 0 sequencer 0 amp 2]
  (let [cnt      (in:kr beat-cnt-bus)
        beat-trg (in:kr beat-trg-bus)
        bar-trg  (and (buf-rd:kr 1 sequencer cnt)
                      (= beat-num (mod cnt 8))
                      beat-trg)
        vol      (set-reset-ff bar-trg)]
    (out
     out-bus (* vol
                amp
                (pan2
                 (rlpf
                  (scaled-play-buf 1 buf rate bar-trg)
                  (demand bar-trg 0 (dbrown 200 20000 50 INF))
                  (lin-lin:kr (lf-tri:kr 0.01) -1 1 0.1 0.9)))))))

;; Here's Dan Stowell's dubstep synth modified to work with the global
;; pulses
(defsynth dubstep [note 40 wobble BEAT-FRACTION hi-man 0 lo-man 0 sweep-man 0 deci-man 0 tan-man 0 shape 0 sweep-max-freq 3000 hi-man-max 1000 lo-man-max 500 beat-vol 0 lag-delay 0.5 amp 1 out-bus 0]
  (let [bpm     300
        wob     (pulse-divider (in:kr root-trg-bus) wobble)
        sweep   (lin-lin:kr (lag-ud wob 0.01 lag-delay) 0 1 400 sweep-max-freq)
        snd     (mix (saw (* (midicps note) [0.99 1.01])))
        snd     (lpf snd sweep)
        snd     (normalizer snd)

        snd     (bpf snd 1500 2)
        ;;special flavours
        ;;hi manster
        snd     (select (> hi-man 0.05) [snd (* 4 (hpf snd hi-man-max))])

        ;;sweep manster
        snd     (select (> sweep-man 0.05) [snd (* 4 (hpf snd sweep))])

        ;;lo manster
        snd     (select (> lo-man 0.05) [snd (lpf snd lo-man-max)])

        ;;decimate
        snd     (select (> deci-man 0.05) [snd (round snd 0.1)])

        ;;crunch
        snd     (select (> tan-man 0.05) [snd (tanh (* snd 5))])

        snd     (* 0.5 (+ (* 0.8 snd) (* 0.3 (g-verb snd 100 0.7 0.7))))
        ]
    (out out-bus (pan2 (* amp (normalizer snd))))))

;; Here's a nice supersaw synth
(definst supersaw2 [freq 440 amp 2.5 fil-mul 2 rq 0.3]
  (let [input  (lf-saw freq)
        shift1 (lf-saw 4)
        shift2 (lf-saw 7)
        shift3 (lf-saw 5)
        shift4 (lf-saw 2)
        comp1  (> input shift1)
        comp2  (> input shift2)
        comp3  (> input shift3)
        comp4  (> input shift4)
        output (+ (- input comp1)
                  (- input comp2)
                  (- input comp3)
                  (- input comp4))
        output (- output input)
        output (leak-dc:ar (* output 0.25))
        output (normalizer (rlpf output (* freq fil-mul) rq))]

    (* amp output (line 1 0 10 FREE))))


;; OK, let's make some noise!

;; Now, let's start up all the synths:
(defn start-synths []
  (let [r-cnt  (root-cnt)
        b-cnt   (beat-cnt)
        b-trg   (beat-trg)
        r-trg   (root-trg)
        kicks   (doall
                 (for [x (range 8)]
                   (mono-sequencer :buf kick-s :beat-num x :sequencer buf-0)))
        clicks  (doall
                 (for [x (range 8)]
                   (mono-sequencer :buf click-s :beat-num x :sequencer buf-1 :amp 1)))
        booms   (doall
                 (for [x (range 8)]
                   (mono-sequencer :buf boom-s :beat-num x :sequencer buf-2)))

        subbies (doall
                 (for [x (range 8)]
                   (mono-sequencer :buf subby-s :beat-num x :sequencer buf-3)))

        cyms (doall
                 (for [x (range 8)]
                   (mono-sequencer :buf cym :beat-num x :sequencer buf-4 :amp 3)))

        crashes (doall
                 (for [x (range 8)]
                   (mono-sequencer :buf crash :beat-num x :sequencer buf-5 :amp 1)))]
    {:r-cnt r-cnt
     :b-cnt b-cnt
     :r-trg r-trg
     :b-trg b-trg
     :kicks kicks
     :clicks clicks
     :booms booms
     :subbies subbies
     :cyms cyms
     :crashes crashes}))

(defn start-system
  []
  (meta-ex.quilome/clear suse.quilome/global-monome)
  (buffer-write! buf-0 [0 0 0 0 0 0 0 0])
  (buffer-write! buf-1 [0 0 0 0 0 0 0 0])
  (buffer-write! buf-2 [0 0 0 0 0 0 0 0])
  (buffer-write! buf-3 [0 0 0 0 0 0 0 0])
  (buffer-write! buf-4 [0 0 0 0 0 0 0 0])
  (buffer-write! buf-5 [0 0 0 0 0 0 0 0])
  (buffer-write! buf-6 [0 0 0 0 0 0 0 0])
  (poly/clear meta-ex.monome-event/m)
(def ssaw-rq 0.3)
(def ssaw-fil-mul 4)
  (def synths (start-synths))
   (do

    (do

      (defn coord-7-7
        [v]
        (ctl dubstep :deci-man v))

      (defn coord-7-6
        [v]
        (ctl dubstep :lo-man v))

      (defn coord-7-5
        [v]
        (ctl dubstep :hi-man v))

      (defn coord-7-4
        [v]
        (ctl dubstep :lo-man v))


      (defn coord-7-3
        [v]
        (when (= 1 v)
          (ctl dubstep :note 40)))

      (defn coord-7-2
        [v]
        (when (= 1 v)
          (ctl dubstep :note 36)))

      (defn coord-7-1
        [v]
        (when (= 1 v)
          (ctl dubstep :note 33)))

      (defn coord-7-0
        [v]
        (when (= 1 v )
          (ctl dubstep :note 28)))

      (defn row-0-updated
        [r]
        (buffer-write! buf-0 r))

      (defn row-1-updated
        [r]
        (buffer-write! buf-1 r))

      (defn row-2-updated
        [r]
        (buffer-write! buf-2 r))

      (defn row-3-updated
        [r]
        (buffer-write! buf-3 r))

      (defn row-4-updated
        [r]
        (buffer-write! buf-4 r))

      (defn row-5-updated
        [r]
        (buffer-write! buf-5 r))

      (defn row-6-updated
        [r]
        (buffer-write! buf-6 r))

      (defn get-row [grid idx]
        (reverse (for [i (range 8)]
                   (get grid [idx i]  0))))

      (defn monome-updated [grid]
        (let [row0 (get-row grid 0)
              row1 (get-row grid 1)
              row2 (get-row grid 2)
              row3 (get-row grid 3)
              row4 (get-row grid 4)
              row5 (get-row grid 5)
              row6 (get-row grid 6)]
          (row-0-updated row0)
          (row-1-updated row1)
          (row-2-updated row2)
          (row-3-updated row3)
          (row-4-updated row4)
          (row-5-updated row5)
          (row-6-updated row6)

          ;; (coord-7-7 (get grid [7 7] 0))
          ;; (coord-7-6 (get grid [7 6] 0))
          ;; (coord-7-5 (get grid [7 5] 0))
          ;; (coord-7-4 (get grid [7 4] 0))
          ;; (coord-7-3 (get grid [7 3] 0))
          ;; (coord-7-2 (get grid [7 2] 0) )
          ;; (coord-7-1 (get grid [7 1] 0))
          ;; (coord-7-0 (get grid [7 0] 0))


          ))

      )


;    (defonce m (poly/init "/dev/tty.usbserial-m64-0790")
      )


    (def leds* (atom {}))

    (add-watch leds* ::monome-updated (fn [k r o n]
                                        (#'monome-updated n)))
    ;;    (poly/remove-all-callbacks m)

    (on-event [:monome :press]
              (fn [{:keys [x y]}]
                (swap! leds* (fn [prev-grid]
                               (let [prev-val (get prev-grid [x y] 0)]
                                 (assoc prev-grid [x y] (mod (inc prev-val) 2))))))
              :monome-press)

    (on-event [:monome :press]
              (fn [{:keys [x y monome]}]
                (poly/toggle-led monome x y)
                (println (keys monome)))
              :monome-led))


(defn set-ssaw-rq [v]
  (def ssaw-rq v))

(defn set-ssaw-fil-mul [v]
  (def ssaw-fil-mul v))

;;(poly/disconnect m)
