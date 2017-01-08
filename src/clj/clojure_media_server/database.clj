(ns clojure-media-server.database
  (:require [mount.core :refer [defstate]]
            [clojure.java.io :as io]
            [cprop.core :refer [load-config]]
            [id3]
            [claudio.id3 :as clid3]
            [hugsql.core :as hugsql]))

(def sqlitedb
  {:classname   "org.sqlite.JDBC",
   :subprotocol "sqlite",
   :subname     "test.db"})

(defn all-files-in-folder [folder]
  (let [files (.listFiles folder)]
    (if (seq files)
      (map #(if (.isDirectory %)
              (all-files-in-folder %)
              %) files))))

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
  "Fetch all files from base folder and all folders under it"
  []
  (let [music-library-home (or (:cloms-music-home (load-config)) "/home/tokuogum/Clojure/clojure-media-server/testmedia")]
    (map #(populate-with-song-metadata %)
       (filter #(re-matches #"(.*).mp3$" (.getPath %))
               (flatten (all-files-in-folder (io/file music-library-home)))))))

(defn get-all-album-titles [songs]
  (distinct (map :album songs)))

(defn populate-db []
  (let [songs (get-files-in-folder)
        albums (get-all-album-titles songs)]
    (hash-map :albums (into {} (map-indexed (fn [idx i] [(keyword (str idx)) i]) albums)),
              :songs (into {} (map-indexed (fn [idx i] [(keyword (str idx)) i]) songs)))))
(defn recreate-album-table []
  (drop-albums-table sqlitedb)
  (create-album-table sqlitedb))

(defn recreate-song-table []
  (drop-songs-table sqlitedb)
  (create-song-table sqlitedb))

(defn refresh-database []
  (let [songs (get-files-in-folder)
        albums (get-all-album-titles songs)]
    (recreate-album-table)
    (recreate-song-table)
    (dorun (map #(insert-album sqlitedb {:name %}) albums))
    (let [albums (get-all-albums sqlitedb)]
      (dorun (map #(insert-song sqlitedb {:album_id (:id (first (filter (fn [album] (= (:album %) (:name album)))(get-all-albums sqlitedb))))
                                          :name (:title %)
                                          :path (:path %)}) songs)))))

(defn start-sql-connection []
  ;;initialize database functions
  (hugsql/def-db-fns "sql/music.sql")
  (hugsql/def-sqlvec-fns "sql/music.sql")
  (populate-db))

(defstate db :start (start-sql-connection))

(defn get-song-by-id [id]
  (get-in db [:songs (keyword id)]))
