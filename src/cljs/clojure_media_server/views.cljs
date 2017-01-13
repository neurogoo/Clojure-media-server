(ns clojure-media-server.views
  (:require [reagent.core  :as reagent]
            [re-frame.core :refer [subscribe dispatch]]))

(defn clickable-link [{id :id, title :title}]
  [:li {:on-click (fn [e]
                    (dispatch [:update-current-song id])
                    (.stopPropagation e))}
   title
   [:label {:id id
            :on-mouse-over #()
            }]
   [:br]])

(defn display-song [song]
  ^{:key (:id (:id song))} [clickable-link {:id (:id song) :title (:title song)}])

(defn display-album [album-name album-id selected?]
  (let [opened (reagent/atom false)]
    [:ul {:on-click (fn [e]
                      (dispatch [:toggle-album-display album-id])
                      (dispatch [:get-album-songs album-id]))}
     album-name
     (when selected?
       (for [song (sort-by #(js/parseInt (:track-number %)) ((keyword (str album-id)) @(subscribe [:album-songs])))]
       ^{:key (:title song)}[display-song song]))]))

(defn show-albums []
  [:div
   (doall (for [album (sort-by :name @(subscribe [:albums]))]
            ^{:key (:id album)} [display-album (:name album) (:id album) (:selected? album)]))])

(defn audio-player-inner [data title track-number]
  (let [!audio (atom nil)]
    [:div
     [:div
      [:div (str "Title: " title)] 
      [:div (str "Track-number: " track-number)]
      [:audio {:src data
               :ref (fn [el]
                        (reset! !audio el))
               :controls true}]
      [:div
        [:button {:on-click (fn []
                              (when-let [audio @!audio] ;; not nil?
                                (if (.-paused audio)
                                  (.play audio)
                                  (.pause audio))))}
         "Toogle"]]]]))

(defn audio-player-outer []
  (let [data (subscribe [:current-song-data])
        song-title (subscribe [:current-song-name])
        song-track-number (subscribe [:current-song-track-number])]
    (fn []
      [audio-player-inner @data @song-title @song-track-number])))

(defn home-page []
  [:div [:h2 "Clojure media server"]
   [audio-player-outer]
   [show-albums]])

(defn about-page []
  [:div [:h2 "A clojure-media-server"]
   [:div [:a {:href "/"} "go to the home page"]]])
