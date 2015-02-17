(ns faillog.util
  (:require [clojure.tools.logging :as log]
            [clj-http.client :as client]
            [clj-time.coerce :use from-long]
            [clj-time.format :use [unparse formatters]]))

(defn json-get [url]
  (log/info "Calling url:" url)
  (:body (client/get url {:as :json})))

(defn ts-to-datestr [timestamp]
  (unparse (formatters :date)
           (from-long timestamp)))
