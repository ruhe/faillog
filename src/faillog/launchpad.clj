(ns faillog.launchpad
  (:require [faillog.util :use json-get]
            [clojure.core :use memoize]
            [clojure.set :use rename-keys]))

(def BUG_TASKS_URL
  "https://api.launchpad.net/1.0/bugs/%d/bug_tasks")

(def BUG_URL
  "https://api.launchpad.net/1.0/bugs/%d")


(defn- get-assignee [link]
  (-> (json-get link)
      (select-keys [:display_name :name])))

(def get-assignee-memo (memoize get-assignee))

(defn- get-main-bug-task-raw [bug-id]
  (-> (json-get (format BUG_TASKS_URL bug-id))
      :entries
      first
      (select-keys [:assignee_link :bug_target_name
                    :importance :milestone_link :status])
      (rename-keys {:bug_target_name :target})))

(defn- get-bug-basic-info [url]
  (-> (json-get url)
      (select-keys [:id :title :duplicate_of_link])))

(defn- check-if-duplicate [bug]
  (if (nil? (:duplicate_of_link bug))
    bug
    (get-bug-basic-info (:duplicate_of_link bug))))


(defn- get-main-bug-task [bug-id]
  (let [bug (get-main-bug-task-raw bug-id)
        assignee_link (:assignee_link bug)]
    (if (not (nil? (:assignee_link bug)))
      (assoc bug
        :assignee (get-assignee-memo (:assignee_link bug)))
      bug)))

(defn- append-main-bug-task [bug]
  (into (get-main-bug-task (:id bug)) bug))

(defn- get-bug-internal [bug-id]
  (-> (get-bug-basic-info (format BUG_URL bug-id))
      (check-if-duplicate)
      (append-main-bug-task)
      (dissoc :assignee_link)
      (dissoc :duplicate_of_link)))

(def get-bug (memoize get-bug-internal))
