(ns clojure-media-server.views
  (:require [reagent.core  :as reagent]
            [goog.string :as gstring]
            [re-frame.core :refer [subscribe dispatch]]))

(defn clickable-link [{id :id, title :title}]
  [:li.song {:on-click (fn [e]
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
  [:ul.album {:on-click (fn [e]
                          (dispatch [:toggle-album-display album-id])
                          (dispatch [:get-album-songs album-id]))}
   album-name
   (when selected?
     (for [song (sort-by #(js/parseInt (:track-number %)) ((keyword (str album-id)) @(subscribe [:album-songs])))]
       ^{:key (:title song)}[display-song song]))])

(defn show-albums []
  [:div.song-list
   (doall (for [album (sort-by :name @(subscribe [:albums]))]
            ^{:key (:id album)} [display-album (:name album) (:id album) (:selected? album)]))])

(defn audio-player-inner [data artist title track-number]
  (let [!audio (atom nil) !event-handler-added? (atom nil)]
    [:div.musicplayer
     [:div
      [:div
       [:label.musictitle (str "Artist: " artist)]
       [:label.musictitle (str " Title: " title)]
       [:label.musictitle (str " Track-number: " track-number)] 
       [:audio {:src data
                :id "audiotag"
                :ref (fn [el]
                       (reset! !audio el)
                       (when-let [audio @!audio]
                         (dispatch [:add-music-listener audio])))
                :controls true
                :autoplay true}]]
      [:div
       [:button {:on-click #(dispatch [:playlist-previous-song])}
        "Previous"]
       [:button {:on-click (fn []
                              (when-let [audio @!audio] ;; not nil?
                                (if (.-paused audio)
                                  (.play audio)
                                  (.pause audio))))}
         "Toggle"]
       [:button {:on-click #(dispatch [:playlist-next-song])}
        "Next"]]]]))

(defn audio-player-outer []
  (let [data (subscribe [:current-song-data])
        song-artist (subscribe [:current-song-artist])
        song-title (subscribe [:current-song-name])
        song-track-number (subscribe [:current-song-track-number])]
    (fn []
      [audio-player-inner @data @song-artist @song-title @song-track-number])))

(defn refresh-icon [])

(defn animated-refresh-icon [])

(defn refresh-database []
  [:label [:i.fa.fa-refresh {:aria-hidden "true"
                      :on-click #(.log js/console "Päivittää")}]"Päivitä tietokanta "])

(defn search-box []
  [:label [:i.fa.fa-search][:input {:type "text"}]])

(defn upper-menu-box []
  [:div [refresh-database](gstring/unescapeEntities "&nbsp;")[search-box]])

(defn album-art-display []
  [:img.album-art {:src @(subscribe [:current-song-album-art])}])

(defn home-page []
  [:div [:h2 "Clojure media server"]
   [audio-player-outer]
   [album-art-display]
   [upper-menu-box]
   [show-albums]])

(defn about-page []
  [:div [:h2 "A clojure-media-server"]
   [:div [:a {:href "/"} "go to the home page"]]])
