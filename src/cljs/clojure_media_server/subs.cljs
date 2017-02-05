(ns clojure-media-server.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub
 :showing
 (fn [db _]        ;; db is the (map) value in app-db
   (:showing db))) ;; I repeat:  db is a value. Not a ratom.  And this fn does not return a reaction, just a value.

(reg-sub
 :current-view
 (fn [db _]
   (:current-view db)))

(reg-sub
 :albums
 (fn [db _]
   (:albums db)))

(reg-sub
 :album-songs
 (fn [db _]
   (:album-songs db)))

(reg-sub
 :current-song
 (fn [db _]
   (:current-song db)))

(reg-sub
 :current-song-name
 (fn [_ _]  
   (subscribe [:current-song]))
 (fn [song _]
   (:title song)))

(reg-sub
 :current-song-track-number
 (fn [_ _]  
   (subscribe [:current-song]))
 (fn [song _]
   (:track-number song)))

(reg-sub
 :current-song-data
 (fn [_ _]  
   (subscribe [:current-song]))
 (fn [song _]
   (:url song)))

(reg-sub
 :current-song-album-art
 (fn [_ _]  
   (subscribe [:current-song]))
 (fn [song _]
   (str (:url song) "/art")))

(reg-sub
 :current-song-artist
 (fn [_ _]  
   (subscribe [:current-song]))
 (fn [song _]
   (:artist song)))

(reg-sub
 :playlist
 (fn [db _]
   (:playlist db)))
