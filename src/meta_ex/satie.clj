(ns satie
  (:use [clojure.core.match :only [match]]
        [overtone.live]
        [overtone.inst sampled-piano]
        [meta-ex.petals :only [num-petals-to-draw*]])
  (:require [meta-ex.monome-event]))



;; hat = Klank.ar(`[ [ 6563, 9875 ],
;;                [ 0.6, 0.5 ],
;;                [ 0.002, 0.003] ], PinkNoise.ar(1));
;;     hatOut = Pan2.ar(hat*env3, pan2, hatLevel);




;;Erik Satie Gnossienne No. 1
(def phrase1a [:iii :v :iv# :iii :iii :ii# :iii :ii#])
(def phrase1b [:iii :v :iv# :iii :v# :vi :v# :vi])
(def phrase1c [:iii :v :iv# :iii :iii :ii# :i :vii- :vi- :vii- :vi- :vii- :i :vii- :vii- :vi-])

(def phrase2 [:i :ii :i :vii- :i :ii :i :vii- :i :vii- :vii- :vi-])

(def phrase3 [:iii :iv# :v# :vi :vii :ii#+ :vii :vi :vii :vi :vii :vi :vi :v# :iv :iii :iii :ii# :i :vii- :vii- :vi-])

(def phrase1a-reprise [:iii :v :iv# :iii :iii :ii#])
(def phrase1b-reprise [:iii :v :iv# :iii :v# :vi])

(def phrase1-bass [:vi--- [:vi- :iii- :i-] [:vi- :iii- :i-]])
(def phrase2-bass [:iii-- [:iii- :vii-- :v--] [:iii- :vii-- :v--]])

(def phrase3-bass [:ii--- [:vi-- :ii- :iv-] [:vi-- :ii- :iv-]])


(def right-hand-degrees (concat phrase1a phrase1b phrase1c
                                phrase1a phrase1b phrase1c
                                phrase2
                                phrase2
                                phrase3
                                phrase3
                                phrase2
                                phrase2
                                phrase1a-reprise
                                phrase1b-reprise
                                phrase1a-reprise
                                phrase1b-reprise
                                phrase2
                                phrase2
                                phrase3
                                phrase3
                                phrase2
                                phrase2))


(def left-hand-degrees (concat (apply concat (repeat 6 phrase1-bass))  ;;A
                               phrase2-bass                            ;;B
                               (apply concat (repeat 8 phrase1-bass))  ;;C
                               phrase2-bass                            ;;D
                               (apply concat (repeat 2 phrase1-bass))  ;;E
                               (apply concat (repeat 2 phrase3-bass))  ;;F
                               (apply concat (repeat 2 phrase1-bass))  ;;G
                               (apply concat (repeat 2 phrase3-bass))  ;;H
                               (apply concat (repeat 14 phrase1-bass)) ;;I
                               (apply concat (repeat 2 phrase3-bass))  ;;J
                               (apply concat (repeat 2 phrase1-bass))  ;;K
                               (apply concat (repeat 2 phrase3-bass))  ;;L
                               (apply concat (repeat 10 phrase1-bass)) ;;M
                               (apply concat (repeat 2 phrase3-bass))  ;;N
                               (apply concat (repeat 2 phrase1-bass))  ;;O
                               (apply concat (repeat 2 phrase3-bass))  ;;P
                               (apply concat (repeat 14 phrase1-bass)) ;;Q
                               (apply concat (repeat 2 phrase3-bass))  ;;R
                               (apply concat (repeat 2 phrase1-bass))  ;;S
                               (apply concat (repeat 2 phrase3-bass))  ;;T
                               phrase1-bass                            ;;U
                               ))

(def lh-pitches (degrees->pitches left-hand-degrees :major :Ab4))
(def rh-pitches (degrees->pitches right-hand-degrees :major :Ab4))

(def cur-pitch-rh (atom -1))
(def cur-pitch-lh (atom -1))

(defn reset-pos
  []
  (reset! cur-pitch-rh -1)
  (reset! cur-pitch-lh -1))

(defn vol-mul
  [vol]
  (* vol 0.008))

(defn play-next-rh
  [vol]
  (let [idx (swap! cur-pitch-rh inc)
        pitch (nth (cycle rh-pitches) idx)]
    (swap! num-petals-to-draw* inc)
    (sampled-piano pitch (/ (vol-mul vol) 2) :out-bus 0)))

(defn play-next-lh
  [vol]
  (let [idx (swap! cur-pitch-lh inc)
        pitch (nth (cycle lh-pitches) idx)]
    (if (sequential? pitch)
      (doseq [p pitch]
        (sampled-piano p (/ (vol-mul vol) 2) :out-bus 0))
      (sampled-piano pitch (/ (vol-mul vol) 2) :out-bus 0))))


(on-event [:monome :press]
          (fn [{:keys [x y]}]
            (match [x y]
                   [7 _] (reset-pos)
                   [_ 0] (play-next-lh (+ (rand-int 5) (* 12 (+ x 4))))
                   [_ 7] (play-next-rh (+ (rand-int 5) (* 12 (+ x 4))))))
          :monome-press)

(on-event [:monome :press]
          (fn [{:keys [x y monome]}]
;;            (poly/toggle-led monome x y)
            )
          :monome-led)
;;(boot-external-server)

;;(play-next-lh 8)


;;(poly/remove-all-callbacks m)
;;(poly/disconnect m)


(comment   (def wwii (sample-player (sample (freesound-path 43807)) :loop? true :out-bus 10))
           (def windy (sample-player (sample (freesound-path 17553)) :loop? true :out-bus 10))
           (ctl wwii :rate 0.5)
           (stop))
