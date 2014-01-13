(ns satie
  (:use [overtone.live]
        [overtone.synth sampled-piano]
        [meta-ex.kit.mixer]
        [meta-ex.sets.ignite])
  (:require
   [meta-ex.hw.polynome :as poly]
   [meta-ex.hw.monomes :as mon]
   [meta-ex.hw.fonome :as fon]))


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
    (sampled-piano (- pitch 0) (/ (vol-mul vol) 0.25) :out-bus (nkmx :r0))))

(defn play-next-lh
  [vol]
  (let [idx (swap! cur-pitch-lh inc)
        pitch (nth (cycle lh-pitches) idx)]
    (if (sequential? pitch)
      (doseq [p pitch]
        (sampled-piano p (/ (vol-mul vol) 0.25) :out-bus (nkmx :r0)))
      (sampled-piano pitch (/ (vol-mul vol) 0.25) :out-bus (nkmx :r0)))))

;; (play-next-rh 2)
;; (play-next-lh 5)

;; (reset-pos)



;; (on-event [:midi :note-on]
;;           (fn [msg]
;;             (let [amp (* 50 (:velocity-f msg ))]
;;               (if (= 48 (:note msg))
;;                 (play-next-lh amp)
;;                 (play-next-rh amp)))
;;             )
;;           ::satie)

;; (event-debug-off)

;; (first (mon/monomes))

;; (def satie-f   (fon/mk-fonome ::satie642 2 2))

;; (poly/dock-fonome! (first (mon/monomes)) satie-f ::my-fonome3 2 2)

;; (on-event [])

;; (event-monitor-timer)
;; (event-monitor-keys) ;;=> #{[:overtone :trigger 54 2] [:overtone :trigger 50 1] [:overtone :trigger 46 0] "/m-x/beat-trigger/" [:fonome :press :satie/satie64 1 0] [:fonome :press :satie/satie64] [:fonome :press :satie/satie64 0 0] [:overtone :trigger 0] "/tr" [:overtone :trigger 1] [:overtone :trigger 2] [:overtone :trigger 3] [:overtone :trigger 4] [:overtone :osc-msg-received] [:overtone :trigger 5] [:overtone :trigger 66 5] [:fonome :release :satie/satie64] [:fonome :release :satie/satie64 0 0] [:overtone :trigger 62 4] [:overtone :trigger 58 3]}

;; (on-event [:fonome :press :satie/satie642]
;;           (fn [m]
;;             (if (= 0 (:x m))
;;               (play-next-lh 10)
;;               (play-next-rh 10))
;; )
;;           ::yo)
