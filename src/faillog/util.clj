(ns faillog.util
  (:require [clojure.tools.logging :as log]
            [clj-http.client :as client]))

(defn json-get [url]
  (log/info "Calling url:" url)
  (:body (client/get url {:as :json})))
