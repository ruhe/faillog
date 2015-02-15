(ns faillog.render
  (:require [selmer.parser :refer [render-file]]))

(println (render-file "index.html"
                      {:header "Hello WOrld!"}))
