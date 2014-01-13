(ns meta-ex.kit.timing2
  (:use [overtone.live]
        [overtone.synth.timing]
        [overtone.studio.scope])
  (:require [clj-time.coerce :as tc]
            [overtone.sc.defaults :as defaults]))

(defonce start-time (atom nil))

;; should only be modified by a 'singleton' wall-clock synth
;; global shared state FTW!
(defonce wall-clock-b (control-bus 2 "Timing Tick Buses"))

;; Only one of these should ever be created...
(defsynth wall-clock [tick-bus-2c 0]
  (let [[b-tick s-tick] (in:kr tick-bus-2c 2)
        maxed?          (= defaults/SC-MAX-FLOAT-VAL s-tick)
        small-tick      (select:kr maxed?
                                   [(+ s-tick 1)
                                    0])
        big-tick        (pulse-count maxed?)]
    (replace-out:kr tick-bus-2c [big-tick small-tick])))

(defn reset-wall-clock-busses
  []
  (control-bus-set-range! wall-clock-b [0 0]))

(defn start-wall-clock
  []
  (reset-wall-clock-busses)
  (let [start-t (+ (now) 500)]
    (reset! start-time start-t)
    (def ss-t start-t)
    (at start-t (wall-clock wall-clock-b))))

(defn server-time
  []
  (let [[b-t s-t] (control-bus-get-range wall-clock-b 2)
        n-ticks   (+ (* b-t defaults/SC-MAX-FLOAT-VAL)
                     s-t)]
    (+ ss-t (* 1000 n-ticks (server-control-dur)))))

(start-wall-clock )

(time (- (server-time) (now)))

(time @start-time)

(time (server-time))

(kill wall-clock)

(defonce result-b (control-bus 2))

(defsynth report-time [res-b 0]
  (let [t (in:kr )]))

(let [new-t (+ (now) 500)]
  (def result-t new-t)
  ())
