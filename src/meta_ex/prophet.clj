(ns meta-ex.prophet
  (:use [overtone.live]

        [meta-ex.kit.mixer])
  (:require [meta-ex.kit.timing :as tim]))

(defsynth prophet2
  "The Prophet Speaks (page 2)

   Dark and swirly, this synth uses Pulse Width Modulation (PWM) to
   create a timbre which continually moves around. This effect is
   created using the pulse ugen which produces a variable width square
   wave. We then control the width of the pulses using a variety of LFOs
   - sin-osc and lf-tri in this case. We use a number of these LFO
   modulated pulse ugens with varying LFO type and rate (and phase in
   some cases to provide the LFO with a different starting point. We
   then mix all these pulses together to create a thick sound and then
   feed it through a resonant low pass filter (rlpf).

   For extra bass, one of the pulses is an octave lower (half the
   frequency) and its LFO has a little bit of randomisation thrown into
   its frequency component for that extra bit of variety."

  [amp 1 freq 440 cutoff-freq 12000 rq 0.3  attack 1 decay 2 out-bus 0 beat-b (:id tim/inv-root-b) beat-cnt-b (:id tim/count-b) rhyth-bf 0 rhyth-bf-size 16]

  (let [freq (lag freq 2)
        snd  (pan2 (mix [(pulse freq (* 0.1 (/ (+ 1.2 (sin-osc:kr 1)) )))
                         (pulse freq (* 0.8 (/ (+ 1.2 (sin-osc:kr 0.3) 0.7) 2)))
                         (pulse freq (* 0.8 (/ (+ 1.2 (lf-tri:kr 0.4 )) 2)))
                         (pulse freq (* 0.8 (/ (+ 1.2 (lf-tri:kr 0.4 0.19)) 2)))
                         (* 0.5 (pulse (/ freq 2) (* 0.8 (/ (+ 1.2 (lf-tri:kr (+ 2 (lf-noise2:kr 0.2))))
                                                            2))))]))
        snd  (normalizer snd)
        cnt  (mod (in:kr beat-cnt-b)
                  rhyth-bf-size)
        beat (* (in:kr beat-b)
                (buf-rd:kr 1 rhyth-bf cnt))
        env  (env-gen (perc attack decay) :gate beat)
        snd  (rlpf (* env snd snd) (lag cutoff-freq 0.3) rq)]

    (out out-bus (* amp snd))))


(comment
  (rotate -2 [1 2 3 4 5])

  (def rhyth (buffer 16))
  (buffer-write! rhyth [1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0])
  (def bb (tim/beat-bus 1/16))


  (defonce proph-g (group "The Prophet" ))

  (let [n  (note :a1)
        d 10
        rq 0.6
        a 0.3
        amp 0.5
        cf 1000]
    (def p  (prophet2 [:head proph-g] :freq (midi->hz n)  :decay d :rq rq :attack a :cutoff-freq cf :beat-b (:beat bb) :beat-cnt-b (:count bb) :rhyth-bf rhyth :out-bus (nkmx :s1)) )
    (def p2 (prophet2 [:head proph-g] :freq (midi->hz (- n 12)) :decay d :rq rq :attack a :cutoff-freq cf :beat-b (:beat bb) :beat-cnt-b (:count bb) :rhyth-bf rhyth :out-bus (nkmx :s1)))
    (def p3  (prophet2 [:head proph-g] :freq (midi->hz (+ n 7)) :decay d :rq rq :attack a :cutoff-freq cf :beat-b (:beat bb) :beat-cnt-b (:count bb) :rhyth-bf rhyth :out-bus (nkmx :s1)))
    )

  )
(volume 0.5)

(ctl proph-g :cutoff-freq 2000 :amp 1)

(let [n (note :a2)
      f (midi->hz n)
      f2 (midi->hz (- n 12))
      f3 (midi->hz (+ 7 n))]
  (ctl p :freq f :out-bus (nkmx :m0))
  (ctl p2 :freq f2 :out-bus (nkmx :m0))
  (ctl p3 :freq f3 :out-bus (nkmx :m0)))

(kill p p2 p3)
(stop)


(do

)
