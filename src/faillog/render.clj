(ns faillog.render
  (:require [selmer.parser :refer [render-file]]
            [faillog.jenkins :as jenkins]
            [faillog.launchpad :as lp]))


(def jenkins "http://jenkins-product.srt.mirantis.net:8080/")
(def job "6.1.staging.centos.bvt_1")

(defn attach-bug [build]
  (if (:bug build)
    (assoc build :bug (lp/get-bug (:bug build)))
    build))

(defn attach-bugs [builds]
  (sort-by :number > (map attach-bug builds)))


(defn render-bugs []
  (render-file "index.html"
               {:builds (attach-bugs
                         (jenkins/get-failed-builds jenkins job))}))
