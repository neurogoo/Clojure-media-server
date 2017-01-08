(ns clojure-media-server.database
  (:require [mount.core :refer [defstate]]
            [clojure.java.io :as io]
            [cprop.core :refer [load-config]]
            [id3]
            [claudio.id3 :as clid3]))

(defn all-files-in-folder [folder]
  (let [files (.listFiles folder)]
    (if (seq files)
      (map #(if (.isDirectory %)
              (all-files-in-folder %)
              %) files))))
;joee
(defn exception-friendly-tag [tag data]
  (try
    (tag data)
    (catch Exception e "")))
(defn populate-with-song-metadata [song]
  "Read the tags of each file and return them as map"
  (let [id3-tag-data (try (clid3/read-tag song)
                          (catch Exception e {:title "" :album "" :track-number ""})
                          (finally {:title "" :album "" :track-number ""}))]
    (hash-map :path (.getPath song)
              :title (.trim (or (:title id3-tag-data) ""))
              :album (.trim (or (:album id3-tag-data) ""))
              :track-number (.trim (or (:track id3-tag-data) "")))))
(defn get-files-in-folder
  "ei sorttaa vielä"
  []
  (let [music-library-home (or (:cloms-music-home (load-config)) "/home/tokuogum/Clojure/clojure-media-server/testmedia")]
    (map #(populate-with-song-metadata %)
       (filter #(re-matches #"(.*).mp3$" (.getPath %))
               (flatten (all-files-in-folder (io/file music-library-home)))))))
(defn get-all-albums [songs]
  (distinct (map :album songs)))
(defn populate-db []
  (let [songs (get-files-in-folder)
        albums (get-all-albums songs)]
    (hash-map :albums (into {} (map-indexed (fn [idx i] [(keyword (str idx)) i]) albums)),
              :songs (into {} (map-indexed (fn [idx i] [(keyword (str idx)) i]) songs)))))
(defstate db :start (populate-db))

(defn get-song-by-id [id]
  (get-in db [:songs (keyword id)]))
