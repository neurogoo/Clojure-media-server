(ns clojure-media-server.database
  (:require [mount.core :refer [defstate]]
            [clojure.java.io :as io]
            [cprop.core :refer [load-config]]
            [id3]
            [claudio.id3 :as clid3]
            [hugsql.core :as hugsql]
            [config.core]
            [clojure-media-server.tag-library :as taglib]
            [me.raynes.conch.low-level :as sh]))

(def sqlitedb
  {:classname   "org.sqlite.JDBC",
   :subprotocol "sqlite",
   :subname     "test.db"})

;;initialize database handlers
(hugsql/def-db-fns "sql/music.sql")
(hugsql/def-sqlvec-fns "sql/music.sql")

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
  (let [default-song-values {:title "" :album "" :track-number "" :artist ""}
        id3-tag-data (try (clid3/read-tag song)
                          (catch Exception e default-song-values)
                          (finally default-song-values))]
    (hash-map :path (.getPath song)
              :title (.trim (or (:title id3-tag-data) ""))
              :album (.trim (or (:album id3-tag-data) ""))
              :artist (.trim (or (:artist id3-tag-data) "")) 
              :track-number (.trim (or (:track id3-tag-data) "")))))

(defn get-files-in-folder
  "Fetch all files from base folder and all folders under it"
  []
  (let [music-library-home (or (:cloms-music-home (load-config)) "/home/tokuogum/Clojure/clojure-media-server/testmedia")]
    (pmap #(populate-with-song-metadata %)
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
    (insert-albums sqlitedb {:albums (map #(vector %) albums)})
    (let [albums (get-all-albums sqlitedb)]
      (insert-songs sqlitedb {:songs (map #(vector (:id (first (filter (fn [album] (= (:album %) (:name album))) albums)))
                                                   (:title %)
                                                   (:path %)) songs)}))))

(defn start-sql-connection []
  (when (config.core/env :production)
    (refresh-database))
  (+ 1 2)
  #_(populate-db))

(defn get-song-album-art [id]
  (taglib/get-image-data (io/file (:path (get-song-by-id sqlitedb {:id id})))))

(defn get-song-data [id]
  (io/file (:path (get-song-by-id sqlitedb {:id id}))))
                                       
(defn get-song-metadata [id]
  (populate-with-song-metadata (io/file (:path (get-song-by-id sqlitedb {:id id})))))

(defn get-songs []
  (let [songs (get-all-songs sqlitedb)]
    (map #(assoc (populate-with-song-metadata (io/file (:path %))) :id (:id %)) songs)))

(defn get-albums []
  (get-all-albums sqlitedb))

(defn get-album-songs [album-id]
  (let [songs (get-songs-by-album-id sqlitedb {:album_id album-id})]
    (map #(assoc (populate-with-song-metadata (io/file (:path %))) :id (:id %)) songs)))

(defn flac->mp3-test []
  (:out (sh/proc "ffmpeg" "-i" "Lataukset/Partytime/ClariS - PARTY TIME/05 - RESTART.flac" "-f" "opus" "-")))

(defstate db :start (start-sql-connection))

