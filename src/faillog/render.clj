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

(defn build-report [jenkins jobs]
  (map (fn [name] {:name name
                   :builds (attach-bugs
                            (jenkins/get-failed-builds jenkins name))})
       jobs))

(defn render-bugs [jenkins jobs]
  (render-file "index.html"
               {:jenkins jenkins
                :jobs (build-report jenkins jobs)}))


(defn generate-report [jenkins jobs output]
  (spit output (render-bugs jenkins jobs)))
