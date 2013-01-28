(ns meta-ex.timing
  (:use [overtone.core]
        [overtone.gui.scope]))

(pscope :control-bus cb1)
(pscope :control-bus cb2)
(pscope :control-bus cb3)
(pscope :buf d)

(buffer? (buffer-info 0))
(keys d)(:id :size :n-channels :rate :allocated-on-server :path :args :name :n-samples :duration :rate-scale)
(keys (buffer-info 0))(:id :size :n-channels :rate :n-samples :rate-scale :duration)

(defonce timing-g (group "M-x timing" :tgt (foundation-safe-pre-default-group)))

(defonce cb1 (control-bus))
(defonce cb2 (control-bus))
(defonce cb3 (control-bus))

(defsynth trigger [rate 100 out-bus 0]
  (out:kr out-bus (impulse:kr rate)))

(defsynth counter [in-bus 0 out-bus 0]
  (out:kr out-bus (pulse-count:kr (in:kr in-bus))))

(defsynth divider [div 32 in-bus 0 out-bus 0]
  (out:kr out-bus (pulse-divider (in:kr in-bus) div)))

(defsynth root-saw [rate 10 out-bus 0]
  (out:kr out-bus (lf-saw:kr rate)))

(defsynth saw->sin [in-bus 0 out-bus 0]
  (let [sig (in:kr in-bus )
        sig (sin sig)]
    (out:kr out-bus sig)))

(defsynth mul-adder [mul 1 add 0 in-bus 0 out-bus 0]
  (let [sig (in:kr in-bus )
        sig (mul-add sig mul add)]
    (out:kr out-bus sig)))


(defsynth noise [amp 1 freq 440 pan 0]
  (out 0 (pan2 (* amp (sin-osc freq)) pan)))

(defonce root-sw (root-saw :tgt timing-g :out-bus cb1 :rate 1))

(def my-sin (saw->sin :in-bus cb1 :out-bus cb2))
(def n (noise))
(def m-adder (mul-adder :in-bus cb1 :out-bus cb2 :mul 1 :add 0))

(ctl m-adder :add 0)


(node-map-controls* n [:amp cb1])
(use 'overtone.helpers.lib)
(map idify (stringify [1]))
(stop)
(sc-debug-on)

(demo 10 (pan2 (sin-osc (mul-add:kr  (lf-saw:kr 1) 20 400 ))))

(stop)
(pscope :bus 1)


(def d (sample (freesound-path 777)))
(pscope :buf d)
(to-sc-id d)
(buffer-size d)
(buffer-info d)

(to-sc-id d)
(buffer-info 0)
