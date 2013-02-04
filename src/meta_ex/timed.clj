(ns meta-ex.timed
  (:use [overtone.core]))

(defonce running-patterns* (atom #{}))

(defrecord ScheduledPattern [desc continue? fun]
  IKillable
  (kill* [this]
    (swap! disj running-patterns* this)
    (reset! (:continue? this) false)))

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
           current-val (atom true)]
       (apply
        (fn t-rec [t val val-inc end]
          (when (and
                 @cont?
                 (or (and (pos? diff)
                          (< val end))
                     (> val end)))
            (try
              (reset! current-val val)
              (f val)
              (catch Exception e
                (.printStackTrace e)))
            (apply-at  (+ t time-diff) t-rec [(+ t time-diff) (+ val val-inc) val-inc end])))
        [(now) start val-inc end])
       (ScheduledPattern. desc cont? f))))

(dissoc #{1 2 3} 3)

(use 'clojure.set)
(disj #{1 2 3} 3)
