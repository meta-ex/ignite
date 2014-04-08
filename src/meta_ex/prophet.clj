(ns meta-ex.prophet
  (:use [overtone.live]
        [meta-ex.keyboard]
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

  [amp 1 note 52 note-mod 0 slide 0 cutoff-freq 12000 cutoff-slide 2 rq 0.3  attack 1 decay 2 out-bus 0 beat-b (:id tim/inv-root-b) beat-cnt-b (:id tim/count-b) rhyth-bf 0 rhyth-bf-size 16]

  (let [note (+ note note-mod)
        freq (midicps note)
        freq (lag freq slide)
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
        snd  (rlpf (* env snd snd) (lag cutoff-freq cutoff-slide) rq)]

    (out out-bus (* amp snd))))

(do
  (defonce rhyth (buffer 16))
  (defonce proph-g (group "The Prophet" ))

  (def p4 (prophet2 [:head proph-g] :note-mod -24 :amp 0 :out-bus (nkmx :s0)) )
  (def p5 (prophet2 [:head proph-g] :note-mod 0 :amp 0 :out-bus (nkmx :s0)))
  (def p6  (prophet2 [:head proph-g] :note-mod -12 :amp 0 :out-bus (nkmx :s0)))

  (on-event (sam-kb :note-on)
            (fn [m] (ctl proph-g :note (:note m)))
            ::proph-control)

  (buffer-write! rhyth (rotate 1 [1 0 1 0 1 0 1 0 1 0 1 0 1 0 0 1]))
  )

(let [d 0.5
      a 0.01
      rq 0.5
      bb tim/beat-2th
      bb2 tim/beat-4th
      bb3 tim/beat-6th
      bb3 bb3
      bb2 bb2
      bb bb
      amp 3
      cf 100
      n (note :a3)]

  (ctl p4 :note-mod 5 :cutoff-freq 3000 :decay 0.1  :beat-b (:beat bb2) :beat-cnt-b (:count bb2) :amp 0)
  (ctl p5 :note-mod 12 :decay 0.2  :beat-b (:beat bb3) :beat-cnt-b (:count bb3) :amp 0)
  (ctl p6 :note-mod -24 :decay 0.5 :amp 20 :cutoff-freq 100 :beat-b (:beat bb) :beat-cnt-b (:count bb)  :amp 0.1))

(kill proph-g)


(comment
  (rotate -2 [1 2 3 4 5])

  (defonce rhyth (buffer 16))
  (buffer-write! rhyth [1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0])
  (buffer-write! rhyth [1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1])
  (buffer-write! rhyth [1 1 1 1 1 1 1 1 0 1 1 1 1 0 1 1])
  (def bb (tim/beat-bus 1/16))
)
