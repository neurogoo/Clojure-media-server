(ns clojure-media-server.library
  (:require [clojure.java.io :as io]
            [id3]))
(def music-library-home "/home/tokuogum/Clojure/clojure-media-server/testmedia")
(defn test-print []
  (let [files (file-seq (io/file music-library-home))]
    (doseq [x files]
      (clojure.pprint/pprint x))))
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
  [folder]
  (map #(populate-with-song-metadata %)
       (filter #(re-matches #"(.*).mp3$" (.getPath %))
               (flatten (all-files-in-folder (io/file music-library-home))))))
(defn get-indexed-files-in-folder [folder]
  (map #(hash-map :album (first %) :songs (second %)) (group-by :album (map-indexed (fn [idx i] (assoc i :id idx)) (get-sorted-files-in-folder folder)))))
(defn get-files-in-folder [folder]
  (get-indexed-files-in-folder folder))
(defn get-playlist []
  )
(defn get-song []
  (io/file "/home/tokuogum/Clojure/clojure-media-server/testmedia/Supergiant Games - Bastion Original Soundtrack/Supergiant Games - Bastion Original Soundtrack - 01 Get Used to It.mp3"))
(defn get-song-data [id]
  (io/file (:path (some #(when (= (str (:id %)) id) %) (flatten (map :songs (get-indexed-files-in-folder "")))))))

;(id3/with-mp3 [mp3 "/home/tokuogum/Clojure/clojure-media-server/testmedia/Supergiant Games - Bastion Original Soundtrack/Supergiant Games - Bastion Original Soundtrack - 01 Get Used to It.mp3"] (:tag mp3))
                                        ;(map #(.isDirectory %) (.listFiles (io/file music-library-home)))
;(clojure.pprint/pprint (map #(.list %) (.listFiles (io/file music-library-home))))

