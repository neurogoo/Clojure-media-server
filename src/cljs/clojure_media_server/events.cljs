(ns clojure-media-server.events
  (:require [clojure-media-server.db :refer [default-value]]
            [ajax.core :refer [GET POST]]              
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

(reg-event-db
   :update-current-song
   (fn [db [_ song]]
     (let [current-song (first (filter #(= song (:id %)) (flatten (vals (:album-songs db)))))
           current-album (first (first (filter #(some (fn [e] (= song (:id e))) (second %)) (:album-songs db))))]
       (assoc (assoc db :current-song {:id song
                                       :title (:title current-song)
                                       :track-number (:track-number current-song)
                                       :url (str "/song/" song)})
              :playlist (current-album (:album-songs db))))))

(reg-event-db
 :playlist-next-song
 (fn [db _]
   (let [current-song-id (:id (:current-song db))]
     (second (drop-while #(not= current-song-id (:id %)) (:playlist db))))))
