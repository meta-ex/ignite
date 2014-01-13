(ns meta-ex.shader
  (:use [overtone.live])
  (:require [shadertone.tone :as t]))

(t/start-fullscreen "resources/shaders/fireball.glsl")

(t/start-fullscreen "resources/shaders/sine_dance.glsl")
(t/start-fullscreen "resources/shaders/electron.glsl")

(t/start-fullscreen "resources/shaders/spectrograph.glsl"
         ;; this puts the FFT data in iChannel0 and a texture of the
         ;; previous frame in iChannel1
                    :textures [:overtone-audio :previous-frame])

(t/start-fullscreen "resources/shaders/menger-san.glsl"
         ;; this puts the FFT data in iChannel0 and a texture of the
         ;; previous frame in iChannel1
                    )

(t/start-fullscreen "resources/shaders/zoomwave.glsl"
                    :textures [ :overtone-audio :previous-frame ])

(t/start-fullscreen "resources/shaders/wave.glsl"  :textures [ :overtone-audio ])

(t/start-fullscreen "resources/shaders/simpletex.glsl"
         :textures [:overtone-audio "resources/textures/granite.png" "resources/textures/towel.png"])

(t/stop)

(demo 5 (* (sin-osc:kr 0.3) (saw [200 101])) )

(t/start-fullscreen "resources/shaders/simplecube.glsl" :textures ["resources/textures/buddha_*.jpg"])

(defsynth vvv
  []
  (let [a (+ 300 (* 50 (sin-osc:kr (/ 1 3))))
        b (+ 300 (* 100 (sin-osc:kr (/ 1 5))))
        _ (tap "a" 60 (a2k a))
        _ (tap "b" 60 (a2k b))]
    (out 0 (pan2 (+ (sin-osc a)
                    (sin-osc b))))))
(def v (vvv))
(t/start-fullscreen "resources/shaders/vvv.glsl"
         :user-data { "iA" (atom {:synth v :tap "a"})
                      "iB" (atom {:synth v :tap "b"}) })
(kill v)
(stop)
