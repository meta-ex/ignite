(ns meta-ex.hw.nk.state-maps
  (:use [meta-ex.lib.timed]
        [meta-ex.hw.nk.stateful-device]
        [overtone.core]
        [overtone.helpers.doc :only [fs]]

        [overtone.helpers.ref :only [swap-returning-prev!]]))

;; new state-map
;; (def state-maps (agent {:states {0 {:mixer    {:state (nk-state-map 0)
;;                                                :button-id :s0}
;;                                     :grumbles {:state (nk-state-map 0)
;;                                                :button-id :s1}}

;;                                  1 {:mixer    {:state (nk-state-map 0)
;;                                                :button-id :s4}
;;                                     :bar {:state (nk-state-map 0)
;;                                           :button-id :s0}}}
;;                         :nks   {(first nano-kons) {:syncs        (nk-state-map false)
;;                                                    :flashers     (nk-state-map nil)
;;                                                    :current      [0 :mixer]
;;                                                    :raw-state    (nk-state-map nil)
;;                                                    :mode         :controller
;;                                                    :switch-group nil }}}))
;; Available modes: [:controller, :clutch, :switcher]

(defn- sm-g-k->state
  "Get the state for a specific key and group"
  [sm g k]
  (get-in sm [:states g k :state]))

(defn- sm-g-k->button-id
  "Get the button-id for a specific key and group"
  [sm g k]
  (get-in sm [:states g k :button-id]))

(defn- sm-nk->syncs
  "Get the syncs for a specific nk in the sm"
  [sm nk]
  (get-in sm [:nks nk :syncs]))

(defn- sm-nk->flashers
  "Get the flashers for a specific nk in the sm"
  [sm nk]
  (get-in sm [:nks nk :flashers]))

(defn- sm-nk->raw-state
  "Get the raw state (state of the physical nk device) for a specific nk
  in the sm"
  [sm nk]
  (get-in sm [:nks nk :raw-state]))

(defn- sm-nk->current-gk
  "Get the current state-map [g k] vec for a specific nk in the sm"
  [sm nk]
  (get-in sm [:nks nk :current]))

(defn- sm-nk->current-state
  "Get the current state associated with a specific nk"
  [sm nk]
  (let [[g k] (sm-nk->current-gk sm nk) ]
    (sm-g-k->state sm g k)))

(defn- sm-nk->current-group
  "Get the current group associated with a specific nk. Prefer group
   in :switch-group key if exists."
  [sm nk]
  (let [[g k]        (sm-nk->current-gk sm nk)
        switch-group (get-in sm [:nks nk :switch-group])]
    (or switch-group g)))

(defn sm-nk->current-button-id
  "Returns the button-id for the current state of the nk"
  [sm nk]
  (let [[g k] (sm-nk->current-gk sm nk)]
    (get-in sm [:states g k :button-id])))

(defn- sm-nk->current-mode
  "Return the current mode for nk"
  [sm nk]
  (get-in sm [:nks nk :mode]))

(defn- sm-gk->nks
  "Get all the nks associated with a specific gk vec"
  [sm gk]
  (let [nks (:nks sm)]
    (reduce (fn [r [k v]]
              (if (= gk (:current v))
                (conj r k)
                r))
            #{}
            nks)))

(defn- sm-g->button-ids-nks
  "Return a map of button-ids to lists of nks currently controlling the state
   with that button-id for group g"
  [sm g]
  (let [all-states-for-g (get-in sm [:states g])
        k->button-id     (reduce (fn [r [k state-info]]
                                   (conj r [k (:button-id state-info)]))
                                 #{}
                                 all-states-for-g)]
    (reduce (fn [r [k bid]]
              (let [nks (sm-gk->nks sm [g k])]
                (assoc r bid nks)))
            {}
            k->button-id)))

(defn sm-g-button-id->valid?
  "Returns true if the button-id is associated with a state in group g."
  [sm g button-id]
  (let [available (sm-g->button-ids-nks sm g)]
    (contains? available button-id)))

(defn sm-g-button-id->k
  "Returns the state-k for state with button-id within group g"
  [sm g button-id]
  (ffirst (filter (fn [[k v]] (= button-id (:button-id v)))
                  (get-in sm [:states g]))))

(defn- sm-nk-switcher-mode?
  "Returns true if the specific nk is currently in switcher mode"
  [sm nk]
  (= :switcher (sm-nk->current-mode sm nk)))

(defn- sm-nk-controller-mode?
  "Returns true if the specific nk is currently in controller mode"
  [sm nk]
  (= :controller (sm-nk->current-mode sm nk)))

(defn- sm-nk-clutch-mode?
  "Returns true if the specific nk is currently in clutch mode"
  [sm nk]
  (= :clutch (sm-nk->current-mode sm nk)))

(defn- sm-nk-manipulation-mode?
  "Returns true if the specific nk is being manipulated -
  i.e. controller mode or clutch mode"
  [sm nk]
  (or (sm-nk-controller-mode? sm nk)
      (sm-nk-clutch-mode? sm nk)))

(defn- controller-id?
  "Returns true if state-id represents a controller (pot or slider)"
  [state-id]
  (boolean (re-matches #"(slider|pot)[0-7]" (name state-id))))

(defn- sync-id?
  "Returns true if state-id represents a sync button (smr buttons)"
  [state-id]
  (boolean (re-matches #"[smr][0-7]" (name state-id))))

(defn- sm-g->valid?
  "Returns true if g is a valid group in sm"
  [sm g]
  (contains? (:states sm) g))

(defn- sm-g-k->valid?
  "Returns true if k is a valid state key within group g in sm"
  [sm g k]
  (and (sm-g->valid? sm g)
       (contains? (get-in sm [:states g]) k)))

(defn- controller-id->sync-led-id
  "Given a controller id - returns the matching sync led id"
  [id]
  (assert (controller-id? id))
  (let [n   (name id)
        idx (re-find #"[0-7]" n)
        but (if (.startsWith n "slider")
              "r"
              "s")]

    (keyword (str but idx))))

(defn- sync-led-id->controller-id
  "Given a sync led id - returns the matching controller id"
  [id]
  (assert (sync-id? id))
  (let [n   (name id)
        idx (re-find #"[0-7]" n)
        but (if (.startsWith n "r")
              "slider"
              "pot")]

    (keyword (str but idx))))

(defn- controller-id->warmer-led-id
  "Given a controller id, returns the corresponding led used to indicate
   the 'warmth' of the physical controller relative to the virtual -
   i.e. distance can represented by the warmth led through relative
   flashing speeds (slow for far, quick for near)."
  [id]
  (let [n (name id)
        idx (re-find #"[0-7]" n)]
    (keyword (str "m" idx))))

(defn- mk-blink-led
  "Returns a state-machine-like function which will alternate the led
   associated with id on each successive call."
  [nk id]
  (cycle-fn (fn [] (led-on nk id))
            (fn [] (led-off nk id))))

(def delay-mul 1000)

(defn- sm-nk-swap-current
  "Return a new sm with the current gk vec replaced"
  [sm nk g k]
  (assoc-in sm [:nks nk :current] [g k]))

(defn- sm-nk-swap-switch-group
  "Swap the switch-group - a key representing the new group to switch to"
  [sm nk g]
  (assoc-in sm [:nks nk :switch-group] g))

(defn- sm-swap-state
  "Return a new sm with the state with specific group and k replaced"
  [sm g k new-state]
  (assoc-in sm [:states g k :state] new-state))

(defn- sm-nk-swap-mode
  "Return a new sm with the mode of the specific nk replaced"
  [sm nk mode]
  (assoc-in sm [:nks nk :mode] mode))

(defn- sm-nk-swap-state
  "Return a new sm with the state matching the nk replaced"
  [sm nk new-state]
  (let [[g k] (sm-nk->current-gk sm nk)]
    (sm-swap-state sm g k new-state)))

(defn- sm-nk-swap-raw-state
  "Return a new sm with the nk's raw-state replaced"
  [sm nk new-raw-state]
  (assoc-in sm [:nks nk :raw-state] new-raw-state))

(defn- sm-nk-swap-flashers
  "Return a new sm with the flashers replaced"
  [sm nk flashers]
  (assoc-in sm [:nks nk :flashers] flashers))

(defn- sm-nk-swap-syncs
  "Return a new sm with the syncs replaced"
  [sm nk syncs]
  (assoc-in sm [:nks nk :syncs] syncs))

(defn- sm-add-state
  "Return a new sm with the new state"
  [sm g k s button-id]
  (assoc-in sm [:states g k] {:state s
                              :button-id button-id}))

(defn- sm-add-nk
  "Add a new nk to the statemap"
  [sm nk]
  (assoc-in sm [:nks nk] {:syncs (nk-state-map false)
                          :flashers (nk-state-map nil)
                          :current nil
                          :mode :controller
                          :raw-state (nk-state-map nil)
                          :switch-group nil}))

(defn mk-state-map
  "Create a new state map agent used to represent a bunch of nks and
   their available virtual states"
  ([] (mk-state-map []))
  ([nks]
     (let [sm {:states {}
               :nks {}}]
       (agent (reduce (fn [r nk]
                        (sm-add-nk r nk))
                      sm
                      nks)))))

(defn nk-smr-leds-off
  "Turn off all smr leds on nk"
  [nk]
  (doseq [i (range 8)]
    (led-off nk (keyword (str "s" i)))
    (led-off nk (keyword (str "m" i)))
    (led-off nk (keyword (str "r" i)))))

(defn nk-smr-leds-on
  "Turn on all smr leds on nk"
  [nk]
  (doseq [i (range 8)]
    (led-on nk (keyword (str "s" i)))
    (led-on nk (keyword (str "m" i)))
    (led-on nk (keyword (str "r" i)))))

(defn nk-rec-leds-on
  "Turn off all recording leds on nk"
  [nk]
  (led-on nk :rewind)
  (led-on nk :fast-forward)
  (led-on nk :stop)
  (led-on nk :play)
  (led-on nk :record))

(defn nk-rec-leds-off
  "Turn off all recording leds on nk"
  [nk]
  (led-off nk :rewind)
  (led-off nk :fast-forward)
  (led-off nk :stop)
  (led-off nk :play)
  (led-off nk :record))

(defn nk-show-group
  "Turn on the rec lights specific to the group"
  [nk group]
  (nk-rec-leds-off nk)
  (cond
   (= 0 group)  (led-on nk :record)
   (= 2 group)  (led-on nk :play)
   (= 4 group)  (led-on nk :stop)
   (= 8 group)  (led-on nk :fast-forward)
   (= 16 group) (led-on nk :rewind)))

(defn kill-all-flashers*
  "Kill all the flashers on a specific nk"
  [sm nk]
  (let [state-map       (sm-nk->current-state sm nk)
        flashers        (sm-nk->flashers sm nk)
        flashers        (reduce (fn [r [k v]]
                                  (when (and v (live? v))
                                    (kill v)
                                    (led-off nk k))
                                  (assoc r k nil))
                                {}
                                flashers)]
    (doseq [i (range 8)]
      (led-off nk (keyword (str "m" i))))

    (sm-nk-swap-flashers sm nk flashers))
  sm)

(defn refresh*
  "Refresh a nk's leds. If in a manipulation mode:
   - kills all flashers
   - shows group
   - turns on sync leds"
  [sm nk]
  (if  (sm-nk-manipulation-mode? sm nk)
    (let [sm    (kill-all-flashers* sm nk)
          syncs (sm-nk->syncs sm nk)
          group (sm-nk->current-group sm nk)]
      (nk-smr-leds-off nk)
      (nk-show-group nk group)
      (doseq [[id synced?] syncs]
        (when synced?
          (led-on nk (controller-id->sync-led-id id))))
      sm)
    sm))

(defn- flasher-delay
  "Calculate the flasher delay. This is a function of the 'distance'
   between the virtual val and the physical raw val. Closeness is
   defined as a shorter delay whilst distance is a longer delay"
  [val raw]
  (if (and (number? val)
           (number? raw))
    (* (Math/abs (- val raw)) delay-mul)

    ;;broken vals: choose an arbitrarily large value
    2000))

(defn- mk-flasher
  "Creates a ScheduledPattern which will flash a specific led id for nk
   with a specified delay time in ms."
  [nk id delay]
  (temporal (mk-blink-led nk id) delay))

(declare update-syncs-and-flashers*)

(defn- sm-nk-unsync-other-nks*
  "Update syncs and flashers associated with the specific group, state
   key and id for all other registered nks in sm other than this specific nk"
  [sm nk g k id v]
  (reduce (fn [r nk]
            (update-syncs-and-flashers* r nk id v))
          sm
          (remove #{nk} (sm-gk->nks sm [g k]))))

(defn- nk-update-states-range*
  "Update the state of nk's range control (i.e. a slider or pot) with
  specific id to new raw value."
  [sm nk id raw]
  (if (sm-nk-manipulation-mode? sm nk)
    (let [[g k]         (sm-nk->current-gk sm nk)
          raw-state     (sm-nk->raw-state sm nk)
          old-raw       (get raw-state id)
          old-state     (sm-nk->current-state sm nk)
          syncs         (sm-nk->syncs sm nk)
          flashers      (sm-nk->flashers sm nk)
          val           (get old-state id)
          was-synced?   (get syncs id)
          flasher-id    (controller-id->sync-led-id id)
          warmer-id     (controller-id->warmer-led-id id)
          flasher       (get flashers flasher-id)
          synced?       (and
                         (not (sm-nk-clutch-mode? sm nk))
                         (or was-synced?
                             (and old-raw (<= old-raw val raw))
                             (and old-raw (>= old-raw val raw))))
          syncs         (assoc syncs id synced?)
          new-val       (if synced? raw val)
          state         (assoc old-state id new-val)
          new-raw-state (assoc raw-state id raw)
          sm            (if synced?
                          (sm-nk-unsync-other-nks* sm nk g k id new-val)
                          sm)]

      (when (and (not was-synced?) synced?)
        (when flasher (kill flasher))
        (led-on nk flasher-id)
        (led-off nk warmer-id))

      (let [flashers (if synced?
                       (do
                         (event [:v-nanoKON2 g k :control-change id]
                                :group g
                                :id id
                                :old-state old-state
                                :state state
                                :old-val val
                                :val new-val)
                         (event [:v-nanoKON2 g k :control-change]
                                :group g
                                :id id
                                :old-state old-state
                                :state state
                                :old-val val
                                :val new-val)
                         (assoc flashers flasher-id nil))

                       (let [delay    (flasher-delay val raw)
                             flasher (if (and flasher (live? flasher))
                                       (delay-set! flasher delay)
                                       (mk-flasher nk flasher-id delay))]
                         (if (or (and old-raw raw val
                                      (< old-raw raw val))
                                 (and old-raw raw val
                                      (> old-raw raw val)))
                           (led-on nk warmer-id)
                           (led-off nk warmer-id))
                         (assoc flashers flasher-id flasher)))]
        (-> sm
            (sm-nk-swap-syncs nk syncs)
            (sm-nk-swap-flashers nk flashers)
            (sm-nk-swap-state nk state)
            (sm-nk-swap-raw-state nk new-raw-state))))
    sm))

(defn switch-state*
  "Switch the state of the nk to state with k in group g"
  [sm nk g k]
  (if (sm-g-k->valid? sm g k)
    (do
      (nk-smr-leds-off nk)

      (let [sm         (kill-all-flashers* sm nk)
            latest-raw (sm-nk->raw-state sm nk)
            state      (sm-g-k->state sm g k)
            syncs      (sm-nk->syncs sm nk)
            syncs      (reduce (fn [r [id v]]
                                 (when (= :slider0 id)
                                   (println "hi" [id (get state id) (get latest-raw id)]))
                                 (let [synced? (= (get state id)
                                                  (get latest-raw id))]

                                   (when synced?
                                     (led-on nk (controller-id->sync-led-id id)))
                                   (assoc r id synced?)))
                               {}
                               syncs)]
        (nk-show-group nk g)
        (-> sm
            (sm-nk-swap-current nk g k)
            (sm-nk-swap-syncs nk syncs)
            (sm-nk-swap-switch-group nk nil)
            (sm-nk-swap-mode nk :controller))))
    sm))

(defn sm-nk-kill-flasher*
  "Kill flasher with id on nk"
  [sm nk id]
  (let [flashers (sm-nk->flashers sm nk)
        flasher  (get flashers id)
        flashers (assoc flashers id nil)]

    (when flasher
      (kill flasher))

    (sm-nk-swap-flashers sm nk flashers)))

(defn nk-force-sync*
  "Force the state value assocated with the nk's id to the new raw
   value."
  [sm nk id raw]
  (cond

   (and (controller-id? id)
        (nil? raw))
   (do (future
         (dotimes [x 5]
           (led-on nk (controller-id->sync-led-id id))
           (Thread/sleep 50)
           (led-off nk (controller-id->sync-led-id id))
           (Thread/sleep 50)))
     sm)

   (controller-id? id)
   (let [[g k]           (sm-nk->current-gk sm nk)
         old-state       (sm-nk->current-state sm nk)
         state           (assoc old-state id raw)
         syncs           (sm-nk->syncs sm nk)
         syncs           (assoc syncs id true)
         sm              (sm-nk-kill-flasher* sm nk (controller-id->sync-led-id id))
         sm              (sm-nk-unsync-other-nks* sm nk g k id raw)
         val             (get old-state id)]

     (led-on nk (controller-id->sync-led-id id))
     (led-off nk (controller-id->warmer-led-id id))

     (event [:v-nanoKON2 g k :control-change k]
            :id id
            :old-state old-state
            :state state
            :old-val val
            :val raw)

     (event [:v-nanoKON2 g k :control-change]
            :id id
            :old-state old-state
            :state state
            :old-val val
            :val raw)
     (-> sm
         (sm-nk-swap-state nk state)
         (sm-nk-swap-syncs nk syncs)))

   :else sm))

(defn- group-button-id->group
  "Use powers of two to eventually support binary notation across the
   record row of buttons."
  [id]
  (cond
   (= :record id) 0
   (= :play id) 2
   (= :stop id) 4
   (= :fast-forward id) 8
   (= :rewind id) 16))

(defn- group-button?
  [id]
  (or (= :record id)
      (= :play id)
      (= :stop id)
      (= :fast-forward id)
      (= :rewind id)))

(declare nk-enter-switcher-mode*)

(defn nk-update-states-button*
  "A button has been pressed...
   - if we're in switcher mode, switch state
   - if we're in controller-mode, force sync
   - otherwise do nothing

   Always ensure that the nk's raw state is updated with the new raw
   value.
   "
  [sm nk id raw]
  (let [g         (sm-nk->current-group sm nk )
        raw-state (sm-nk->raw-state sm nk)]
    (cond

     ;; switch group (within switcher mode)
     (and (= 1.0 raw)
          (sm-nk-switcher-mode? sm nk)
          (group-button? id))
     (let [g (group-button-id->group id)]
       (-> sm
           (sm-nk-swap-switch-group nk g)
           (nk-enter-switcher-mode* nk)))

     ;; switch state
     (and (= 0.0 raw)
          (sm-nk-switcher-mode? sm nk)
          (sm-g-button-id->valid? sm g id))
     (let [state-k (sm-g-button-id->k sm g id)]
       (led-off nk :cycle)
       (-> sm
           (kill-all-flashers* nk)
           (switch-state* nk g state-k)
           (sm-nk-swap-switch-group nk nil)
           (sm-nk-swap-raw-state nk (assoc raw-state id raw))))

     ;; force sync
     (and (= 1.0 raw)
          (sm-nk-controller-mode? sm nk)
          (sync-id? id))
     (let [controller-id (sync-led-id->controller-id id)

           ctl-raw       (get raw-state controller-id)]
       (-> sm
           (nk-force-sync* nk controller-id ctl-raw)
           (sm-nk-swap-raw-state nk (assoc raw-state id raw))))

     ;; do nothing except register button press in raw-state
     :else (-> sm
               (sm-nk-swap-raw-state nk (assoc raw-state id raw))))))

(defn- update-syncs-and-flashers*
  "Update the syncs and flashers for the specific state matching nk's
   id. Does not update the raw-state of the nk."
  [sm nk id v]
  (if  (sm-nk-controller-mode? sm nk)
    (let [raw-state  (sm-nk->raw-state sm nk)
          raw        (get raw-state id)
          old-syncs  (sm-nk->syncs sm nk)
          old-sync   (get old-syncs id)
          syncs      (assoc old-syncs id false)
          flashers   (sm-nk->flashers sm nk)
          flasher    (get flashers id)]

      (when flasher
        (delay-set! flasher (flasher-delay v raw)))

      (when old-sync
        (led-off nk (controller-id->sync-led-id id)))
      (sm-nk-swap-syncs sm nk syncs))
    sm))

(defn nk-update-states
  "update states asynchronously with an agent, however make it 'more'
   synchronous by syncing with a promise. This is useful as this fn is
   designed to be used within an on-latest-event handler which works
   better with synchronous fns. However, we also want the sequential
   no-retry property of agents which is why we're using them here."
  [state-a nk id raw]
  (let [p (promise)]
    (send state-a
          (fn [sm p]
            (let [res (if (and (contains? (:nks sm) nk)
                               (sm-nk->current-state sm nk))
                        (if (button? id)
                          (nk-update-states-button* sm nk id raw)
                          (nk-update-states-range* sm nk id raw))
                        sm)]
              (deliver p true)
              res))
          p)
    @p))

;; (defn update-states-range
;;   [state-a nk k v]
;;   (when-not (button? k)))

(defn switch-state
  "Switch nk to state matching g state-k"
  [state-a nk g state-k]
  (send state-a switch-state* nk g state-k))

(defn refresh
  "Refresh the leds of the specific nk (i.e. stops all flashers)"
  [state-a nk]
  (send state-a refresh* nk))

(defn ensure-valid-val!
  [v]
  (assert (or
           (map? v)
           (and (number? v)
                   (<= 0 v)
                   (<= v 1)))
          "State value must be a number between 0 and 1 inclusively"))

(defn ensure-valid-group!
  [k]
  (assert (and (integer? k)
               (<= 0 k)
               (<= k 8))
          "State group must be a number between 0 and 8 inclusively"))

;; Fix me
;; (defn update-state*
;;   [sm g k v]
;;   (if (contains? (get-in sm [:states g]) k)
;;     (let [old-state (sm-g-k->state sm g k)
;;           state     (assoc old-state k v)
;;           button-id (sm-g-k->button-id sm g k)

;;           []
;;           sm        (reduce (fn [r nk]
;;                               (update-syncs-and-flashers* r nk button-id v))
;;                             sm
;;                             (sm-gk->nks sm g k))]
;;       (sm-swap-state sm k state))
;;     sm))

;; (defn update-state
;;   "update the state associated with group g, state key k to value v"
;;   [state-a g k v]
;;   (ensure-valid-val! v)

;;   (send state-a update-state* g state-k v))

(defn- add-nk*
  [sm nk]
  (sm-add-nk sm nk))

(defn add-nk
  "add a new nk to the statemap agent"
  [state-a nk]
  (send state-a add-nk* nk))

(defn- add-state*
  [sm group state-k state button-id]
  (sm-add-state sm group state-k state button-id))

(defn add-state
  ([state-a group button-id init-val-or-state-map]
     (add-state state-a group button-id button-id init-val-or-state-map))
  ([state-a group state-k button-id init-val-or-state-map]
     (ensure-valid-val! init-val-or-state-map)
     (ensure-valid-group! group)
     (let [state (if (number? init-val-or-state-map)
                   (nk-state-map init-val-or-state-map)
                   init-val-or-state-map)]
       (send state-a add-state* group state-k state button-id)
       (str "Added state " group ", " state-k ", " button-id))))

(defn nk-enter-switcher-mode*
  [sm nk]
  (let [sm             (kill-all-flashers* sm nk)
        sm             (sm-nk-swap-mode sm nk :switcher)
        g              (sm-nk->current-group sm nk)
        available      (sm-g->button-ids-nks sm g)
        flashers       (sm-nk->flashers sm nk)
        curr-button-id (sm-nk->current-button-id sm nk)]

    (nk-smr-leds-off nk)
    (nk-show-group nk g)
    (led-on nk :cycle)

    (let [flashers (reduce (fn [r [k v]]
                             (let [nks (get available k)]
                               (cond
                                (nil? nks) (assoc r k v)
                                (empty? nks) (do
                                               (led-on nk k)
                                               (assoc r k v))
                                :else (if (= curr-button-id k)
                                        (assoc r k (mk-flasher nk k 250))
                                        (assoc r k (mk-flasher nk k 50))))))
                           {}
                           flashers)]
      (sm-nk-swap-flashers sm nk flashers))))

(defn nk-absolute-val-viz-on*
  [sm nk]
  (let [sm      (kill-all-flashers* sm nk)
        sm      (sm-nk-swap-mode sm nk :viz)
        state   (sm-nk->current-state sm nk)
        syncs   (sm-nk->syncs sm nk)
        ctl-ids (filter controller-id? (keys state))
        group    (sm-nk->current-group sm nk)]

    (nk-smr-leds-off nk)
    (nk-show-group nk group)
    (reduce (fn [r ctl-id]
              (let [val      (get state ctl-id)
                    sync-led (controller-id->sync-led-id ctl-id)
                    synced?  (get syncs ctl-id)]
                (if synced?
                  (do
                    (led-on nk sync-led)
                    r)
                  (let [flashers (sm-nk->flashers r nk)
                        delay    (scale-range val 0 1 500 10)
                        flasher  (mk-flasher nk sync-led delay)
                        flashers (assoc flashers sync-led flasher)]
                    (sm-nk-swap-flashers r nk flashers)))))
            sm
            ctl-ids)))

(defn nk-force-sync
  [state-a nk id raw]
  (send state-a nk-force-sync* nk id raw))

(defn nk-force-sync-all*
  [sm nk]
  (if (sm-nk-manipulation-mode? sm nk)
    (let [raw-state (sm-nk->raw-state sm nk)
          ctl-ids   (filter controller-id? (keys raw-state))]
      (reduce (fn [r id]
                (let [raw (get raw-state id)]
                  (nk-force-sync* r nk id raw)))
              sm
              ctl-ids))
    sm))

(defn nk-force-sync-all
  [state-a nk]
  (send state-a nk-force-sync-all* nk))

(defn nk-leave-switcher-mode*
  [sm nk]
  (let [[g k] (sm-nk->current-gk sm nk)]
    (led-off nk :cycle)
    (switch-state* sm nk g k)))

(defn nk-switcher-mode*
  [sm nk]
  (if (sm-nk-switcher-mode? sm nk)
    (nk-leave-switcher-mode* sm nk)
    (nk-enter-switcher-mode* sm nk)))

(defn nk-switcher-mode
  [state-a nk]
  (send state-a nk-switcher-mode* nk))

(defn- clutch-on*
  [sm nk]
  (let [mode (if (sm-nk-switcher-mode? sm nk)
               :switcher
               :clutch)]
    (sm-nk-swap-mode sm nk mode)))

(defn- clutch-off*
  [sm nk]
  (let [mode (if (sm-nk-switcher-mode? sm nk)
               :switcher
               :controller )]
    (sm-nk-swap-mode sm nk mode)))

(defn nk-clutch-on
  [state-a nk]
  (send state-a clutch-on* nk))

(defn nk-clutch-off
  [state-a nk]
  (send state-a clutch-off* nk))

(defn nk-absolute-val-viz-on
  [state-a nk]
  (send state-a nk-absolute-val-viz-on* nk))

(defn nk-absolute-val-viz-off
  [state-map-a nk]
  (send state-map-a nk-leave-switcher-mode* nk))

(defn nk-save-state*
  [sm nk state-prom]
  (deliver state-prom (sm-nk->current-state sm nk))
  sm)

(defn nk-save-state
  [state-map-a nk]
  (let [state-prom (promise)]
    (send state-map-a nk-save-state* nk state-prom)
    @state-prom))

(defn nk-load-state*
  [sm nk new-state]
  (let [sm (sm-nk-swap-state sm nk new-state)]
    (switch-state* sm nk (sm-nk->current-gk sm nk))))

(defn nk-load-state
  [state-map-a nk new-state]
  (send state-map-a nk-load-state* nk new-state))
