(ns meta-ex.rhythm
   (:use [overtone.live]
         [meta-ex.sets.ignite]
         [meta-ex.kit.mixer]
         [meta-ex.synths.synths]
         [meta-ex.giorgio])
   (:require [meta-ex.kit.timing :as tim]))


(defonce rhythm-bass-g (group "Rhythm Bass"))

(defonce bass-note (atom 30))

(defonce rhythm-g (group "Rhythm" :after tim/timing-g))

(defonce saw-bf1 (buffer 256))
(defonce saw-bf2 (buffer 256 "TIM Saw Buffer 2"))

;;  (defcbus saw-x-b1 1 "Tim Saw")

(defonce saw-x-b1 (control-bus 1 "TIM Saw"))
(defonce saw-x-b2 (control-bus 1 "TIM Saw2"))
(defonce saw-x-b3 (control-bus 1 "TIM Saw3"))

(defonce phasor-b1 (control-bus 1 "TIM Saw Phsr"))
(defonce phasor-b2 (control-bus 1 "TIM Saw Phsr 2"))

(defonce saw-s1 (tim/saw-x [:head rhythm-g] :out-bus saw-x-b1))
(defonce saw-s2 (tim/saw-x [:head rhythm-g] :out-bus saw-x-b2))
(defonce saw-s3 (tim/saw-x [:head rhythm-g] :out-bus saw-x-b3))

(defonce phasor-s1 (tim/buf-phasor [:after saw-s1] saw-x-b1 :out-bus phasor-b1 :buf saw-bf1))
(defonce phasor-s2 (tim/buf-phasor [:after saw-s2] saw-x-b2 :out-bus phasor-b2 :buf saw-bf2))


(defsynth foo [attack 0.01 sustain 0.03 release 0.1 amp 0.8 out-bus 0 ]
  (let [freq (/ (in:kr phasor-b2) 2)
        env  (env-gen (lin-env attack sustain release) 1 1 0 1)
        src  (mix (saw [freq (* 1.01 freq)]))
        src  (lpf src (mouse-y 100 20000))
        sin  (sin-osc (* 1 freq))
        sin2 (sin-osc freq)
        src  (mix [src sin sin2])]
    (out out-bus (pan2 (* src amp)))))

(defsynth beepy [amp 1 out-bus 0]
  (let [freq   (* (in:kr phasor-b2) 1 )
        ct-saw (+ (lin-lin (in:kr saw-x-b3) 0 1 0.5 1))]
    (out out-bus (* 0.5  ct-saw amp 1.25 (mix (+ (lf-tri [(* 0.5 freq)
                                                          (* 0.25 freq)
                                                          (* 0.5 freq)
                                                          (* 2.01 freq)])
                                                 ;;                                                 (square (* 1/8 freq) )
                                                 ))
                    ))))

(defsynth foo-bass [lpf-f 1000 lpf-mul 1 amp 0 out-bus 0]
  (let [freq  (/ (in:kr phasor-b1) 8)
        ct-saw           (in:kr saw-x-b3)]
    (out out-bus (* amp (* 0.5 (* (+ 0.2 ct-saw) (lpf (sum [
                                                            (sin-osc (/ freq))
                                                            (sin-osc (/ freq 0.25))
                                                            (square (* 2 freq))
                                                            (saw freq)
                                                            ])
                                                      (* lpf-mul ct-saw lpf-f))))))))

(defn set-saw-s1
  "bass-rate"
  [rate]
  (ctl saw-s1 :freq-mul rate))

(defn set-saw-s2
  "mid-hi-rate"
  [rate]
  (ctl saw-s2 :freq-mul rate))

(defn set-saw-s3
  "bass-wob-rate"
  [rate]
  (ctl saw-s3 :freq-mul rate))

(defn data-riff-load
  [notes shift bf]
  (buffer-write! bf (map midi->hz
                         (map (fn [midi-note] (+ shift midi-note))
                              (take 256 (cycle (map note notes))))))  )

(defn data-riff-load-bf1
  ([notes] (data-riff-load-bf1 notes 0))
  ([notes shift]
     (data-riff-load notes shift saw-bf1)))

(defn data-riff-load-bf2
  ([notes] (data-riff-load-bf2 notes 0))
  ([notes shift]
     (data-riff-load notes shift saw-bf2)))


;;[:D3 :D0 :D3 :C3 :C3 :C5 :C4 :D4]

(buffer-write! saw-bf2 (map midi->hz
                            (map (fn [midi-note] (+ 0 midi-note))
                                 (map note [:C3 :E4 :C6 :D4 :F6 :E5 :D5 :D3 :C3 :C4 :E3 :D4 :C4 :E4 :D5 :D5]))))

(buffer-write! saw-bf1 (map midi->hz
                           (map (fn [midi-note] (+ 0 midi-note))
                                (map note (repeat 8 :c5)))))

(buffer-write! saw-bf2 (map midi->hz
                           (map (fn [midi-note] (+ -24 midi-note))
                                (map note (repeat 16 :d5)))))


(defn map-keyboard-on
  []
  (on-event [:midi :note-on]
            (fn [m]
              (buffer-write! saw-bf1 (map midi->hz
                                          (map (fn [midi-note] (+ -12 midi-note))
                                               (repeat 256 (:note m))))))
            ::phat-bass-keyboard))

(defn map-keyboard-off
  []
  (remove-event-handler ::phat-bass-keyboard))

(defn- set-bass!
  [notes]
  (reset! bass-note (- (first notes) 12)))

(defn- modify-bufs
  [bufs vals]
  (set-bass! vals)
  (doseq [b bufs]
    (buffer-write! b vals )))

(defn giorgio [idx]
  (modify-bufs
   [saw-bf2 ]
   (map midi->hz (flatten (repeat 16 (take 16 (drop (* idx 16) meta-ex.giorgio/score))))))
  (buffer-write! saw-bf1 (map midi->hz
                              (map (fn [midi-note] (+ -12 midi-note))
                                   (repeat 256 (hz->midi @bass-note))))) )

(defonce hi   (beepy    [:head rhythm-bass-g] :amp 0 :out-bus (nkmx 8 :s0)))
(defonce mid  (foo      [:head rhythm-bass-g] :amp 0 :out-bus (nkmx 8 :s1)))
(defonce bass (foo-bass [:head rhythm-bass-g] :amp 0 :out-bus (nkmx 8 :m0)))


(defn hi-amp   [amp] (ctl hi   :amp amp))
(defn mid-amp  [amp] (ctl mid  :amp amp))
(defn bass-amp [amp] (ctl bass :amp amp))
