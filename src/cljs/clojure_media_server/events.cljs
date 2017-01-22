(ns clojure-media-server.events
  (:require [clojure-media-server.db :refer [default-value]]
            [ajax.core :refer [GET POST]]
            [clojure.zip :as zip]
            [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx path trim-v
                                  after debug dispatch]]))

(reg-event-db
   :bad-response
   (fn [_ _]
     (.log js/console (str "something bad happened"))))

(reg-event-db
   :process-response
   (fn [db [_ a]]
       (assoc db :albums (js->clj a))))

(reg-event-db
   :get-albums
   (fn [db [_ a]]
       (GET "/album"   ;; dirty great big side-effect
            {:handler       #(dispatch [:process-response %1])
             :error-handler #(dispatch [:bad-response %1])})  
     (assoc db :flag true)))

(reg-event-db
   :process-album-song-response
   (fn [db [_ response album-id]]
       (assoc-in db [:album-songs (keyword (str album-id))] (js->clj response))))

(reg-event-db
   :get-album-songs
   (fn [db [_ album-id]]
     (when-not (get-in db [:album-songs (keyword (str album-id))])
      (GET (str "/song/album/" album-id)
          {:handler #(dispatch [:process-album-song-response %1 album-id])
           :error-handler #(dispatch [:bad-response %1])}))
     (assoc db :flag true)))

(reg-event-fx                     ;; on app startup, create initial state
  :initialise-db                  ;; event id being handled
  (fn [_ _]                    ;; the handler being registered
    default-value))  ;; all hail the new state

(reg-event-db
   :toggle-album-display
   (fn [db [_ album-id]]
     (assoc db :albums
               (map #(if (= album-id (:id %))
                       (assoc % :selected? (not (:selected? %)))
                       %) (:albums db)))))

(defn current-song-hash [song]
  {:id (:id song)
   :title (:title song)
   :track-number (:track-number song)
   :url (str "/song/" (:id song))})

(defn search-song-from-album-by-id [playlist id]
  (if (or (zip/end? playlist) (= (:id (zip/node playlist)) id))
    playlist
    (recur (zip/next playlist) id)))

(defn zip-album-playlist [playlist current-song]
  (let [playzip (zip/seq-zip (seq playlist))]
    (search-song-from-album-by-id playzip (:id current-song))))

(reg-event-db
   :update-current-song
   (fn [db [_ song]]
     (let [current-song (first (filter #(= song (:id %)) (flatten (vals (:album-songs db)))))
           current-album (first (first (filter #(some (fn [e] (= song (:id e))) (second %)) (:album-songs db))))]
       (assoc (assoc db :current-song (current-song-hash current-song))
              :playlist (zip-album-playlist (sort-by #(js/parseInt (:track-number %)) (current-album (:album-songs db))) current-song)))))

(reg-event-db
 :playlist-next-song
 (fn [db _]
   (let [next-song (zip/next (:playlist db))]
     (assoc (assoc db :current-song (current-song-hash (zip/node next-song)))
            :playlist next-song))))

(reg-event-db
 :playlist-previous-song
 (fn [db _]
   (let [prev-song (zip/prev (:playlist db))]
     (assoc (assoc db :current-song (current-song-hash (zip/node prev-song)))
            :playlist prev-song))))
