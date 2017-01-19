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
  (let [!audio (atom nil) !event-handler-added? (atom nil)]
    [:div
     [:div
      [:div (str "Title: " title)] 
      [:div (str "Track-number: " track-number)]
      [:audio {:src data
               :id "audiotag"
               :ref (fn [el]
                      (reset! !audio el)
                      (when-let [audio @!audio]
                        (when (not @!event-handler-added?)
                          (.log js/console "Event handler lisätty")
                          (reset! !event-handler-added? nil)
                          (.addEventListener audio "loadstart" #(.play audio))
                          (.addEventListener audio "ended" (fn [e]
                                                             (dispatch [:playlist-next-song audio])
                                                             (.play audio))))))
               :controls true
               :autoplay true}]
      [:div
       [:button {}
        "Previous"]
       [:button {:on-click (fn []
                              (when-let [audio @!audio] ;; not nil?
                                (if (.-paused audio)
                                  (.play audio)
                                  (.pause audio))))}
         "Toogle"]
       [:button {:on-click #(dispatch [:playlist-next-song])}
        "Next"]]]]))

(defn audio-player-outer []
  (let [data (subscribe [:current-song-data])
        song-title (subscribe [:current-song-name])
        song-track-number (subscribe [:current-song-track-number])]
    (fn []
      [audio-player-inner @data @song-title @song-track-number])))

(defn refresh-icon [])

(defn animated-refresh-icon [])

(defn refresh-database []
  [:label "Päivitä tietokanta "
   [:i.fa.fa-refresh {:aria-hidden "true"
                      :on-click #(.log js/console "Päivittää")}]])

(defn home-page []
  [:div [:h2 "Clojure media server"]
   [audio-player-outer]
   [refresh-database]
   [show-albums]])

(defn about-page []
  [:div [:h2 "A clojure-media-server"]
   [:div [:a {:href "/"} "go to the home page"]]])
