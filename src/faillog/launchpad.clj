(ns faillog.launchpad
  (:require [faillog.util :use json-get]
            [clojure.core :use memoize]
            [clojure.set :use rename-keys]))

(def BUG_TASKS_URL
  "https://api.launchpad.net/1.0/bugs/%d/bug_tasks")

(def BUG_URL
  "https://api.launchpad.net/1.0/bugs/%d")


(defn- get-assignee-internal [link]
  (-> (json-get link)
      (select-keys [:display_name :name])))

(def get-assignee (memoize get-assignee-internal))

(defn- append-assignee-info
  "Appends info about assignee {:name, :display_name} to the bug,
   if there is :assignee_link. Otherwise returns the bug as is"
  [bug]
  (let [link (:assignee_link bug)]
    (if link
      (assoc bug :assignee (get-assignee link))
      bug)))


(defn- get-main-bug-task-raw [bug-id]
  (-> (json-get (format BUG_TASKS_URL bug-id))
      :entries
      first
      (select-keys [:assignee_link :bug_target_name
                    :importance :milestone_link :status])
      (rename-keys {:bug_target_name :target})))

(defn- append-main-bug-task [bug]
  (into (get-main-bug-task-raw (:id bug)) bug))


(defn- get-bug-basic-info [url]
  (-> (json-get url)
      (select-keys [:id :title :duplicate_of_link])))

(defn- replace-duplicate
  "Check if bug is a duplicate of another one.
   If so, query another bug, instead of the duplicate,
   otherwise return bug without any changes."
  [bug]
  (if (nil? (:duplicate_of_link bug))
    bug
    (get-bug-basic-info (:duplicate_of_link bug))))

(defn- cleanup-bug [bug]
  (dissoc bug :assignee_link :duplicate_of_link))

(defn- get-bug-internal [bug-id]
  (try
    (-> (get-bug-basic-info (format BUG_URL bug-id))
        (replace-duplicate)
        (append-main-bug-task)
        (append-assignee-info)
        (cleanup-bug))
    (catch Exception e
      {:id bug-id :title (format "Unknown #%d" bug-id)})))

(def get-bug (memoize get-bug-internal))
