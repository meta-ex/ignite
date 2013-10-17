(ns meta-ex.lib.timed
  (:use [overtone.helpers.ref])
  (:require [overtone.music.time :as time]
            [overtone.sc.protocols :as protocols]))

(defonce running-patterns* (atom #{}))

;;(kill-all-running-patterns)

(defn kill-all-running-patterns
  []
  (let [[ops _] (reset-returning-prev! running-patterns* #{})]
    (doseq [p ops]
      (protocols/kill* p))))

;;(kill-all-running-patterns)

(defprotocol IDelaySet
  (delay-set! [this t] "Set the delay for a scheduled fn to t ms"))

(defprotocol ILive
  (live? [this] "Returns true if this object is live"))

;; This would be nice!
;; (defprotocol IPausable
;;   (pause [this] "Pause this object")
;;   (start [this] "Start this object"))

(defrecord ScheduledTimedRange [desc continue? fun kill-sync-prom]
  protocols/IKillable
  (kill* [this]
    (swap! running-patterns* disj this)
    (reset! (:continue? this) false)
    @kill-sync-prom
    this)
  ILive
  (live? [this] @(:continue? this)))

(defrecord ScheduledPattern [desc continue? delay kill-sync-prom]
  protocols/IKillable
  (kill* [this]
    (swap! running-patterns* disj this)
    (reset! (:continue? this) false)
    @kill-sync-prom
    this)
  IDelaySet
  (delay-set! [this t]
    (reset! (:delay this) t)
    this)
  ILive
  (live? [this] @(:continue? this)))

(defn timed-range
  "Linear range through time. Schedules f to be called frequently with a
   single argument which is a linear transition of vals between start
   and end taking time seconds to make the full transition.

   Resolution of calls to f can be modified with the optional argument
   resolution. The default is 10 hz (i.e. 10 calls of f per second). It
   is important to ensure that f takes less than (/ resolution 1000) ms
   to execute.

   Catches any exceptions created by f and prints the stacktrace to
   *out* before continuing execution of pattern.

   Returns a ScheduledPattern record which may be killed with the kill
   fn."
  ([f start end time] (timed-range f start end time 10 "Timed Range"))
  ([f start end time resolution-or-desc]
     (if (string? resolution-or-desc)
       (timed-range f start end time 10 resolution-or-desc)
       (timed-range f start end time resolution-or-desc "Timed Range")))
  ([f start end time resolution desc]
     (let [time-diff   (/ 1000 resolution)
           diff        (- end start)
           val-inc     (/ diff (/ (* time 1000) time-diff))
           cont?       (atom true)
           current-val (atom true)
           kill-sync   (promise)
           pattern     (ScheduledTimedRange. desc cont? f kill-sync)]
       (swap! running-patterns* conj pattern)
       (apply
        (fn t-rec [t val val-inc end]
          (if (and
               @cont?
               (or (and (pos? diff)
                        (< val end))
                   (> val end)))
            (do (try
                  (reset! current-val val)
                  (f val)
                  (catch Exception e
                    (.printStackTrace e)))
                (time/apply-at  (+ t time-diff) t-rec [(+ t time-diff) (+ val val-inc) val-inc end ]))
            (deliver kill-sync true)))
        [(time/now) start val-inc end])
       pattern)))

(defn temporal
  ([f delay] (temporal f delay []))
  ([f delay args] (temporal f delay args "Temporal fn"))
  ([f delay args desc]
     (let [cont?    (atom true)
           delay-a  (atom delay)
           t        (time/now)
           kill-sync (promise)
           pattern  (ScheduledPattern. desc cont? delay-a kill-sync)
           recur-fn (fn rf [t & args]
                      (if @cont?
                        (let [res (apply f args)
                              res (if (sequential? res) res [])]
                          (let [d  @delay-a
                                nt (+ d t)]
                            (time/apply-at nt rf nt args)))
                        (deliver kill-sync true)))]
       (time/apply-at (+ t delay) recur-fn (+ t delay) args)
       (swap! running-patterns* conj pattern)
       pattern)))
