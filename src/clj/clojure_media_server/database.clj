(ns clojure-media-server.database
  (:require [mount.core :refer [defstate]]
            [clojure.java.io :as io]
            [id3]))

(defn all-files-in-folder [folder]
  (let [files (.listFiles folder)]
    (if (seq files)
      (map #(if (.isDirectory %)
              (all-files-in-folder %)
              %) files))))
(defn populate-with-song-metadata [song]
  (let [id3-tag-data (id3/with-mp3 [mp3 (.getPath song)] (:tag mp3))]
    (hash-map :path (.getPath song) :title (.trim (:title id3-tag-data)) :album (.trim (:album id3-tag-data)) :track-number (.trim (:track-number id3-tag-data)))))
(defn get-sorted-files-in-folder
  "ei sorttaa vielä"
  []
  (let [music-library-home "/home/tokuogum/Clojure/clojure-media-server/testmedia"]
    (map #(populate-with-song-metadata %)
       (filter #(re-matches #"(.*).mp3$" (.getPath %))
               (flatten (all-files-in-folder (io/file music-library-home)))))))
(defn populate-db []
  (map #(hash-map :album (first %) :songs (second %)) (group-by :album (map-indexed (fn [idx i] (assoc i :id idx)) (get-sorted-files-in-folder)))))

(defstate db :start (populate-db))

