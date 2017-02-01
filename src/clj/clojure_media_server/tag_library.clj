(ns clojure-media-server.tag-library
  (:require [clojure.java.io :as io]))

(defn has-image-data? [file]
  (let [tag (.getTag (org.jaudiotagger.audio.AudioFileIO/read file))]
    (if (.getFirstArtwork tag)
      true
      false)))

(defn search-image-data-from-folder [file]
  (let [folder (.getParent file)
        list-of-pictures (filter #(clojure.string/ends-with? % ".jpg") (.listFiles (io/file folder)))]
    (if-let [cover-picture (first (filter #(clojure.string/starts-with? % "cover") list-of-pictures))]
      (io/file cover-picture)
      (when-let [picture (first list-of-pictures)]
        (io/file picture)))))

(defn get-image-data [file]
  "Tries to fetch album art from mp3 metadata and same folder"
  (let [tag (.getTag (org.jaudiotagger.audio.AudioFileIO/read file))]
    (if-let [first-art-work (.getFirstArtwork tag)]
      (java.io.ByteArrayInputStream. (.getBinaryData first-art-work))
      (search-image-data-from-folder file))))
