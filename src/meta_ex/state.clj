(ns meta-ex.state
  (:require [meta-ex.hw.nk.stateful-device :as nksd]
            [meta-ex.hw.nk.state-maps :as nksm]
            [meta-ex.hw.nk.connected :as nk-connected]
            [meta-ex.resources :as resources]
            [meta-ex.kit.monome-sequencer :as mseq]))

(defn- nk-bank
  "Returns the nk bank number for the specified bank key"
  [bank-k]
  (case bank-k
    :master 0
    :m64 2
    :m128 4
    :synths 8))

(defn- validate-bank-k!
  [k]
  (assert (number? k) (str "bank key should be a number, got: " k))
  (assert (contains? #{0 2 4 8} k) (str "currently supported bank keys are: 0, 2, 4, 8. Got: " k)))

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
