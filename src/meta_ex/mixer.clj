(ns meta-ex.mixer
  (:use [overtone.live]
        [overtone.helpers.lib :only [uuid]]))

(defonce korg-nano-kontrol-mixers
  (atom {}))

(defcgen wobble
  "wobble an input src"
  [src {:doc "input source"}
   wobble-factor {:doc "num wobbles per second"}]
  (:ar
   (let [sweep (lin-exp (lf-tri wobble-factor) -1 1 40 3000)
         wob   (lpf src sweep)
         wob   (* 0.8  wob)
         wob   (+ wob (bpf wob 1500 2))]
     (+ wob (* 0.2 (g-verb wob 9 0.7 0.7))))))

(defsynth meta-mix [amp 1.5
                    pan 0
                    in-bus 10
                    rev-mix 0
                    delay-decay 0
                    dec-mix 0
                    wobble-mix 0
                    delay-mix 0
                    hpf-mix 0
                    wobble-factor 0
                    rev-damp 0
                    rev-room 0
                    samp-rate 0
                    bit-rate 0
                    delay-rate 0
                    hpf-freq 1060
                    hpf-rq 0
                    delay-reset-trig [0 :kr]
                    lpf-mix 0
                    lpf-freq 100
                    delay-buf 0
                    out-bus 0]
  (let [src       (in:ar in-bus 2)
        samps     (buf-frames:kr delay-buf)
        pos       (phasor:ar delay-reset-trig 1 0 (* delay-rate samps))
        old       (buf-rd:ar 1 delay-buf pos :loop true)
        delay-sig (+ src (* delay-decay old))

        src       (+ (* (- 1 delay-mix) src)
                     (* delay-mix delay-sig))

        src       (+ (* (- 1 rev-mix) src)
                     (* rev-mix (free-verb src :mix 1 :room rev-room :damp rev-damp)))

        src       (+ (* (- 1 dec-mix) src)
                     (* dec-mix (decimator src samp-rate bit-rate)))

        src       (+ (* (- 1 hpf-mix) src)
                     (* hpf-mix (normalizer (rhpf src hpf-freq hpf-rq))))

        src       (+ (* (- 1 lpf-mix) src)
                     (* lpf-mix (lpf src lpf-freq)))

        src       (+ (* (- 1 wobble-mix) src)
                     (* wobble-mix (wobble src wobble-factor)))

        ]

    (buf-wr:ar [(mix src)] delay-buf pos :loop true)
    (out out-bus (pan2 (* amp src) pan))))

(def nano2-fns {:slider0 (fn [v mixer-g] (ctl mixer-g :rev-mix v))
                :slider1 (fn [v mixer-g] (ctl mixer-g :delay-decay v))
                :slider2 (fn [v mixer-g] (ctl mixer-g :dec-mix v))
                :slider3 (fn [v mixer-g] (ctl mixer-g :wobble-mix v))
                :slider4 (fn [v mixer-g] (ctl mixer-g :delay-mix v))
                :slider5 (fn [v mixer-g] (ctl mixer-g :hpf-mix v))
                :slider6 (fn [v mixer-g] (ctl mixer-g :wobble-factor (scale-range v 0 1 0 15)))
                :slider7 (fn [v mixer-g] (ctl mixer-g :amp (scale-range v 0 1 0 3)))
                :pot7    (fn [v mixer-g] (ctl mixer-g :pan (scale-range v 0 1 -1 1)))

                :pot0    (fn [v mixer-g] (ctl mixer-g :rev-damp v))
                :pot1    (fn [v mixer-g] (ctl mixer-g :rev-room v))
                :pot2    (fn [v mixer-g] (ctl mixer-g :samp-rate (* 22000 v)))
                :pot3    (fn [v mixer-g] (ctl mixer-g :bit-rate (* 32 v)))
                :pot4    (fn [v mixer-g] (ctl mixer-g :delay-rate v))
                :pot5    (fn [v mixer-g] (ctl mixer-g :hpf-freq (+ 60 (* 2000 v))))
                :pot6    (fn [v mixer-g] (ctl mixer-g :hpf-rq v))})

(defn- mk-mixer
  [event-k mixer-g]
  (let [bufff       (buffer (* 2 44100))
        in-bus      (audio-bus 2)
        mixer       (meta-mix :target mixer-g :in-bus in-bus :delay-buf bufff)
        handler-k (uuid)]
    (println "registering a mixer listening on " event-k)
    (on-latest-event event-k
                     (fn [msg]
                       (let [id  (:id msg)
                             val (:val msg)]
                         (if-let [f (get nano2-fns id)]
                           (f val mixer)
                           (println "unbound: " note))))
                     handler-k)
    {:bufff       bufff
     :mixer-g     mixer-g
     :mixer       mixer
     :handler-key handler-k
     :in-bus      in-bus
     :event-key   event-k}))


(defn add-mixer
  ([event-k]
     (add-mixer event-k (foundation-safe-post-default-group)))
  ([event-k tgt-g]
     (let [mixers (swap! korg-nano-kontrol-mixers
                         (fn [mixers]
                           (when (contains? mixers event-k)
                             (throw (Exception.
                                     (str "Korg Nano Kontrol Mixer with event key "
                                          event-k " already exists."))))

                           (assoc mixers event-k (mk-mixer event-k tgt-g))))]
       (get mixers event-k))))

(defn add-nk-mixer
  ([k]
     (add-mixer [:nanoKON2 k :control-change]))
  ([k tgt-g]
     (add-mixer [:nanoKON2 k :control-change] tgt-g)))

(defn mx
  [k]
  (:mixer-g (get @korg-nano-kontrol-mixers k)))

(defn nkmx
  [k]
  (:in-bus (get @korg-nano-kontrol-mixers [:nanoKON2 k :control-change])))

(defn nkmx-synth
  [k]
  (:mixer (get @korg-nano-kontrol-mixers [:nanoKON2 k :control-change])))
