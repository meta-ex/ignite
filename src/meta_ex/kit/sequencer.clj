(ns meta-ex.kit.sequencer
  (:use [overtone.core]
        [meta-ex.kit.mixer]))

(defsynth orig-mono-sequencer
  "Plays a single channel audio buffer."
  [buf 0 rate 1 out-bus 0 beat-num 0 pattern 0  num-steps 8 beat-cnt-bus 0 beat-trg-bus 0 rq-bus 0]
  (let [cnt      (in:kr beat-cnt-bus)
        beat-trg (in:kr beat-trg-bus)
        bar-trg  (and (buf-rd:kr 1 pattern cnt)
                      (= beat-num (mod cnt num-steps))
                      beat-trg)
        vol      (set-reset-ff bar-trg)]
    (out out-bus (* vol
                    (pan2
                     (rlpf
                      (scaled-play-buf 1 buf rate bar-trg)
                      (demand bar-trg 0 (dbrown 200 20000 50 INF))
                      (lin-lin:kr (lf-tri:kr 0.01) -1 1 0.1 0.9)))))))

(defsynth mono-sequencer
  "Plays a single channel audio buffer."
  [buf 0 rate 1 out-bus 0 beat-num 0 pattern 0  num-steps 8 beat-cnt-bus 0 beat-trg-bus 0 rq-bus 0]
  (let [cnt      (in:kr beat-cnt-bus)
        beat-trg (in:kr beat-trg-bus)
        bar-trg  (and (buf-rd:kr 1 pattern cnt)
                      (= beat-num (mod cnt num-steps))
                      beat-trg)
        vol      (set-reset-ff bar-trg)]
    (out out-bus (* vol (scaled-play-buf 1 buf rate bar-trg)))))

(defn- mk-sequencer-patterns
  [samples num-steps]
  (doall (map (fn [s] (with-meta {:num-steps   num-steps
                                 :pattern-buf (buffer num-steps)}
                       {:type ::sequencer-pattern}))
              samples)))

(defn- start-synths [samples patterns mixers num-steps tgt-group beat-cnt-bus beat-trg-bus out-bus]
  (let [out-busses (if mixers
                     (map :in-bus mixers)
                     (repeat out-bus))]
    (doall (mapcat (fn [sample pattern out-bus]
                     (map (fn [step-idx]
                            (mono-sequencer [:tail tgt-group]
                                            :buf (to-sc-id sample)
                                            :beat-num step-idx
                                            :pattern (:pattern-buf pattern)
                                            :beat-cnt-bus beat-cnt-bus
                                            :beat-trg-bus beat-trg-bus
                                            :out-bus out-bus))
                          (range num-steps)))
                samples
                patterns
                out-busses))))

(defn mk-sequencer
  "Creates a sequencer that resides at the tail of the target group with
   synths to play each sample with the specified number of steps using
   clock busses"
  ([nk-group handle samples num-steps tgt-group beat-trg-bus beat-cnt-bus out-bus]
     (mk-sequencer nk-group handle samples num-steps tgt-group beat-trg-bus beat-cnt-bus out-bus true))
  ([nk-group handle samples num-steps tgt-group beat-trg-bus beat-cnt-bus out-bus with-mixers?]
     (let [desc            (str "M-x Sequencer " handle)
           num-samps       (count samples)
           container-group (group handle :tail tgt-group)
           seq-group       (group "m-x-sequencer" :head container-group)
           mixer-group     (group "m-x-mixers" :after seq-group)
           patterns        (mk-sequencer-patterns samples num-steps)
           mixer-handles   (map #(str handle "-" %) (range num-samps))
           mixers          (when with-mixers?
                             (doall (map #(add-nk-mixer nk-group % mixer-group out-bus) mixer-handles)))
           synths          (start-synths samples patterns mixers num-steps seq-group beat-cnt-bus beat-trg-bus out-bus)]
       (with-meta {:num-samps     num-samps
                   :beat-trg-bus  beat-trg-bus
                   :beat-cnt-bus  beat-cnt-bus
                   :patterns      patterns
                   :synths        (agent synths)
                   :num-steps     num-steps
                   :group         container-group
                   :seq-group     seq-group
                   :mixer-group   mixer-group
                   :desc          desc
                   :mixer-handles mixer-handles
                   :mixers        mixers
                   :out-bus       out-bus
                   :with-mixers?  with-mixers?
                   :tgt-group     tgt-group}
         {:type ::sequencer}))))

(defn swap-samples! [sequencer samples]
  (send (:synths sequencer)
        (fn [synths]
          (kill (:seq-group sequencer))
          (start-synths (take (:num-samps sequencer) samples)
                        (:patterns sequencer)
                        (:mixers sequencer)
                        (:num-steps sequencer)
                        (:seq-group sequencer)
                        (:beat-cnt-bus sequencer)
                        (:beat-trg-bus sequencer)
                        (:out-bus sequencer)))))

(defn sequencer-write!
  [sequencer idx pattern]
  (let [buf (:pattern-buf (nth (:patterns sequencer) idx))]
    (buffer-write! buf pattern)))

(defn sequencer-pattern
  "Returns the current state of the sequencer pattern with index idx"
  [sequencer idx]
  (let [buf (:pattern-buf (nth (:patterns sequencer) idx))]
    (seq (buffer-data buf))))

(defn sequencer-patterns
  "Returns a sequence of the current state of all the patterns in
   sequencer"
  [sequencer]
  (doall (map (fn [i] (sequencer-pattern sequencer i)) (range (:num-steps sequencer)))))

(defn sequencer-pause
  [s]
  (node-pause (:group s)))

(defn sequencer-play
  [s]
  (node-start (:group s)))

(defn sequencer-kill
  [s]
  (group-free (:group s))
  (doseq [mixer (:mixers s)]
    (kill-mixer mixer)))

(defn sequencer-set-out-bus!
  [s out-bus]
  (if (:mixers s)
    (ctl (:mixer-group s) :out-bus out-bus)
    (ctl (:seq-group s) :out-bus out-bus)))
