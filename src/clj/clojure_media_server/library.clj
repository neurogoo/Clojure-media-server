(ns clojure-media-server.library
  (:require [clojure.java.io :as io]
            [clojure-media-server.database :refer [db]]
            [id3]))
(defn get-files-in-folder [folder]
  db)
(defn get-playlist []
  ())
(defn get-song []
  (io/file "/home/tokuogum/Clojure/clojure-media-server/testmedia/Supergiant Games - Bastion Original Soundtrack/Supergiant Games - Bastion Original Soundtrack - 01 Get Used to It.mp3"))
(defn get-song-data [id]
  (io/file (:path (some #(when (= (str (:id %)) id) %) (flatten (map :songs db))))))
(defn get-song-metadata [id]
  (select-keys (some #(when (= (str (:id %)) (str id)) %) (flatten (map :songs db))) [:title :track-number]))

;(id3/with-mp3 [mp3 "/home/tokuogum/Clojure/clojure-media-server/testmedia/Supergiant Games - Bastion Original Soundtrack/Supergiant Games - Bastion Original Soundtrack - 01 Get Used to It.mp3"] (:tag mp3))
                                        ;(map #(.isDirectory %) (.listFiles (io/file music-library-home)))
;(clojure.pprint/pprint (map #(.list %) (.listFiles (io/file music-library-home))))

