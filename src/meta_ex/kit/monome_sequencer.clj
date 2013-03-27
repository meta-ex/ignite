(ns meta-ex.kit.monome-sequencer
  (:use [overtone.core]
        [overtone.helpers.lib :only [uuid]])
  (:require [meta-ex.hw.fonome :as fon]
            [meta-ex.kit.triggers :as trg]
            [meta-ex.kit.sequencer :as seq]))

(defonce m-sequencers (atom {}))

(defn- get-row [grid y range-x]
  (let [row (for [x (range range-x)]
              (get grid [x y] false))]
    (map #(if % 1 0) row)))

(defn sequencer-write-row! [sequencer y range-x grid]
  (let [row (get-row grid y range-x)]
    (when-not (>= y (count (:samples sequencer)))
      (seq/sequencer-write! sequencer y row))))

(defn sequencer-write-grid! [sequencer range-x range-y grid]
  (doseq [y (range range-x)]
    (sequencer-write-row! sequencer y range-x grid)))

(defn mk-monome-sequencer
  ([handle samples]
     (mk-monome-sequencer handle samples []))
  ([handle samples trig-samples]
     (mk-monome-sequencer handle samples trig-samples (first @fon/fonomes)))
  ([handle samples trig-samples tgt-fonome]
     (mk-monome-sequencer handle samples trig-samples tgt-fonome 0 ))
  ([handle samples trig-samples tgt-fonome out-bus]
     (mk-monome-sequencer handle samples trig-samples tgt-fonome 0 true))
  ([handle samples trig-samples tgt-fonome out-bus with-mixers?]
     (when-not tgt-fonome
       (throw (IllegalArgumentException. "Please pass a valid fonome to mk-monome-sequencer")))

     (let [range-x   (:width tgt-fonome)
           range-y   (:height tgt-fonome)
           sequencer (seq/mk-sequencer handle
                                       (take (dec range-y) samples)
                                       range-x
                                       (foundation-default-group)
                                       trg/beat-b
                                       trg/cnt-b
                                       out-bus
                                       with-mixers?)
           key1      (uuid)
           key2      (uuid)
           key3      (uuid)]

       (swap! m-sequencers (fn [ms]
                             (when (contains? ms handle)
                               (seq/sequencer-kill sequencer)
                               (throw (IllegalArgumentException.
                                       (str "A monome-sequencer with handle "
                                            handle
                                            " already exists."))))
                             (assoc ms handle sequencer)))

       (sequencer-write-grid! sequencer range-x range-y (fon/led-state tgt-fonome))

       (on-event [:fonome :led-change (:id tgt-fonome)]
                 (fn [{:keys [new-leds y]}]
                   (if y
                     (sequencer-write-row! sequencer y range-x new-leds)
                     (sequencer-write-grid! sequencer range-x range-y new-leds)))
                 key1)

       (on-event [:fonome :press (:id tgt-fonome)]
                 (fn [{:keys [x y fonome]}]
                   (fon/toggle-led fonome x y))
                 key2)

       (on-trigger trg/count-trig-id
                   (fn [beat]
                     (let [beat-track-y (dec (:height tgt-fonome ))]
                       (doseq [x (range (:width tgt-fonome)) ]
                         (fon/led-off tgt-fonome x beat-track-y))
                       (fon/led-on tgt-fonome  (mod beat range-x) beat-track-y)))
            key3)

       (oneshot-event :reset (fn [_] (remove-handler key1) (remove-handler key2)) (uuid))

       (with-meta
         {:sequencer      (atom sequencer)
          :led-change-key key1
          :press-key      key2
          :beat-key       key3
          :fonome         tgt-fonome
          :handle         handle
          :status         (atom :running)}
         {:type ::monome-sequencer}))))

(defn running? [seq]
  (= :running @(:status seq)))

(defn stop-sequencer [seq]
  (when (running? seq)
    (reset! (:status seq) :stopped)
    (swap! m-sequencers dissoc (:handle seq))
    (seq/sequencer-kill @(:sequencer seq))
    (remove-handler (:led-change-key seq))
    (remove-handler (:press-key seq))
    (remove-handler (:beat-key seq))))

(defn swap-samples! [m-seq samples]
  (let [sequencer     @(:sequencer m-seq)
        _             (seq/sequencer-kill sequencer)
        new-sequencer (seq/mk-sequencer (:handle m-seq)
                                        (take (dec (-> m-seq :fonome :height)) samples)
                                        (-> m-seq :fonome :width)
                                        (foundation-default-group)
                                        trg/beat-b
                                        trg/cnt-b
                                        (-> sequencer :out-bus)
                                        (-> sequencer :with-mixers?))]
    (sequencer-write-grid! new-sequencer (-> m-seq :fonome :width) (-> m-seq :fonome :width)  (fon/led-state (:fonome m-seq)))
    (reset! (:sequencer m-seq) new-sequencer)
    m-seq))
