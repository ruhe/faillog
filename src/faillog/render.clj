(ns faillog.render
  (:require [selmer.parser :refer [render-file]]
            [faillog.jenkins :as jenkins]
            [faillog.launchpad :as lp]))


(defn attach-bug [build]
  (if (:bug build)
    (assoc build :bug (lp/get-bug (:bug build)))
    build))

(defn attach-bugs [builds]
  (sort-by :number > (map attach-bug builds)))


(defn render-bugs [jenkins job]
  (render-file "index.html"
               {:builds (attach-bugs
                         (jenkins/get-failed-builds jenkins job))}))
