(ns meta-ex.sequencer
  (:use [overtone.core]))

(defsynth mono-sequencer
  "Plays a single channel audio buffer."
  [buf 0 rate 1 out-bus 0 beat-num 0 pattern 0 amp 2 num-steps 8 beat-cnt-bus 0 beat-trg-bus 0]
  (let [cnt      (in:kr beat-cnt-bus)
        beat-trg (in:kr beat-trg-bus)
        bar-trg  (and (buf-rd:kr 1 pattern cnt)
                      (= beat-num (mod cnt num-steps))
                      beat-trg)
        vol      (set-reset-ff bar-trg)]
    (out out-bus (* vol
                    amp
                    (pan2
                     (rlpf
                      (scaled-play-buf 1 buf rate bar-trg)
                      (demand bar-trg 0 (dbrown 200 20000 50 INF))
                      (lin-lin:kr (lf-tri:kr 0.01) -1 1 0.1 0.9)))))))

(defn- mk-sequencer-samples
  [samples num-steps]
  (map (fn [s] (with-meta {:num-steps num-steps
                          :pattern   (buffer num-steps)
                          :sample    s}
                {:type ::sequencer-sample}))
       samples))

(defn- start-synths [samples num-steps tgt-group beat-cnt-bus beat-trg-bus]
  (doall (map (fn [sample]
                (doseq [x (range num-steps)]
                  (mono-sequencer :tgt tgt-group
                                  :buf (:sample sample)
                                  :beat-num x
                                  :pattern (:pattern sample)
                                  :beat-cnt-bus beat-cnt-bus
                                  :beat-trg-bus beat-trg-bus)))
              samples)))

(defn mk-sequencer
  "Creates a sequencer that resides at the tail of the target group with
   synths to play each sample with the specified number of steps using
   clock busses"
  ([samples num-steps tgt-group beat-trg-bus beat-cnt-bus]
     (mk-sequencer samples num-steps tgt-group beat-trg-bus beat-cnt-bus ""))
  ([samples num-steps tgt-group beat-trg-bus beat-cnt-bus desc]
     (let [samples   (mk-sequencer-samples samples num-steps)
           desc      (str "M-x Sequencer " desc)
           seq-group (group desc :tail tgt-group)
           synths    (start-synths samples num-steps seq-group beat-cnt-bus beat-trg-bus)]
       (with-meta {:beat-trg-bus beat-trg-bus
                   :beat-cnt-bus beat-cnt-bus
                   :samples      samples
                   :synths       synths
                   :num-steps    num-steps
                   :group        seq-group
                   :desc         desc}
         {:type ::sequencer}))))

(defn sequencer-write!
  [sequencer idx pattern]
  (let [buf (:pattern (nth (:samples sequencer) idx))]
    (buffer-write! buf pattern)))

(defn sequencer-pattern
  "Returns the current state of the sequencer pattern with index idx"
  [sequencer idx]
  (let [buf (:pattern (nth (:samples sequencer) idx))]
    (seq (buffer-data buf))))

(defn sequencer-patterns
  "Returns a sequence of the current state of all the patterns in
   sequencer"
  [sequencer]
  (doall (map (fn [i] (sequencer-pattern sequencer i)) (range (:num-steps sequencer)))))

(defn sequencer-restart
  [s]
  (kill (:group s))
  (doseq [samp (:samples s)]
    (buffer-write! (:buf samp) (repeat (:num-steps s) 0)))
  (assoc s :synths (start-synths (:samples s) (:num-steps s) (:group s))))

(defn sequencer-pause
  [s]
  (node-pause (:group s)))

(defn sequencer-play
  [s]
  (node-start (:group s)))

(defn sequencer-kill
  [s]
  (kill (:group s)))
