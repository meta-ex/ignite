(ns meta-ex.synths.synths
  (:use [overtone.core]))


(defsynth woah [note 52 out-bus 0 x 0]
    (let [freq (midicps note)
          x    (abs x)
          x    (/ x 700)
          x    (min x 15)
          x    (max x 0.5)
          snd  (lpf (sync-saw
                     freq
                     (* (* freq 1.5) (+ 2 (sin-osc:kr x))))
                    1000)]
      (out out-bus (* 0.25 (pan2 snd)))))


(defsynth woah [note 52 amp 1 out-bus 0 depth 2 range 1 rate 0.5]
    (let [freq (midicps note)
          snd  (lpf (sync-saw
                     freq
                     (* (* freq 1.5) (+ depth (* range (sin-osc:kr rate)))))
                    1000)]
      (out out-bus (* 0.25 (pan2 (* amp snd))))))


(defsynth spacey [out-bus 0 amp 1]
  (out out-bus (* amp (g-verb (blip (mouse-y 24 48) (mouse-x 1 100)) 200 8))))

(defsynth cs80
  [freq 880
   amp 0.5
   att 0.75
   decay 0.5
   sus 0.8
   rel 1.0
   fatt 0.75
   fdecay 0.5
   fsus 0.8
   frel 1.0
   cutoff 200
   dtune 0.002
   vibrate 4
   vibdepth 0.015
   gate 1
   ratio 1
   cbus 1
   freq-lag 0.1
   out-bus 0
   lpf-freq 1000]
  (let [freq (lag freq freq-lag)
        cuttoff (in:kr cbus)
        env     (env-gen (adsr att decay sus rel) gate :action FREE)
        fenv    (env-gen (adsr fatt fdecay fsus frel 2) gate)

        vib     (+ 1 (lin-lin:kr (sin-osc:kr vibrate) -1 1 (- vibdepth) vibdepth))

        freq    (* freq vib)
        sig     (mix (* env amp (saw [freq (* freq (+ dtune 1))])))
        sig     (lpf sig lpf-freq)
        sig     (normalizer sig)]
    (out out-bus (* amp sig))))


(defsynth supersaw2 [freq 440 amp 2.5 fil-mul 2 rq 0.3 out-bus 0]
  (let [input  (lf-saw freq)
        shift1 (lf-saw 4)
        shift2 (lf-saw 7)
        shift3 (lf-saw 5)
        shift4 (lf-saw 2)
        comp1  (> input shift1)
        comp2  (> input shift2)
        comp3  (> input shift3)
        comp4  (> input shift4)
        output (+ (- input comp1)
                  (- input comp2)
                  (- input comp3)
                  (- input comp4))
        output (- output input)
        output (leak-dc:ar (* output 0.25))
        output (normalizer (rlpf output (* freq fil-mul) rq))]

    (out out-bus (* amp output (line 1 0 10 FREE)))))
