(ns faillog.render
  (:require [selmer.parser :refer [render-file]]
            [faillog.jenkins :as jenkins]
            [faillog.launchpad :as lp]))


(defn attach-bug [build]
  (if (:bugs build)
    (assoc build :bugs (map #(lp/get-bug %) (:bugs build)))
    build))

(defn attach-bugs [builds]
  (sort-by :number > (map attach-bug builds)))


(defn foo [jenkins jobs]
  (map (fn [x] {:job x :builds (attach-bugs
                          (jenkins/get-failed-builds jenkins x))}) jobs))

(defn render-bugs [jenkins jobs]
  (render-file "index.html"
               {:jenkins jenkins
                :jobs (foo jenkins jobs)}))


(defn generate-report [jenkins job]
  (spit "/tmp/test.html" (render-bugs jenkins job)))
