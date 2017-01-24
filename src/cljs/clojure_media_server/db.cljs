(ns clojure-media-server.db
  (:require [cljs.reader]
            [cljs.spec :as s]
            [re-frame.core :as re-frame]))
(def default-value                                          ;; what gets put into app-db by default.
  {:album-songs {}
   :albums {}
   :current-song {:title ""
                  :track-number ""
                  :url "/song/1"}
   :playlist ()
   :music-autoplay-handler-added? false})                                          ;; show all todos
