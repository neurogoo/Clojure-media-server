(ns clojure-media-server.db
  (:require [cljs.reader]
            [cljs.spec :as s]
            [re-frame.core :as re-frame]))
(def default-value                                          ;; what gets put into app-db by default.
  {:album-songs {}
   :albums {}
   :current-song {}})                                          ;; show all todos
