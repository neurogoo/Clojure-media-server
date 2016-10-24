(ns clojure-media-server.library
  (:gen-class))
(def music-library-home "/home/tokuogum/Clojure/clojure-media-server/testmedia")
(defn test-print []
  (let [files (file-seq (clojure.java.io/file music-library-home))]
    (doseq [x files]
      (clojure.pprint/pprint x))))
(defn get-sorted-files-in-folder [folder]
  (sort (filter #(re-matches #"(.*).mp3$" %) (.list (clojure.java.io/file music-library-home)))))
(defn get-indexed-files-in-folder [folder]
  (map-indexed (fn [idx i] {(keyword (str idx)) i}) (get-sorted-files-in-folder folder)))
(defn get-files-in-folder [folder]
  (get-indexed-files-in-folder folder))
(defn get-playlist []
  )
(defn get-song []
  (clojure.java.io/file "/home/tokuogum/Clojure/clojure-media-server/testmedia/Supergiant Games - Bastion Original Soundtrack - 02 A Proper Story.mp3"))
(defn get-song-data [id]
  (clojure.java.io/file (clojure.string/join ["/home/tokuogum/Clojure/clojure-media-server/testmedia/" ((keyword id)(into {} (get-indexed-files-in-folder ""))) ])))
