(ns clojure-media-server.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub
 :showing
 (fn [db _]        ;; db is the (map) value in app-db
   (:showing db))) ;; I repeat:  db is a value. Not a ratom.  And this fn does not return a reaction, just a value.
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
