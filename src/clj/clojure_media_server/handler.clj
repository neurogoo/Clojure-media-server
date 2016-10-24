(ns clojure-media-server.handler
  (:use [ring.util.response :only [response]])
  (:require [compojure.core :refer [GET POST defroutes]]
            [compojure.route :refer [not-found resources]]
            [hiccup.page :refer [include-js include-css html5]]
            [clojure-media-server.middleware :refer [wrap-middleware]]
            [clojure-media-server.library :as mylib]
            [config.core :refer [env]]))

(def mount-target
  [:div#app
      [:h3 "ClojureScript has not been compiled!"]
      [:p "please run "
       [:b "lein figwheel"]
       " in order to start the compiler"]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn loading-page []
  (html5
    (head)
    [:body {:class "body-container"}
     mount-target
     (include-js "/js/app.js")]))

(defn song []
  {:status 200
   :headers {"Content-Type" "audio/mpeg"}
   :body (mylib/get-song)})

(defn return-files-in-folder [folder]
  (mylib/get-files-in-folder folder))

(defn return-song-data [id]
  {:status 200
   :headers {"Content-Type" "audio/mpeg"}
   :body (mylib/get-song-data id)})

(defroutes routes
  (GET "/" [] (loading-page))
  (GET "/about" [] (loading-page))
  ;(GET "/song" [] (song))
  (GET "/song/:id" [id] (return-song-data id))
  (POST "/files" [req] (return-files-in-folder (get-in req [:params "folder"])))
  
  (resources "/")
  (not-found "Not Found"))

(def app (wrap-middleware #'routes))
