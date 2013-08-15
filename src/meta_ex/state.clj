(ns meta-ex.state
  (:require [meta-ex.hw.nk.stateful-device :as nksd]
            [meta-ex.hw.nk.state-maps :as nksm]
            [meta-ex.hw.nk.connected :as nk-connected]
            [meta-ex.resources :as resources]))

(defn save-nk-bank
  [bank store k]
  (let [states (nksm/save-bank-states nk-connected/state-maps
                                      bank)]
    (resources/edn-save store k states)
    :saved))

(defn load-nk-bank
  [bank store k]
  (let [states (resources/edn-load store k)]
    (if states
      (do (nksm/load-bank-states nk-connected/state-maps bank states)
          :loaded)
      :failed)))
