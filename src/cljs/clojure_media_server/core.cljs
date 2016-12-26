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
(defn update-currently-playing-song [song-id]
  (reset! currently-playing-song-id (str song-id))
  (reset! currently-playing-song (clojure.string/join ["http://localhost:3449/song/" (str song-id)]))
  (update-currently-playing-song-metadata)
  (.play (.getElementById js/document "audiotag")))

(defn clickable-link [{id :id, title :title}]
  [:li {:display "none" }
   [:label {:id id
                :on-mouse-over #()
                :on-click #(update-currently-playing-song id)} title] [:br]])

(defn get-folder-list []
  (POST "/files"
        {:handler handler
         :error-handler error-handler
         :params {"folder" "/home/tokuogum/Clojure/clojure-media-server/testmedia"}}))
(defn display-album [album songs]
  (reagent/with-let [opened (atom false)]
    [:ul {:on-click #(swap! opened not)}
     album
     (when @opened
       (for [song (sort-by #(js/parseInt (:track-number %)) songs)]
       ^{:key (:id song)} [clickable-link song]))]))

(defn albums []
  [:div
   (doall (for [{album :album
                 songs :songs} @files]
            [display-album album songs]))])

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

(defn playlist []
  [:div])

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
