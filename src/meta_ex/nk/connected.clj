(ns meta-ex.nk.connected
  (:use [overtone.core])
  (:require [meta-ex.nk.stateful-device :as nksd]
            [meta-ex.nk.state-maps :as nksm]))

(defonce nk-connected-rcvs (midi-find-connected-receivers "nanoKONTROL2"))
(defonce nk-connected-devs (midi-find-connected-devices "nanoKONTROL2"))
(def nk-stateful-devs (map nksd/stateful-nk nk-connected-devs))
(def nano-kons (nksd/merge-nano-kons nk-connected-rcvs nk-stateful-devs))
(def state-maps (nksm/mk-state-map nano-kons))

(nksm/add-state state-maps :grumbles :s0 0)
(nksm/add-state state-maps :mixer :s1 0)
(nksm/add-state state-maps :master-drum :m0 0)
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
              (nksm/refresh state-maps (:nk m))))
          ::refresh)

(on-event [:nanoKON2 :control-change :record]
          (fn [m]
            (if (< 0 (:val m))
              (nksm/nk-clutch-on state-maps (:nk m))
              (nksm/nk-clutch-off state-maps (:nk m))))
          ::clutch)

(on-event [:nanoKON2 :control-change :marker-set]
          (fn [m]
            (when (< 0 (:val m))
              (nksm/nk-force-sync-all state-maps (:nk m) (:old-state m) (:state m))))
          ::clutch)

(on-event [:nanoKON2 :control-change :cycle]
          (fn [m]
            (when (< 0 (:val m))
              (nksm/nk-switcher-mode state-maps (:nk m))))
          ::switch-state)

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

;; things to do:

;; have a way of recording states

;; have a way of giving more bespoke starting vals

;; Have a way of visualising each non-synced control via flashes which
;; are not relative to the difference between the raw val and the state
;; val, but are an indication between 0 and 1 to gain an approximation
;; of the current value at a glance

;;(.printStackTrace (agent-error state-maps))
