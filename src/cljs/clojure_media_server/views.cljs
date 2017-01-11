(ns clojure-media-server.views
  (:require [reagent.core  :as reagent]
            [re-frame.core :refer [subscribe dispatch]]))

(defn clickable-link [{id :id, title :title}]
  [:li title
   [:label {:id id
            :on-mouse-over #()
            :on-click #()}]
   [:br]])

(defn display-song [song]
  ^{:key (:id (:track-number song))} [clickable-link {:id (:track-number song) :title (:title song)}])

(defn display-album [album-name album-id selected?]
  (let [opened (reagent/atom false)]
    [:ul {:on-click (fn [e]
                      (dispatch [:toggle-album-display album-id])
                      (dispatch [:get-album-songs album-id])) }
     album-name
     (when selected?
       (for [song (sort-by #(js/parseInt (:track-number %)) ((keyword (str album-id)) @(subscribe [:album-songs])))]
       ^{:key (:title song)}[display-song song]))]))

(defn show-albums []
  [:div
   (doall (for [album (sort-by :name @(subscribe [:albums]))]
            ^{:key (:id album)} [display-album (:name album) (:id album) (:selected? album)]))])

(defn audio-player-tag []
  [:div
   [:label (song-title) ][:br]
   [:label (song-track-number)][:br]
   [:audio {:id "audiotag" :controls true :src @currently-playing-song}]])

(defn home-page []
  [:div [:h2 "Clojure media server"]
   [audio-player-tag]
   [show-albums]])

(defn about-page []
  [:div [:h2 "A clojure-media-server"]
   [:div [:a {:href "/"} "go to the home page"]]])
