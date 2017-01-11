(ns clojure-media-server.core
    (:require [reagent.core :as r]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [re-frame.core :as rf]
              [goog.string :as gstring]
              [clojure-media-server.events]
              [clojure-media-server.subs]
              [clojure-media-server.views]))

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'clojure-media-server.views/home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'clojure-media-server.views/about-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (r/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (rf/dispatch-sync [:initialise-db])
  (rf/dispatch [:get-albums])
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
