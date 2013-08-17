(ns meta-ex.state
  (:require [meta-ex.hw.nk.stateful-device :as nksd]
            [meta-ex.hw.nk.state-maps :as nksm]
            [meta-ex.hw.nk.connected :as nk-connected]
            [meta-ex.resources :as resources]
            [meta-ex.kit.monome-sequencer :as mseq]
            [meta-ex.hw.fonome :as fon]))

(defn- nk-bank
  "Returns the nk bank number for the specified bank key"
  [bank-k]
  (case bank-k
    :master 0
    :m64 2
    :m128 4
    :riffs 8
    :synths 16))

(defn- validate-bank-k!
  [k]
  (assert (number? k) (str "bank key should be a number, got: " k))
  (assert (contains? #{0 2 4 8 16} k) (str "currently supported bank keys are: 0, 2, 4, 8. Got: " k)))

(defn- resolve-bank-k
  [bank-k]
  (let [k (cond
           (keyword? bank-k) (nk-bank bank-k)
           :else bank-k)]
    (validate-bank-k! k)
    k))

(defn save-nk-bank
  [bank store k]
  (let [bank   (resolve-bank-k bank)
        states (nksm/save-bank-states nk-connected/state-maps
                                      bank)]
    (resources/edn-save store k states)
    :saved))

(defn load-nk-bank
  [bank store k]
  (let [bank (resolve-bank-k bank)
        states (resources/edn-load store k)]
    (if states
      (do (nksm/load-bank-states nk-connected/state-maps bank states)
          :loaded)
      :failed)))


(defn save-sequencer
  [sequencer-k store k]
  (let [sequencer-k (name sequencer-k)
        fonome      (:fonome (get @mseq/m-sequencers sequencer-k))
        state       @(:state fonome)
        leds        (:leds state)
        ]
    (resources/edn-save store k leds)
    :saved))

(defn load-sequencer
  [sequencer-k store k]
  (let [sequencer-k (name sequencer-k)
        fonome      (:fonome (get @mseq/m-sequencers sequencer-k))
        leds        (resources/edn-load store k)
        ]
    (if leds
      (do
        (fon/set-led-state! fonome leds)
        :loaded)
      :failed)))

(defn save-nk
  [bank button-id store k]
  (let [bank (resolve-bank-k bank)
        state (nksm/save-state-by-button-id nk-connected/state-maps bank button-id)]
    (resources/edn-save store k state)
    :saved))

(defn load-nk
  [bank button-id store k]
  (let [bank (resolve-bank-k bank)
        state (resources/edn-load store k)]
    (if state
      (do
        (nksm/replace-state-by-button-id nk-connected/state-maps bank button-id state)
        :loaded)
      :failed)))
