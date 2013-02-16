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

(on-event [:nanoKON2 :control-change :cycle]
          (fn [m]
            (when (< 0 (:val m))
              (nksm/nk-switch-state state-maps (:nk m))))
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

;; in switcher mode - flash the state you're currently in

;; in switcher mode - if you hit the cycle button again, go back to previous state

;; have a way of recording states

;; have a way of giving more bespoke starting vals

;; pressing the sync flasher led should force sync and make value jump
;; to current raw val

;; there should be a way of forcing an unsync
