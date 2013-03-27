(ns meta-ex.hw.fonome
  (:use [overtone.music.time :only [now]]
        [overtone.libs.event :only [event]]))

;; A fonome is a virtual monome or grid which can be mapped onto a real
;; monome. Typically a virtual monome is smaller than the real monome
;; and the real monome may have many virtual monomes docked onto it.

(defonce fonomes (atom {}))
(def history-size 1000)

(defn mk-fonome
  [id width height]
  (let [state  (agent {:width   width
                       :height  height
                       :buttons {}
                       :leds    {}
                       :history []
                       :id      id})
        fonome (with-meta {:width  width
                           :height height
                           :state  state
                           :id     id}
                 {:type ::fonome})]
    (swap! fonomes (fn [fs]
                     (if (contains? fs id)
                       (throw (Exception. (str "A fonome with id " id " already exists.")))
                       (assoc fs id fonome))))
    fonome))

(defn rm-fonome
  [id]
  (swap! fonomes dissoc id))

(defmethod print-method ::fonome [fonome w]
  (.write w (format "#<fonome: %s [%dx%d]>" (:id fonome) (:width fonome) (:height fonome))))

(defn ensure-valid-coords!
  [f x y]
  (when-not (and (number? x)
                 (number? y))
    (throw (Exception. (str "Coords for fonome should be numbers. Got: " (with-out-str (pr [x y]))))))
  (let [w (:width f)
        h (:height f)
        x (int x)
        y (int y)]
    (when (or (< x 0)
              (< y 0)
              (>= x w)
              (>= y h))
      (throw (Exception. (str "Invalid key range used as an index for fonome. Expected coords with x in the range 0 -> " (dec w) ", and y 0 -> " (dec h) ", got: [" x ", " y "]"))))))

(defn- ensure-fonome!
  [f]
  (when-not (= (type f) ::fonome)
    (throw (Exception. (str "Was expecting a fonome. Found a " (type f))))))

(defn- led-on*
  [s f x y]
  (let [old-leds    (:leds s)
        new-leds    (assoc old-leds [x y] true)
        s           (assoc s :leds new-leds)
        e           [(now) :led-on x y (dissoc s :history :id :width :height)]
        new-history (cons e (take history-size (:history s)))
        event-msg   {:state    s
                     :x        x
                     :y        y
                     :fonome   f
                     :old-leds old-leds
                     :new-leds new-leds}]
    (event [:fonome :led-on (:id s) x y] event-msg)
    (event [:fonome :led-on (:id s)] event-msg)
    (event [:fonome :led-change (:id s)] event-msg)
    (assoc s :history new-history)))

(defn- led-off*
  [s f x y]
  (let [old-leds    (:leds s)
        new-leds    (assoc old-leds [x y] false)
        s           (assoc s :leds new-leds)
        e           [(now) :led-off x y (dissoc s :history :id :width :height)]
        new-history (cons e (take history-size (:history s)))
        event-msg   {:state    s
                     :x        x
                     :y        y
                     :fonome   f
                     :old-leds old-leds
                     :new-leds new-leds}]
    (event [:fonome :led-off (:id s) x y] event-msg)
    (event [:fonome :led-off (:id s)] event-msg)
    (event [:fonome :led-change (:id s)] event-msg)
    (assoc s :history new-history)))

(defn- clear*
  [s f]

  (let [old-leds    (:leds s)
        new-leds    {}
        s           (assoc s :leds new-leds)
        e           [(now) :clear (dissoc s :history :id :width :height)]
        new-history (cons e (take history-size (:history s)))
        event-msg   {:state    s
                     :fonome   f
                     :old-leds old-leds
                     :new-leds new-leds}]
    (event [:fonome :clear (:id s)] event-msg)
    (event [:fonome :led-change (:id s)] event-msg)
    (assoc s :history new-history)))

(defn- toggle-led*
  [s f x y]
  (if (get (:leds s) [x y])
    (led-off* s f x y)
    (led-on* s f x y)))

(defn- press*
  [s f x y]
  (let [s           (assoc s :buttons (assoc (:buttons s) [x y] true))
        e           [(now) :button-press x y (dissoc s :history :id :width :height)]
        new-history (cons e (take history-size (:history s)))
        event-msg   {:state  s
                     :x      x
                     :y      y
                     :fonome f}]
    (event [:fonome :press (:id s) x y] event-msg)
    (event [:fonome :press (:id s)] event-msg)
    (assoc s :history new-history)))

(defn- release*
  [s f x y]
  (let [s           (assoc s :buttons (assoc (:buttons s) [x y] false))
        e           [(now) :button-release x y (dissoc s :history :id :width :height)]
        new-history (cons e (take history-size (:history s)))
        event-msg   {:state  s
                     :x      x
                     :y      y
                     :fonome f}]
    (event [:fonome :release (:id s) x y] event-msg)
    (event [:fonome :release (:id s)] event-msg)
    (assoc s :history new-history)))

(defn led-on
  [f x y]
  (ensure-fonome! f)
  (ensure-valid-coords! f x y)
  (send (:state f) led-on* f x y)
  f)

(defn led-off
  [f x y]
  (ensure-fonome! f)
  (ensure-valid-coords! f x y)
  (send (:state f) led-off* f x y)
  f)

(defn led-change
  [f x y led-state]
  (ensure-fonome! f)
  (if (or (true? led-state)
          (= 1 led-state)
          (= :on led-state))
    (send (:state f) led-on* f x y)
    (send (:state f) led-off* f x y))
  f)

(defn toggle-led
  [f x y]
  (ensure-fonome! f)
  (ensure-valid-coords! f x y)
  (send (:state f) toggle-led* f x y))

(defn press
  [f x y]
  (ensure-fonome! f)
  (ensure-valid-coords! f x y)
  (send (:state f) press* f x y)
  f)

(defn release
  [f x y]
  (ensure-fonome! f)
  (ensure-valid-coords! f x y)
  (send (:state f) release* f x y)
  f)

(defn led
  [f x y]
  (ensure-fonome! f)
  (ensure-valid-coords! f x y)
  (let [s @(:state f)]
    (get (:leds s) [x y])))

(defn button
  [f x y]
  (ensure-fonome! f)
  (ensure-valid-coords! f x y)
  (let [s @(:state f)]
    (get (:buttons s) [x y])))

(defn history
  [f]
  (ensure-fonome! f)
  (:history @(:state f)))

(defn led-state
  [f]
  (ensure-fonome! f)
  (:leds @(:state f)))

(defn clear
  [f]
  (ensure-fonome! f)
  (println "before: " (:leds @(:state f)))
  (send (:state f) clear* f)
  (println "finished: " (:leds @(:state f)))
  f)
