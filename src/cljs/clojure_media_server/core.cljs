(ns clojure-media-server.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [ajax.core :refer [GET POST]]))
;; -----------------
;; Misc
(def files (atom {}))
(def currently-playing-song (atom '()))
(def currently-playing-song-id (atom '()))
(def currently-playing-song-metadata (atom {}))
(def playlist (atom []))

(defn handler [response]
  (.log js/console (str response))
  (def temparvo response)
  (reset! files (js->clj response)))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn update-currently-playing-song-metadata []
  (GET (clojure.string/join ["/song/" (str @currently-playing-song-id) "/data"])
       {:handler (fn [response]
                   (reset! currently-playing-song-metadata (js->clj response)))
        :error-handler error-handler
        :keywords? true
        }))
;(defn build-playlist-from-album [song-id]
;  (let [album (first )]))

(defn album-songs-sorted [album]
  (let [songs (:songs @files)]
    (sort-by #(js/parseInt (:track-number (second %))) (filter #(= album (:album (second %))) songs))))

(defn update-currently-playing-song [song-id]
  (reset! currently-playing-song-id (name song-id))
  (reset! currently-playing-song (clojure.string/join ["/song/" (name song-id)]))
  (update-currently-playing-song-metadata)
  (.addEventListener (.getElementById js/document "audiotag") "ended"
                     #(update-currently-playing-song (key (second (drop-while (fn [song] (not= song-id (first song))) (album-songs-sorted (get-in @files [:songs song-id :album])))))))
  (js/setTimeout (.play (.getElementById js/document "audiotag")) 1000))

(defn clickable-link [{id :id, title :title}]
  [:li {:display "none" }
   [:label {:id id
            :on-mouse-over #()
            :on-click (fn [e] (update-currently-playing-song id)
                        (.stopPropagation e))} title]
   [:br]])

(defn get-folder-list []
  (POST "/files"
        {:handler handler
         :error-handler error-handler
         :params {"folder" ""}}))

(defn display-album [album songs]
  (reagent/with-let [opened (atom false)]
    [:ul {:on-click #(swap! opened not)}
     album
     (when @opened
       (for [song (sort-by #(js/parseInt (:track-number (second %))) (filter #(= album (:album (second %))) songs))]
       ^{:key (:id (key song))} [clickable-link {:id (key song) :title (:title (val song))}]))]))

(defn albums []
  [:div
   (let [songs (:songs @files)]
     (for [album (:albums @files)]
       [display-album (second album) songs]))])

;; -------------------------
;; Views

(defn playlist-video []
  [:video {:controls true :src "http://v2v.cc/~j/theora_testsuite/320x240.ogg"} "Your browser does not support html 5 video"])

(defn song-title []
  (str "Title: " (:title @currently-playing-song-metadata) ))

(defn song-track-number []
  (str "Track number: " (:track-number @currently-playing-song-metadata)))

(defn audio-player-tag []
  [:div
   [:label (song-title) ][:br]
   [:label (song-track-number)][:br]
   [:audio {:id "audiotag" :controls true :src @currently-playing-song}]])

(defn home-page []
  [:div [:h2 "Clojure media server"]
                                        ;[:div [:a {:href "/about"} "go to about page"]]
   [audio-player-tag]
   [albums]])

(defn about-page []
  [:div [:h2 "A clojure-media-server"]
   [:div [:a {:href "/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
(get-folder-list)
