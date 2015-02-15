(ns faillog.jenkins
  (:require [faillog.util :use json-get]))

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

(defn- get-build-raw [jenkins-url job-name build-number]
  (json-get
   (format BUILD_INFO_URL
           jenkins-url
           job-name
           build-number)))

(defn find-bug [build-description]
  (let [url (re-find URL_REGEX build-description)]
    (if url
      (Integer. (re-find BUG_ID_REGEX url))
      nil)))

(defn- get-build [jenkins-url job-name build-number]
  (let [build
        (get-build-raw jenkins-url job-name build-number)]
    (assoc build
           :bug (find-bug (:description build)))))

(defn- failed-build? [build]
  (not= (:result build) "SUCCESS"))

(defn get-failed-builds [jenkins-url job-name]
  (filter failed-build?
          (map (partial get-build jenkins-url job-name)
               ;; limit number of builds to 100
               (take-last 100 (get-build-number-range jenkins-url
                                                      job-name)))))

(defn get-failed-builds2 [jenkins-url job-name]
  (->> (get-build-number-range jenkins-url job-name)
       (take-last 10)
       (map (partial get-build jenkins-url job-name))
       (filter failed-build?)))
