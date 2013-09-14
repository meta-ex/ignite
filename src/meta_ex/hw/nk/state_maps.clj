(ns meta-ex.hw.nk.state-maps
  (:use [meta-ex.lib.timed]
        [meta-ex.hw.nk.stateful-device]
        [overtone.algo.fn :only [cycle-fn]]
        [overtone.algo.scaling :only [scale-range]]
        [overtone.helpers.doc :only [fs]]
        [overtone.helpers.ref :only [swap-returning-prev!]]
        [overtone.libs.event :only [event]])
  (:require [overtone.sc.protocols :as protocols]))

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
;;                                                    :switch-bank  nil }}}))
;; Available modes: [:controller, :clutch, :switcher]

(defn- sm-b-k->state
  "Get the state for a specific key and bank"
  [sm b k]
  (get-in sm [:states b k :state]))

(defn- sm-b-k->button-id
  "Get the button-id for a specific key and bank"
  [sm b k]
  (get-in sm [:states b k :button-id]))

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

(defn- sm-nk->current-bk
  "Get the current state-map [b k] vec for a specific nk in the sm"
  [sm nk]
  (get-in sm [:nks nk :current]))

(defn- sm-nk->current-state
  "Get the current state associated with a specific nk"
  [sm nk]
  (let [[b k] (sm-nk->current-bk sm nk) ]
    (sm-b-k->state sm b k)))

(defn- sm-nk->current-bank
  "Get the current bank associated with a specific nk. Prefer bank
   in :switch-bank key if exists."
  [sm nk]
  (let [[b k]        (sm-nk->current-bk sm nk)
        switch-bank (get-in sm [:nks nk :switch-bank])]
    (or switch-bank b)))

(defn sm-nk->current-button-id
  "Returns the button-id for the current state of the nk"
  [sm nk]
  (let [[b k] (sm-nk->current-bk sm nk)]
    (get-in sm [:states b k :button-id])))

(defn- sm-nk->current-mode
  "Return the current mode for nk"
  [sm nk]
  (get-in sm [:nks nk :mode]))

(defn- sm-bk->nks
  "Get all the nks associated with a specific bk vec"
  [sm bk]
  (let [nks (:nks sm)]
    (reduce (fn [r [k v]]
              (if (= bk (:current v))
                (conj r k)
                r))
            #{}
            nks)))

(defn- sm-b->button-ids-nks
  "Return a map of button-ids to lists of nks currently controlling the state
   with that button-id for bank b"
  [sm b]
  (let [all-states-for-b (get-in sm [:states b])
        k->button-id     (reduce (fn [r [k state-info]]
                                   (conj r [k (:button-id state-info)]))
                                 #{}
                                 all-states-for-b)]
    (reduce (fn [r [k bid]]
              (let [nks (sm-bk->nks sm [b k])]
                (assoc r bid nks)))
            {}
            k->button-id)))

(defn sm-b-button-id->valid?
  "Returns true if the button-id is associated with a state in bank b."
  [sm b button-id]
  (let [available (sm-b->button-ids-nks sm b)]
    (contains? available button-id)))

(defn sm-b-button-id->k
  "Returns the state-k for state with button-id within bank b"
  [sm b button-id]
  (ffirst (filter (fn [[k v]] (= button-id (:button-id v)))
                  (get-in sm [:states b]))))

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

(defn- sm-b->valid?
  "Returns true if b is a valid bank in sm"
  [sm b]
  (contains? (:states sm) b))

(defn- sm-b-k->valid?
  "Returns true if k is a valid state key within bank b in sm"
  [sm b k]
  (and (sm-b->valid? sm b)
       (contains? (get-in sm [:states b]) k)))

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
  "Return a new sm with the current bk vec replaced"
  [sm nk b k]
  (assoc-in sm [:nks nk :current] [b k]))

(defn- sm-nk-swap-switch-bank
  "Swap the switch-bank - a key representing the new bank to switch to"
  [sm nk b]
  (assoc-in sm [:nks nk :switch-bank] b))

(defn- sm-swap-state
  "Return a new sm with the state with specific bank and k replaced"
  [sm b k new-state]
  (assoc-in sm [:states b k :state] new-state))

(defn- sm-nk-swap-mode
  "Return a new sm with the mode of the specific nk replaced"
  [sm nk mode]
  (assoc-in sm [:nks nk :mode] mode))

(defn- sm-nk-swap-state
  "Return a new sm with the state matching the nk replaced"
  [sm nk new-state]
  (let [[b k] (sm-nk->current-bk sm nk)]
    (sm-swap-state sm b k new-state)))

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
  [sm b k s button-id]
  (assoc-in sm [:states b k] {:state s
                              :button-id button-id}))

(defn- sm-add-nk
  "Add a new nk to the statemap"
  [sm nk]
  (assoc-in sm [:nks nk] {:syncs (nk-state-map false)
                          :flashers (nk-state-map nil)
                          :current nil
                          :mode :controller
                          :raw-state (nk-state-map nil)
                          :switch-bank nil}))

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

(defn nk-show-bank
  "Turn on the rec lights specific to the bank"
  [nk bank]
  (nk-rec-leds-off nk)
  (cond
   (= 0 bank)  (led-on nk :record)
   (= 2 bank)  (led-on nk :play)
   (= 4 bank)  (led-on nk :stop)
   (= 8 bank)  (led-on nk :fast-forward)
   (= 16 bank) (led-on nk :rewind)))

(defn kill-all-flashers*
  "Kill all the flashers on a specific nk"
  [sm nk]
  (let [state-map       (sm-nk->current-state sm nk)
        flashers        (sm-nk->flashers sm nk)
        flashers        (reduce (fn [r [k v]]
                                  (when (and v (live? v))
                                    (protocols/kill* v)
                                    (led-off nk k))
                                  (assoc r k nil))
                                {}
                                flashers)]
    (doseq [i (range 8)]
      (led-off nk (keyword (str "m" i))))

    (sm-nk-swap-flashers sm nk flashers))
  sm)

(declare update-syncs-and-flashers*)

(defn- update-leds*
  "Refresh a nk's leds. If in a manipulation mode:
   - updates all flashers
   - shows bank
   - turns on sync leds"
  [sm nk]
  (if (sm-nk-manipulation-mode? sm nk)
    (let [bank (sm-nk->current-bank sm nk)
          state (sm-nk->current-state sm nk)]
      (nk-show-bank nk bank)
      (reduce (fn [sm [id v]]
                (if (controller-id? id )
                  (update-syncs-and-flashers* sm nk id v)
                  sm))
              sm
              state))
    sm))

(defn- refresh*
  "Refresh a nk's leds. If in a manipulation mode:
   - kills all flashers
   - shows bank
   - turns on sync leds"
  [sm nk]
  (if  (sm-nk-manipulation-mode? sm nk)
    (let [sm (kill-all-flashers* sm nk)
          sm (update-leds* sm nk)]
      sm
    sm)))

(defn- flasher-delay
  "Calculate the flasher delay. This is a function of the 'distance'
   between the virtual val and the physical raw val. Closeness is
   defined as a shorter delay whilst distance is a longer delay"
  [val raw]
  (if (and (number? val)
           (number? raw))
    (* (+ 0.01 (Math/abs (- val raw))) delay-mul)

    ;;broken vals: choose an arbitrarily large value
    2000))

(defn- mk-flasher
  "Creates a ScheduledPattern which will flash a specific led id for nk
   with a specified delay time in ms."
  [nk id delay]
  (temporal (mk-blink-led nk id) delay))

(declare update-syncs-and-flashers*)

(defn- sm-nk-unsync-other-nks*
  "Update syncs and flashers associated with the specific bank, state
   key and id for all other registered nks in sm other than this specific nk"
  [sm nk b k id v]
  (reduce (fn [r nk]
            (update-syncs-and-flashers* r nk id v))
          sm
          (remove #{nk} (sm-bk->nks sm [b k]))))

(defn- emit-event
  "Emit the appropriate v-nanoKON2 events"
  [b k id old-state state old-val val]
  (event [:v-nanoKON2 b k :control-change id]
         :id id
         :old-state old-state
         :state state
         :old-val old-val
         :val val)

  (event [:v-nanoKON2 b k :control-change]
         :id id
         :old-state old-state
         :state state
         :old-val old-val
         :val val)

  (event [:v-nanoKON2]
         :bank b
         :key k
         :type :control-change
         :id id
         :old-state old-state
         :state state
         :old-val old-val
         :val val))

(defn- nk-update-states-range*
  "Update the state of nk's range control (i.e. a slider or pot) with
  specific id to new raw value."
  [sm nk id raw]
  (if (sm-nk-manipulation-mode? sm nk)
    (let [[b k]         (sm-nk->current-bk sm nk)
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
                          (sm-nk-unsync-other-nks* sm nk b k id new-val)
                          sm)]

      (when (and (not was-synced?) synced?)
        (led-on nk flasher-id)
        (led-off nk warmer-id))

      (let [flashers (if synced?
                       (do
                         (emit-event b k id old-state state val new-val)
                         (when flasher (protocols/kill* flasher))
                         (assoc flashers flasher-id nil)
                         )

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
  "Switch the state of the nk to state with k in bank b"
  [sm nk b k]
  (if (sm-b-k->valid? sm b k)
    (do
      (nk-smr-leds-off nk)

      (let [sm         (kill-all-flashers* sm nk)
            latest-raw (sm-nk->raw-state sm nk)
            state      (sm-b-k->state sm b k)
            syncs      (sm-nk->syncs sm nk)
            syncs      (reduce (fn [r [id v]]
                                 (let [synced? (= (get state id)
                                                  (get latest-raw id))]

                                   (when synced?
                                     (led-on nk (controller-id->sync-led-id id)))
                                   (assoc r id synced?)))
                               {}
                               syncs)]
        (nk-show-bank nk b)
        (-> sm
            (sm-nk-swap-current nk b k)
            (sm-nk-swap-syncs nk syncs)
            (sm-nk-swap-switch-bank nk nil)
            (sm-nk-swap-mode nk :controller))))
    sm))

(defn sm-nk-kill-flasher*
  "Kill flasher with id on nk"
  [sm nk id]
  (let [flashers (sm-nk->flashers sm nk)
        flasher  (get flashers id)
        flashers (assoc flashers id nil)]

    (when flasher
      (protocols/kill* flasher))

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
   (let [[b k]           (sm-nk->current-bk sm nk)
         old-state       (sm-nk->current-state sm nk)
         state           (assoc old-state id raw)
         syncs           (sm-nk->syncs sm nk)
         syncs           (assoc syncs id true)
         sm              (sm-nk-kill-flasher* sm nk (controller-id->sync-led-id id))
         sm              (sm-nk-unsync-other-nks* sm nk b k id raw)
         val             (get old-state id)]

     (led-on nk (controller-id->sync-led-id id))
     (led-off nk (controller-id->warmer-led-id id))

     (emit-event b k id old-state state val raw)

     (-> sm
         (sm-nk-swap-state nk state)
         (sm-nk-swap-syncs nk syncs)))

   :else sm))

(defn- bank-button-id->bank
  "Use powers of two to eventually support binary notation across the
   record row of buttons."
  [id]
  (cond
   (= :record id) 0
   (= :play id) 2
   (= :stop id) 4
   (= :fast-forward id) 8
   (= :rewind id) 16))

(defn- bank-button?
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
  (let [b         (sm-nk->current-bank sm nk )
        raw-state (sm-nk->raw-state sm nk)]
    (cond

     ;; switch bank (within switcher mode)
     (and (= 1.0 raw)
          (sm-nk-switcher-mode? sm nk)
          (bank-button? id))
     (let [b (bank-button-id->bank id)]
       (-> sm
           (sm-nk-swap-switch-bank nk b)
           (nk-enter-switcher-mode* nk)))

     ;; switch state
     (and (= 0.0 raw)
          (sm-nk-switcher-mode? sm nk)
          (sm-b-button-id->valid? sm b id))
     (let [state-k (sm-b-button-id->k sm b id)]
       (led-off nk :cycle)
       (-> sm
           (kill-all-flashers* nk)
           (switch-state* nk b state-k)
           (sm-nk-swap-switch-bank nk nil)
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
  (if  (sm-nk-manipulation-mode? sm nk)
    (let [raw-state  (sm-nk->raw-state sm nk)
          raw        (get raw-state id)
          old-syncs  (sm-nk->syncs sm nk)
          synced?    (= v raw)
          syncs      (assoc old-syncs id synced?)
          flashers   (sm-nk->flashers sm nk)
          flasher-id (controller-id->sync-led-id id)
          warmer-id  (controller-id->warmer-led-id id)
          flasher    (get flashers flasher-id)]
      (led-off nk warmer-id)
      (cond
       flasher       (delay-set! flasher (flasher-delay v raw))
       synced?       (led-on nk (controller-id->sync-led-id id))
       (not synced?) (led-off nk (controller-id->sync-led-id id)))

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
  "Switch nk to state matching b state-k"
  [state-a nk b state-k]
  (send state-a switch-state* nk b state-k))

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
          (str "State value must be a number between 0 and 1 inclusively, got: " v)))

(defn ensure-valid-bank!
  [k]
  (assert (and (integer? k)
               (<= 0 k)
               (<= k 16))
          (str "State bank must be a number between 0 and 16 inclusively, got: " k)))

;; Fix me
;; (defn update-state*
;;   [sm b k v]
;;   (if (contains? (get-in sm [:states b]) k)
;;     (let [old-state (sm-b-k->state sm b k)
;;           state     (assoc old-state k v)
;;           button-id (sm-b-k->button-id sm b k)

;;           []
;;           sm        (reduce (fn [r nk]
;;                               (update-syncs-and-flashers* r nk button-id v))
;;                             sm
;;                             (sm-bk->nks sm b k))]
;;       (sm-swap-state sm k state))
;;     sm))

;; (defn update-state
;;   "update the state associated with bank b, state key k to value v"
;;   [state-a b k v]
;;   (ensure-valid-val! v)

;;   (send state-a update-state* b state-k v))

(defn- add-nk*
  [sm nk]
  (sm-add-nk sm nk))

(defn add-nk
  "add a new nk to the statemap agent"
  [state-a nk]
  (send state-a add-nk* nk))

(defn- add-state*
  [sm bank state-k state button-id]
  (sm-add-state sm bank state-k state button-id))

(defn add-state
  ([state-a bank button-id init-val-or-state-map]
     (add-state state-a bank button-id button-id init-val-or-state-map))
  ([state-a bank state-k button-id init-val-or-state-map]
     (ensure-valid-val! init-val-or-state-map)
     (ensure-valid-bank! bank)
     (let [state (if (number? init-val-or-state-map)
                   (nk-state-map init-val-or-state-map)
                   init-val-or-state-map)]
       (send state-a add-state* bank state-k state button-id)
       (str "Added state " bank ", " state-k ", " button-id))))

(defn nk-enter-switcher-mode*
  [sm nk]
  (let [sm             (kill-all-flashers* sm nk)
        sm             (sm-nk-swap-mode sm nk :switcher)
        b              (sm-nk->current-bank sm nk)
        available      (sm-b->button-ids-nks sm b)
        flashers       (sm-nk->flashers sm nk)
        curr-button-id (sm-nk->current-button-id sm nk)]

    (nk-smr-leds-off nk)
    (nk-show-bank nk b)
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
        bank    (sm-nk->current-bank sm nk)]

    (nk-smr-leds-off nk)
    (nk-show-bank nk bank)
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
  (let [[b k] (sm-nk->current-bk sm nk)]
    (led-off nk :cycle)
    (switch-state* sm nk b k)))

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

(defn nk-save-current-state
  [state-map-a nk]
  (sm-nk->current-state @state-map-a nk))

(defn nk-replace-current-state*
  [sm nk new-state]
  (let [sm (sm-nk-swap-state sm nk new-state)]
    (switch-state* sm nk (sm-nk->current-bk sm nk))))

(defn emit-events-on-state-diff
  [b k old-state new-state]
  (doseq [[id v] new-state]
    (when (controller-id? id )
      (let [old-v (get old-state id)]
        (emit-event b
                    k
                    id
                    old-state
                    new-state
                    old-v
                    v)))))

(defn valid-state?
  "Returns true if state is a valid nk state map"
  [state]
  (try
    (reduce (fn [res [k v]]
              (and res
                   (valid-control-id? k)
                   (number? v)
                   (if (button? k)
                     (or
                      (= v 0)
                      (= v 1))
                     (and
                      (<= v 1)
                      (>= v 0)))))
            true
            state)
    (catch Exception e
      false)))

(defn valid-bank-states?
  "Returns true if bank-states is a valid set of bank states"
  [bank-states]
  (try
    (reduce (fn [res [k v]]
              (and res
                   (button? (:button-id v))
                   (valid-state? (:state v))))
            true
            bank-states)
    (catch Exception e
      false)))

(defn nk-replace-current-state
  [state-map-a nk new-state]
  (send state-map-a nk-replace-current-state* nk new-state))

(defn save-state
  [state-map-a b k]
  (let [sm @state-map-a]
    (sm-b-k->state sm b k)))

(defn replace-state
  [state-map-a b k state]
  (assert (valid-state? state) (str "Invalid state" ) )
  (send state-map-a
        (fn [sm]
          (let [old-state (sm-b-k->state b k) ]
            (emit-events-on-state-diff b k old-state state))
          (sm-swap-state b k state)))
  :replaced)

(defn save-state-by-button-id
  [state-map-a b button-id]
  (let [k (sm-b-button-id->k)]
    (save-state state-map-a b k)))

(defn replace-state-by-button-id
  [state-map-a b button-id state]
  (let [k (sm-b-button-id->k)]
    (replace-state state-map-a b k state)))

(defn save-bank-states
  [state-map-a b]
  (let [sm @state-map-a]
    (get-in sm [:states b] {})))

(defn- sm-b-k-update-all-nks*
  "Updates the smr leds on all nks associated with the specified bank
   and state key:

   - unsync if synced and no longer same vals
   - sync if unsynced and same vals
   - update flashers
   "
  [sm b k]
  (let [nks (sm-bk->nks sm [b k])]
    (reduce (fn [r nk]
              (update-leds* r nk) )
            sm
            nks)))

(defn load-bank-states
  "Load in previously saved bank states to the specified bank. This
  works by matching the states by :button-id not key. Keys are left
  unaffected, and states with matching :button-id are updated to the
  states found in new-banks-states."
  [state-map-a b new-bank-states]
  (assert (valid-bank-states? new-bank-states) "Invalid bank states")
  (send state-map-a
        (fn [sm]
          (let [old-states (get-in sm [:states b])]
            (reduce (fn [sm [k info]]
                      (let [new-state-btn-id (:button-id info)
                            old-state-k      (ffirst (filter (fn [[o-k o-info]]
                                                              (= new-state-btn-id
                                                                 (:button-id o-info)))
                                                            old-states))
                            old-state        (get old-states old-state-k)
                            new-state        (:state info)]
                        (if old-state-k
                          (do
                            (emit-events-on-state-diff b k old-state new-state)
                            (-> sm
                                (sm-swap-state b k new-state)
                                (sm-b-k-update-all-nks* b k)))
                          sm)))
                    sm
                    new-bank-states))))
  :replaced)

(defn list-banks
  [state-map-a]
  (keys (:states @state-map-a)))

(defn list-bank-states
  [state-map-a bank]
  (keys  (get-in @state-map-a [:states bank])))
