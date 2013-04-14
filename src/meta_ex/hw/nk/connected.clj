(ns meta-ex.hw.nk.connected
  (:use [overtone.core])
  (:require [meta-ex.hw.nk.stateful-device :as nksd]
            [meta-ex.hw.nk.state-maps :as nksm]))

(defonce nk-connected-rcvs (midi-find-connected-receivers "nanoKONTROL2"))
(defonce nk-connected-devs (midi-find-connected-devices "nanoKONTROL2"))
(defonce nk-stateful-devs (map nksd/stateful-nk nk-connected-devs))
(defonce nano-kons (nksd/merge-nano-kons nk-connected-rcvs nk-stateful-devs))
(defonce state-maps (nksm/mk-state-map nano-kons))

;; (nksm/add-state state-maps :cheese :s2 1)

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

(on-event [:nanoKON2 :control-change :marker-left]
          (fn [m]
            (if (< 0 (:val m))
              (nksm/nk-absolute-val-viz-on state-maps (:nk m))
              (nksm/nk-absolute-val-viz-off state-maps (:nk m))))
          ::viz)

(on-event [:nanoKON2 :control-change :marker-set]
          (fn [m]
            (when (< 0 (:val m))
              (nksm/nk-force-sync-all state-maps (:nk m) (:old-state m) (:state m))))
          ::force-sync-all)

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

;;(.printStackTrace (agent-error state-maps))
