(ns meta-ex.piano
  (:use [overtone.live]
        [meta-ex.kit.mixer]
        [overtone.synth.sampled-piano]))

(def chord-prog
  [#{[2 :minor7] [7 :minor7] [10 :major7]}
   #{[0 :minor7] [8 :major7]}])

(def beat-offsets [0 0.1 0.2 1/3  0.7 0.9])

(def metro (metronome 20))

(def root 40)
(def max-range 35)
(def range-variation 10)
(def range-period 8)

(defn beat-loop
  [metro beat chord-idx]
  (let [[tonic chord-name] (choose (seq (nth chord-prog chord-idx)))
        nxt-chord-idx      (mod (inc chord-idx) (count chord-prog))
        note-range         (cosr beat range-variation  max-range range-period)
        notes-to-play      (rand-chord (+ root tonic)
                                       chord-name
                                       (count beat-offsets)
                                       note-range)]
    (dorun
     (map (fn [note offset]
            (at (metro (+ beat offset))
                (do (sampled-piano note 0.3 :out-bus (nkmx :m0))
;                    (sampled-piano (- note 5 ) 0.3 :out-bus (nkmx :m0))
                    )
                ))
          notes-to-play
          beat-offsets))
    (apply-by (metro (inc beat)) #'beat-loop [metro (inc beat) nxt-chord-idx])))


(beat-loop metro (metro) 0)

;;(def beat-offsets [0 ])~P~
;;(def beat-offsets [0 0.2 1/3  0.5 0.8])

;(def beat-offsets [0 0.2 0.4  0.6 0.8])
;(def beat-offsets [0 0.1 0.2  0.3 0.4])
;(def beat-offsets [0 0.1 0.11 0.13 0.15 0.17 0.2 0.4 0.5 0.55 0.6 0.8])

;(stop)
