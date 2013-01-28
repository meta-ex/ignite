(defproject meta-ex "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.0-RC2"]
                 [aleph "0.3.0-beta7"]
                 [overtone "0.8.1"]
                 [polynome "0.3.0-SNAPSHOT"]
                 [quil "1.6.0"]
                 [compojure "1.1.1"]
                 [org.clojure/data.json "0.1.2"]]
  :jvm-opts    ["-Xms512m" "-Xmx1g"           ; Minimum and maximum sizes of the heap
    "-XX:+UseParNewGC"            ; Use the new parallel GC in conjunction with
    "-XX:+UseConcMarkSweepGC"     ;  the concurrent garbage collector
    "-XX:+CMSConcurrentMTEnabled" ; Enable multi-threaded concurrent gc work (ParNewGC)
    "-XX:MaxGCPauseMillis=20"     ; Specify a target of 20ms for max gc pauses
    "-XX:+CMSIncrementalMode"     ; Do many small GC cycles to minimize pauses
    "-XX:MaxNewSize=257m"         ; Specify the max and min size of the new
    "-XX:NewSize=256m"            ;  generation to be small
    "-XX:+UseTLAB"                ; Uses thread-local object allocation blocks. This
                                  ;  improves concurrency by reducing contention on
                                  ;  the shared heap lock.
    "-XX:MaxTenuringThreshold=0"
;;    "-XX:+PrintGC"                ; Print GC info to stdout
;;    "-XX:+PrintGCDetails"         ;  - with details
;;    "-XX:+PrintGCTimeStamps"
    ] ; Makes the full NewSize available to
                                  ;  every NewGC cycle, and reduces the
                                  ;  pause time by not evaluating
                                  ;  tenured objects. Technically, this
                                  ;  setting promotes all live objects
                                  ;  to the older generation, rather
                                        ;  than copying them.
  )
