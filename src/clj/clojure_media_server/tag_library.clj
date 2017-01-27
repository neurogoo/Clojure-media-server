(ns clojure-media-server.tag-library
  (:require [clojure.java.io :as io]))

(defn has-image-data? [file]
  (let [tag (.getTag (org.jaudiotagger.audio.AudioFileIO/read file))]
    (if (.getFirstArtwork tag)
      true
      false)))

(defn get-image-data [file]
  (let [tag (.getTag (org.jaudiotagger.audio.AudioFileIO/read file))]
    (when-let [first-art-work (.getFirstArtwork tag)]
      (java.io.ByteArrayInputStream. (.getBinaryData first-art-work)))))
