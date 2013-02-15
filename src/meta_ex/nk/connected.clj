(ns meta-ex.nk.connected
  (:use [overtone.core])
  (:require [meta-ex.nk.device :as nkd]
            [meta-ex.nk.state :as nks]))

(defonce nk-connected-rcvs (midi-find-connected-receivers "nanoKONTROL2"))
(defonce nk-connected-devs (midi-find-connected-devices "nanoKONTROL2"))
(defonce nk-stateful-devs (map nkd/stateful-nk nk-connected-devs))
(defonce nano-kons (nkd/merge-nano-kons nk-connected-rcvs nk-stateful-devs))
(defonce state-maps (nks/mk-state-map nano-kons))

;;(nks/add-state state-maps :grumbles 0.5)

;;(nks/switch-state state-maps (first nano-kons) :mixer)

(defn update-state
  [state-k id val]
  (nks/update-state state-maps state-k id val))

(update-state :mixer :slider7 0.5)

(on-event [:nanoKON2 :control-change :marker-right]
          (fn [m]
            (when (< 0 (:val m))
              (nks/kill-all-flashers state-maps (:nk m))))
          ::kill-flashers)

(on-latest-event [:nanoKON2 :control-change]
                 (fn [m]
                   (nks/nk-update-states state-maps
                                         (:nk m)
                                         (:id m)
                                         (:old-val m)
                                         (:val m)
                                         (:old-state m)
                                         (:state m)))
                 ::update-state)
