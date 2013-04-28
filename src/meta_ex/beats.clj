(ns meta-ex.beats
  (:use [overtone.core]
;;        [meta-ex rhythm synths]
        [meta-ex.kit.mixer]
        ))

(defonce beats-bus (audio-bus 2 "beats bus"))

;;(kill (:id synths))


(do
  (def mixers (group "beat-mixers" :tail))
  (def synths (group "beat-synths" :head)))


(defn kill-beats
  []
  (kill synths)
  (kill mixers))

;;(kill-beats)


(defonce bar-dur (atom 2000))
(def _ nil)
(def X true)



(defn freesound-sampler
  [freesound-id]
  (fn play-sample
    ([] (play-sample {}))
    ([args]
       (sample-player (sample (freesound-path freesound-id)) [:head synths] (assoc args :out-bus 0)))))

(do
  (def ring-hat (freesound-sampler 12912))
  (def wind     (freesound-sampler 34338))
  (def kick1     (freesound-sampler 777))
  (def kick     (freesound-sampler 30669))
  (def snare    (freesound-sampler 26903))
  (def tom      (freesound-sampler 147418))
  (def boom     (freesound-sampler 33637))
  (def subby    (freesound-sampler 25649))
  (def lp (freesound-sampler 39711))
  (def crash (freesound-sampler 26884) )
  (def stick (freesound-sampler 437))
  (def short-snare (freesound-sampler 816)))

;;(short-snare)
;;(stick)
;;(crash)
;;(lp)
;;(subby)
;;(boom {:vol 5})
;;(wind)
;;(ring-hat)
;;(kick )
(kick1 )
;;(tom)
;;(wind)

(def patterns* (atom   {:ring-hat [ring-hat [[_]]]
                        :wind [wind [[_]]]
                        :tom [tom [[_]]]
                        :kick [kick [[_]]]
                        :s-snare [short-snare [[_]]]
                        :stick [stick [[_]]]
                        :crash [crash [[_]]]
                        :lp    [lp [[_]]]
                        :subby [subby [[_]]]
                        :boom  [boom [[_]]]}))

(defn update-pat!
  [key pat]
  (swap! patterns* (fn [patterns key new-pat]
                     (let [[samp pat] (get patterns key)]
                       (assoc patterns key [samp new-pat])))
         key pat))

(comment
  (update-pat! :kick  [[X _ X _ [_ _ X _] _  [X]  _]])
  (update-pat! :kick  [[_]])

  (update-pat! :subby [(repeat 8 0.7)])
  (update-pat! :subby [[_]])
  (update-pat! :crash [[_  [_ 0.2 0.5] 0.8 15]])
  (update-pat! :crash [[[[_ 3 _  _] X]]])
  (update-pat! :wind [[_]])
  (update-pat! :lp [[_]])
  (update-pat! :boom [[9 [_] _ _]])
  (update-pat! :s-snare [[X _ (vec (repeat 64 0.8)) [0.4 0.8 0.9 0.4 0.3 0.7]]])
  (update-pat! :s-snare [[_]])
  (update-pat! :kick  [[_]])
  (update-pat! :crash  [[_]])
  (update-pat! :tom [[X X X X]])
  (update-pat! :tom [[_]])

  (update-pat! :ring-hat [(repeat 16 #{0.5 1})])
  (update-pat! :ring-hat [[_]])

  (meta-ex.rhythm/play-rhythm patterns* bar-dur)
  (reset! bar-dur 4000)

  (volume 1)
  (kick)
  (tom)

  (def wwii (sample-player (sample (freesound-path 43807)) :loop? true))
  (def windy (sample-player (sample (freesound-path 17553)) :loop? true))

  (ctl wwii :rate 0.5 :vol 1 :out-bus (nkmx :m1))
  (ctl windy :rate 1 :vol 1 :out-bus (nkmx :m0))


  (kill wwii)
  (kill windy))
