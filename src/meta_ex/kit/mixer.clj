(ns meta-ex.kit.mixer
  (:use [overtone.live]
        [overtone.helpers.lib :only [uuid]])
  (:require [meta-ex.kit.timing :as tim]))

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

(defsynth meta-mix [rev-mix 0
                    delay-decay 0
                    dec-mix 0
                    wobble-mix 0
                    delay-mix 0
                    hpf-mix 0
                    wobble-factor 0
                    amp 1.5
                    rev-damp 0
                    rev-room 0
                    samp-rate 0
                    bit-rate 0
                    delay-rate 0
                    hpf-freq 2060
                    hpf-rq 1
                    pan 0


                    in-bus 10
                    delay-reset-trig [0 :kr]
                    out-bus 0]
  (let [delay-buf (local-buf (* 2 44100))
        src       (in:ar in-bus 2)
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

        src       (+ (* (- 1 wobble-mix) src)
                     (* wobble-mix (wobble src wobble-factor)))
        ]

    (buf-wr:ar [(mix src)] delay-buf pos :loop true)
    (out out-bus (pan2 (* amp src) pan))))

;; (defsynth meta-master-mix [amp 1.5
;;                            in-bus 0
;;                            out-bus 0]
;;   (let [src (in:ar in-bus 2)]))

(defsynth mixer-sin-control
  [out-bus 0

   freq-mul-0 1
   phase-shift-0 0
   mul-0 1
   add-0 0

   freq-mul-1 1
   phase-shift-1 0
   mul-1 1
   add-1 0


   freq-mul-2 1
   phase-shift-2 0
   mul-2 1
   add-2 0

   freq-mul-3 1
   phase-shift-3 0
   mul-3 1
   add-3 0

   freq-mul-4 1
   phase-shift-4 0
   mul-4 1
   add-4 0

   freq-mul-5 1
   phase-shift-5 0
   mul-5 1
   add-5 0

   freq-mul-6 1
   phase-shift-6 0
   mul-6 1
   add-6 0

   freq-mul-7 1
   phase-shift-7 0
   mul-7 1
   add-7 0

   freq-mul-8 1
   phase-shift-8 0
   mul-8 1
   add-8 0

   freq-mul-9 1
   phase-shift-9 0
   mul-9 1
   add-9 0

   freq-mul-10 1
   phase-shift-10 0
   mul-10 1
   add-10 0

   freq-mul-11 1
   phase-shift-11 0
   mul-11 1
   add-11 0

   freq-mul-12 1
   phase-shift-12 0
   mul-12 1
   add-12 0

   freq-mul-13 1
   phase-shift-13 0
   mul-13 1
   add-13 0

   freq-mul-14 1
   phase-shift-14 0
   mul-14 1
   add-14 0

   freq-mul-15 1
   phase-shift-15 0
   mul-15 1
   add-15 0

   ]

  (let [clk (in:kr tim/pi-x-b)]
    (out:kr out-bus [(mul-add:kr (sin  (mul-add clk freq-mul-0 phase-shift-0)) mul-0 add-0)
                     (mul-add:kr (sin  (mul-add clk freq-mul-1 phase-shift-1)) mul-1 add-1)
                     (mul-add:kr (sin  (mul-add clk freq-mul-2 phase-shift-2)) mul-1 add-2)
                     (mul-add:kr (sin  (mul-add clk freq-mul-3 phase-shift-3)) mul-1 add-3)
                     (mul-add:kr (sin  (mul-add clk freq-mul-4 phase-shift-4)) mul-1 add-4)
                     (mul-add:kr (sin  (mul-add clk freq-mul-5 phase-shift-5)) mul-1 add-5)
                     (mul-add:kr (sin  (mul-add clk freq-mul-6 phase-shift-6)) mul-1 add-6)
                     (mul-add:kr (sin  (mul-add clk freq-mul-7 phase-shift-7)) mul-1 add-7)
                     ;; (mul-add:kr (sin  (mul-add clk freq-mul-8 phase-shift-8)) mul-1 add-8)
                     ;; (mul-add:kr (sin  (mul-add clk freq-mul-9 phase-shift-9)) mul-1 add-9)
                     ;; (mul-add:kr (sin  (mul-add clk freq-mul-10 phase-shift-10)) mul-1 add-10)
                     ;; (mul-add:kr (sin  (mul-add clk freq-mul-11 phase-shift-11)) mul-1 add-11)
                     ;; (mul-add:kr (sin  (mul-add clk freq-mul-12 phase-shift-12)) mul-1 add-12)
                     ;; (mul-add:kr (sin  (mul-add clk freq-mul-13 phase-shift-13)) mul-1 add-13)
                     ;; (mul-add:kr (sin  (mul-add clk freq-mul-14 phase-shift-14)) mul-1 add-14)
                     ;; (mul-add:kr (sin  (mul-add clk freq-mul-15 phase-shift-15)) mul-1 add-15)

                     ])))


(def nano2-fns {:slider0 (fn [v mixer-g] (ctl mixer-g :rev-mix v))
                :slider1 (fn [v mixer-g] (ctl mixer-g :delay-decay v))
                :slider2 (fn [v mixer-g] (ctl mixer-g :dec-mix v))
                :slider3 (fn [v mixer-g] (ctl mixer-g :wobble-mix v))
                :slider4 (fn [v mixer-g] (ctl mixer-g :delay-mix v))
                :slider5 (fn [v mixer-g] (ctl mixer-g :hpf-mix v))
                :slider6 (fn [v mixer-g] (ctl mixer-g :wobble-factor (scale-range v 0 1 0 15)))
                :slider7 (fn [v mixer-g] (ctl mixer-g :amp (scale-range v 0 1 0 3)))
                :pot0    (fn [v mixer-g] (ctl mixer-g :rev-damp v))
                :pot1    (fn [v mixer-g] (ctl mixer-g :rev-room v))
                :pot2    (fn [v mixer-g] (ctl mixer-g :samp-rate (* 22000 v)))
                :pot3    (fn [v mixer-g] (ctl mixer-g :bit-rate (* 32 v)))
                :pot4    (fn [v mixer-g] (ctl mixer-g :delay-rate v))
                :pot5    (fn [v mixer-g] (ctl mixer-g :hpf-freq (+ 60 (* 2000 v))))
                :pot6    (fn [v mixer-g] (ctl mixer-g :hpf-rq v))
                :pot7    (fn [v mixer-g] (ctl mixer-g :pan (scale-range v 0 1 -1 1)))

})

(def nano2-fns {:slider0 (fn [v sin-ctl-s] (ctl sin-ctl-s :add-0 v))
                :slider1 (fn [v sin-ctl-s] (ctl sin-ctl-s :add-1 v))
                :slider2 (fn [v sin-ctl-s] (ctl sin-ctl-s :add-2 v))
                :slider3 (fn [v sin-ctl-s] (ctl sin-ctl-s :add-3 v))
                :slider4 (fn [v sin-ctl-s] (ctl sin-ctl-s :add-4 v))
                :slider5 (fn [v sin-ctl-s] (ctl sin-ctl-s :add-5 v))
                :slider6 (fn [v sin-ctl-s] (ctl sin-ctl-s :add-6 (scale-range v 0 1 0 15)))
                :slider7 (fn [v sin-ctl-s] (ctl sin-ctl-s  :add-7 (scale-range v 0 1 0 3)))
                :pot7    (fn [v sin-ctl-s] (ctl sin-ctl-s :pan (scale-range v 0 1 -1 1)))

                :pot0    (fn [v sin-ctl-s] (ctl sin-ctl-s :rev-damp v))
                :pot1    (fn [v sin-ctl-s] (ctl sin-ctl-s :rev-room v))
                :pot2    (fn [v sin-ctl-s] (ctl sin-ctl-s :samp-rate (* 22000 v)))
                :pot3    (fn [v sin-ctl-s] (ctl sin-ctl-s :bit-rate (* 32 v)))
                :pot4    (fn [v sin-ctl-s] (ctl sin-ctl-s :delay-rate v))
                :pot5    (fn [v sin-ctl-s] (ctl sin-ctl-s :hpf-freq (+ 60 (* 2000 v))))
                :pot6    (fn [v sin-ctl-s] (ctl sin-ctl-s :hpf-rq v))})

(defn- mk-mixer
  [event-k mixer-g out-bus]
  (let [in-bus    (audio-bus 2)
        ctl-bus   (control-bus 8)
        live?     (atom true)
        mixer     (meta-mix [:tail mixer-g] :in-bus in-bus :out-bus out-bus)
        sin-ctl   (mixer-sin-control [:head mixer-g] :out-bus ctl-bus)
        handler-k (uuid)]
        (node-map-n-controls mixer :rev-mix ctl-bus 8)
    (println "registering a mixer listening on " event-k)
    (on-latest-event event-k
                     (fn [msg]
                       (let [id  (:id msg)
                             val (:val msg)]
                         (if-let [f (get nano2-fns id)]
                           (f val mixer)
                           (println "unbound: " note))))
                     handler-k)
    (on-node-destroyed mixer
                       (fn [_]
                         (remove-handler handler-k)
                         (swap! korg-nano-kontrol-mixers dissoc event-k)
                         (reset! live? false)))

    (with-meta {:mixer-g     mixer-g
                :mixer       mixer
                :handler-key handler-k
                :in-bus      in-bus
                :event-key   event-k
                :live?       live?
                :sin-ctl     sin-ctl}
      {:type ::mixer})))


(defn add-mixer
  ([event-k]
     (add-mixer event-k (foundation-safe-post-default-group)))
  ([event-k tgt-g]
     (add-mixer event-k tgt-g 0))
  ([event-k tgt-g out-bus]
     (let [mixers (swap! korg-nano-kontrol-mixers
                         (fn [mixers]
                           (when (contains? mixers event-k)
                             (throw (Exception.
                                     (str "Korg Nano Kontrol Mixer with event key "
                                          event-k " already exists."))))

                           (assoc mixers event-k (mk-mixer event-k tgt-g out-bus))))]
       (get mixers event-k))))

(defn kill-mixer [mixer]
  (remove-handler (:handler-key mixer))
  (swap! korg-nano-kontrol-mixers dissoc (:event-key mixer))
  (with-inactive-modification-error :silent
    (kill (:mixer mixer))))

(defn add-nk-mixer
  ([k]
     (add-mixer [:nanoKON2 k :control-change]))
  ([k tgt-g]
     (add-mixer [:nanoKON2 k :control-change] tgt-g))
  ([k tgt-g out-bus]
     (add-mixer [:nanoKON2 k :control-change] tgt-g out-bus)))

(defn mx
  [k]
  (:mixer-g (get @korg-nano-kontrol-mixers k)))

(defn nkmx
  [k]
  (:in-bus (get @korg-nano-kontrol-mixers [:nanoKON2 k :control-change])))

(defn nkmx-synth
  [k]
  (:mixer (get @korg-nano-kontrol-mixers [:nanoKON2 k :control-change])))
