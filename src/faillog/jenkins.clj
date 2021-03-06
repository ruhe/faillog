(ns faillog.jenkins
  (:require [faillog.util :use [json-get ts-to-datestr]]))

(def JOB_INFO_URL
  "%s/job/%s/api/json?tree=firstBuild[number],lastFailedBuild[number]")

(def BUILD_INFO_URL
  "%s/job/%s/%d/api/json?tree=result,timestamp,number,description")

(def URL_REGEX #"http[s]?://(?:[a-zA-Z]|[0-9]|[$-_@.&+]|
                 [!*\(\),]|(?:%[0-9a-fA-F][0-9a-fA-F]))+")

(def BUG_ID_REGEX #"\d{7}")


(defn- get-build-info [jenkins-url job-name]
  (json-get
   (format JOB_INFO_URL
           jenkins-url
           job-name)))

(defn get-build-number-range [jenkins-url job-name]
  (let [build (get-build-info jenkins-url job-name)]
    (range
      (-> build :firstBuild :number)
      ;; inc lastFailedBuild number because clojure range excludes last value
      (-> build :lastFailedBuild :number inc))))

(defn get-build-raw [jenkins-url job-name build-number]
  (json-get
   (format BUILD_INFO_URL
           jenkins-url
           job-name
           build-number)))

(defn find-bugs [build-description]
  (let [urls (re-seq URL_REGEX build-description)]
    (if (not-empty urls)
      (map #(Integer/parseInt %)
           (map (partial re-find BUG_ID_REGEX) urls))
      ())))

(defn assoc-bugs [build]
  (let [descr (:description build)]
    (if descr
      (assoc build :bugs (find-bugs descr))
      build)))

(defn- get-build [jenkins-url job-name build-number]
  (assoc-bugs
   (get-build-raw jenkins-url job-name build-number)))

(defn- failed-build? [build]
  (not= (:result build) "SUCCESS"))

(defn remove-description [build]
  (dissoc build :description))

(defn attach-date [build]
  (assoc build :date (ts-to-datestr (:timestamp build))))

(defn get-failed-builds [jenkins-url job-name]
  (->> (get-build-number-range jenkins-url job-name)
       (take-last 100) ;; take only last 100 builds
       (map (partial get-build jenkins-url job-name))
       (map attach-date)
       (map remove-description)
       (filter failed-build?)))
