(ns meta-ex.nk.connected
  (:use [overtone.core])
  (:require [meta-ex.nk.stateful-device :as nksd]
            [meta-ex.nk.state-maps :as nksm]))

(defonce nk-connected-rcvs (midi-find-connected-receivers "nanoKONTROL2"))
(defonce nk-connected-devs (midi-find-connected-devices "nanoKONTROL2"))
(def nk-stateful-devs (map nksd/stateful-nk nk-connected-devs))
(def nano-kons (nksd/merge-nano-kons nk-connected-rcvs nk-stateful-devs))
(def state-maps (nksm/mk-state-map nano-kons))

(nksm/add-state state-maps :grumbles :s0 0.5)
;; (nksm/add-state state-maps :mixer :s1 0)
;; (nksm/add-state state-maps :cheese :s2 1)

(nksm/switch-state state-maps (first nano-kons) :grumbles)
;; (nksm/switch-state state-maps (first nano-kons) :mixer)
;; (nksm/switch-state state-maps (first nano-kons) :cheese)

(defn update-state
  [state-map-k id val]
  (nksm/update-state state-maps state-map-k id val))

(update-state :grumbles :slider7 1)

(on-event [:nanoKON2 :control-change :marker-right]
          (fn [m]
            (when (< 0 (:val m))
              (nksm/kill-all-flashers state-maps (:nk m))))
          ::kill-flashers)

(on-latest-event [:nanoKON2 :control-change]
                 (fn [m]
                   (nksm/nk-update-states state-maps
                                          (:nk m)
                                          (:id m)
                                          (:old-val m)
                                          (:val m)
                                          (:old-state m)
                                          (:state m)))
                 ::update-state)

;;(nksm/refresh state-maps (first nano-kons))
;; things to do
;; add a 'refresh' fn
;; teach stateful-devs to give themselves an id on connection
