(ns meta-ex.kit.monome-sequencer
  (:use [overtone.core]
        [overtone.helpers.lib :only [uuid]])
  (:require [meta-ex.hw.fonome :as fon]
            [meta-ex.kit.timing :as tim]
            [meta-ex.kit.sequencer :as seq]))

(defonce m-sequencers (atom {}))

(defn- get-row [grid y range-x]
  (let [row (for [x (range range-x)]
              (get grid [x y] false))]
    (map #(if % 1 0) row)))

(defn sequencer-write-row! [sequencer y range-x grid]
  (let [row (get-row grid y range-x)]
    (when-not (>= y (:num-samps sequencer))
      (seq/sequencer-write! sequencer y row))))

(defn sequencer-write-grid! [sequencer range-x range-y grid]
  (doseq [y (range range-x)]
    (sequencer-write-row! sequencer y range-x grid)))

(defn monome-sequencer?
  [o]
  (isa? (type o) ::monome-sequencer))


(defn- update-monome-leds
  [tgt-fonome range-x beat]
  (let [beat-track-y (dec (:height tgt-fonome))]
    (fon/led-on tgt-fonome  (mod (dec beat) range-x) beat-track-y)
    (fon/led-off tgt-fonome  (mod beat range-x) beat-track-y)
    ))


(defn mk-monome-sequencer
  ([nk-group handle samples tgt-fonome]
     (mk-monome-sequencer nk-group handle samples tgt-fonome 0 ))
  ([nk-group handle samples tgt-fonome out-bus]
     (mk-monome-sequencer nk-group handle samples tgt-fonome 0 (foundation-default-group)))
  ([nk-group handle samples tgt-fonome out-bus tgt-g]
     (mk-monome-sequencer nk-group handle samples tgt-fonome out-bus tgt-g true))
  ([nk-group handle samples tgt-fonome out-bus tgt-g with-mixers?]
     (mk-monome-sequencer nk-group handle samples tgt-fonome out-bus tgt-g with-mixers? tim/main-beat))
  ([nk-group handle samples tgt-fonome out-bus tgt-g with-mixers? beat-bus]
     (when-not tgt-fonome
       (throw (IllegalArgumentException. "Please pass a valid fonome to mk-monome-sequencer")))

     (let [range-x     (:width tgt-fonome)
           range-y     (:height tgt-fonome)
           t-id        (:trig-id beat-bus)
           beat-bus-a  (atom beat-bus)
           sequencer   (seq/mk-sequencer nk-group
                                         handle
                                         (take (dec range-y) samples)
                                         range-x
                                         tgt-g
                                         beat-bus-a
                                         out-bus
                                         with-mixers?)
           seq-atom    (atom sequencer)
           key1        (uuid)
           key2        (uuid)
           key3        (uuid)
           m-sequencer (with-meta
                         {:sequencer      seq-atom
                          :led-change-key key1
                          :press-key      key2
                          :beat-key       key3
                          :fonome         tgt-fonome
                          :handle         handle
                          :status         (atom :running)
                          :nk-group       nk-group}
                         {:type ::monome-sequencer})]

       (swap! m-sequencers (fn [ms]
                             (when (contains? ms handle)
                               (seq/sequencer-kill sequencer)
                               (throw (IllegalArgumentException.
                                       (str "A monome-sequencer with handle "
                                            handle
                                            " already exists."))))
                             (assoc ms handle m-sequencer)))

       (sequencer-write-grid! sequencer range-x range-y (fon/led-state tgt-fonome))

       (on-event [:fonome :led-change (:id tgt-fonome)]
                 (fn [{:keys [new-leds y]}]
                   (if y
                     (sequencer-write-row! @seq-atom y range-x new-leds)
                     (sequencer-write-grid! @seq-atom range-x range-y new-leds)))
                 key1)

       (on-event [:fonome :press (:id tgt-fonome)]
                 (fn [{:keys [x y fonome]}]
                   (fon/toggle-led fonome x y))
                 key2)

       (on-trigger t-id
                   (fn [beat]
                     (let [beat-track-y (dec (:height tgt-fonome))]
                       (doseq [x (range (:width tgt-fonome)) ]
                         (fon/led-off tgt-fonome x beat-track-y))
                       (fon/led-on tgt-fonome  (mod beat range-x) beat-track-y)))
                   key3)

       (add-watch beat-bus-a ::update-beat-bus-monome-knightrider
                  (fn [k r o n]
                    (println "updating knightrider " n)
                    (on-trigger (:trig-id n)
                                #(update-monome-leds tgt-fonome range-x %)
                                key3)))

       (oneshot-event :reset (fn [_] (remove-event-handler key1) (remove-event-handler key2)) (uuid))

       m-sequencer)))

(defn running? [seq]
  (= :running @(:status seq)))

(defn stop-sequencer [seq]
  (assert (monome-sequencer? seq))
  (when (running? seq)
    (reset! (:status seq) :stopped)
    (swap! m-sequencers dissoc (:handle seq))
    (seq/sequencer-kill @(:sequencer seq))
    (remove-event-handler (:led-change-key seq))
    (remove-event-handler (:press-key seq))
    (remove-event-handler (:beat-key seq))))

(defn swap-samples! [m-seq samples]
  (assert (monome-sequencer? m-seq))
  (seq/swap-samples! @(:sequencer m-seq) samples))

(defn swap-beat-bus! [m-seq beat-bus]
  (assert (monome-sequencer? m-seq))
  (seq/swap-beat-bus! @(:sequencer m-seq) beat-bus))
