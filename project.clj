(defproject meta-ex "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [aleph "0.3.0-beta7"]
                 [rhizome "0.2.0"]
                 [overtone "0.9.1"]
                 [polynome "0.3.0-SNAPSHOT"]
                 [quil "1.6.0"]
                 [compojure "1.1.1"]
                 [org.clojure/data.json "0.1.2"]
                 [seesaw "1.4.3"]
                 [rogerallen/leaplib "0.8.1"]
                 [rogerallen/leaplib-natives "0.8.1"]
                 [shadertone "0.2.2"]
                 [watchtower "0.1.1"]
                 [prismatic/schema "0.1.1"]
                 [http-kit "2.1.13"]
                 [clj-time "0.6.0"]
                 [org.clojure/core.async "0.1.0-SNAPSHOT"]
                 [org.clojure/core.match "0.2.0"]
                 [org.clojure/clojurescript "0.0-1934"]
                 ]

  :plugins [[lein-cljsbuild "0.3.3"]]
  :source-paths ["cljs-src" "src"]
  :cljsbuild {
              :builds [{:source-paths ["cljs-src"]
                        :compiler {:output-to "resources/web/js/cljs-main.js"
                                   :optimizations :whitespace
}}]}
  :repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}
  :jvm-opts ^:replace [
;;             "-agentpath:/Applications/YourKit_Java_Profiler_12.0.5.app/bin/mac/libyjpagent.jnilib"
    "-Xms6g" "-Xmx7g"           ; Minimum and maximum sizes of the heap
;    "-XX:+UseParNewGC"            ; Use the new parallel GC in conjunction with
;    "-XX:+UseConcMarkSweepGC"     ;  the concurrent garbage collector
;    "-XX:+CMSConcurrentMTEnabled" ; Enable multi-threaded concurrent gc work (ParNewGC)
    "-XX:MaxGCPauseMillis=1000"     ; Specify a target of 20ms for max gc pauses
;    "-XX:+CMSIncrementalMode"     ; Do many small GC cycles to minimize pauses
 ;   "-XX:MaxNewSize=257m"         ; Specify the max and min size of the new
;    "-XX:NewSize=256m"            ;  generation to be small
    "-XX:+UseTLAB"                ; Uses thread-local object allocation blocks. This
                                  ;  improves concurrency by reducing contention on
                                        ;  the shared heap lock.

    "-XX:+UseG1GC"
;    "-XX:MaxTenuringThreshold=0"
;    "-XX:+PrintGC"                ; Print GC info to stdout
;    "-XX:+PrintGCDetails"         ;  - with details
;    "-XX:+PrintGCTimeStamps"
    ] ; Makes the full NewSize available to
                                  ;  every NewGC cycle, and reduces the
                                  ;  pause time by not evaluating
                                  ;  tenured objects. Technically, this
                                  ;  setting promotes all live objects
                                  ;  to the older generation, rather
                                        ;  than copying them.
  )
