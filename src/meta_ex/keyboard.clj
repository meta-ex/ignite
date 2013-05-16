(ns meta-ex.keyboard
  (:use [overtone.live]
        [overtone.synth.sampled-piano]
        [meta-ex.kit.mixer]
        [overtone.samples.piano :only [index-buffer]])
  (:require [meta-ex.kit.timing :as tim]
            [meta-ex.synths.synths :as syn]))

(do

  (defonce wob-b (control-bus "Piano Wob"))
  (defonce sin-x-s (tim/sin-x tim/pi-x-b wob-b ) ))

(defsynth sampled-piano2
  [note 60 level 1 rate 1 loop? 0
   attack 0 decay 1 sustain 1 release 0.1 curve -4 gate 1 out-bus 0 amp 1]
  (let [buf (index:kr (:id index-buffer) note)
        env (env-gen (adsr attack decay sustain release level curve)
                     :gate gate
                     :action FREE)]
    (out out-bus (* 0.5 (* (+ 2 (* 1 (in:kr wob-b))) env amp (scaled-play-buf 2 buf :level level :loop loop? :action FREE))))))

(ctl sin-x-s :mul 2)

(doseq [c (chord :D3 :minor)]
  (sampled-piano2 c 1 :out-bus (nkmx :r0)))


(on-event [:midi-device "KORG INC." "KEYBOARD" "nanoKEY2 KEYBOARD" 0 :note-on]
          (fn [msg]
;;            (sampled-piano2 (:note msg) :out-bus (nkmx :r0) :amp 1)
            )
          ::keyboard)




(defonce cs (syn/cs80 :out-bus (nkmx :s1) :freq (midi->hz (note :g1))))

(ctl cs :out-bus (nkmx :s1) :freq (midi->hz (note :g1)))
(kill cs)


(on-event [:midi :note-on]
          (fn [msg]
            (let [note (- (:note msg ) 12)]
              (ctl cs :freq (midi->hz note)))

            )
          ::control-cs)