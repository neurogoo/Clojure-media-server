(ns clojure-media-server.handler
  (:use [ring.util.response :only [response]])
  (:require [compojure.core :refer [GET POST defroutes context]]
            [compojure.route :refer [not-found resources]]
            [hiccup.page :refer [include-js include-css html5]]
            [clojure-media-server.middleware :refer [wrap-middleware]]
            [clojure-media-server.library :as mylib]
            [clojure-media-server.database :as database]
            [mount.core :as mount]
            [clojure.java.io :as io]
            [ring.util.io :refer [piped-input-stream]]
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
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))
   (include-css "/assets/font-awesome/css/font-awesome.min.css")])

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
   :body (database/get-song-data id)})

(defn return-song-metadata [id]
  (mylib/get-song-metadata id))

(defroutes routes
  (GET "/" [] (loading-page))
  (GET "/about" [] (loading-page))
  (GET "/song" [] {:status 200
                   :headers {"Content-Type" "application/transit+json; charset=UTF-8"}
                   :body (database/get-songs)})
  (GET "/song/album/:id" [id] {:status 200
                               :headers {"Content-Type" "application/transit+json; charset=UTF-8"}
                               :body (database/get-album-songs id)})
  (context "/song/:id" [id]
           (GET "/" [] (return-song-data id))
           (GET "/daa" [] (return-song-metadata id)))
  (GET "/song/:id/data" [id] {:status 200
                              :headers {"Content-Type" "application/transit+json; charset=UTF-8"}
                              :body (return-song-metadata id)})
  (GET "/song/:id/art" [id] {:status 200
                             :headers {"Content-Type" "image/jpeg"}
                             :body (database/get-song-album-art id)})
  (POST "/files" [req] {:status 200
                        :headers {"Content-Type" "application/transit+json; charset=UTF-8"}
                        :body (return-files-in-folder (get-in req [:params "folder"]))}#_(response (return-files-in-folder (get-in req [:params "folder"]))))
  (GET "/album" [] {:status 200
                    :headers {"Content-Type" "application/transit+json; charset=UTF-8"}
                    :body (database/get-albums)})
  (GET "/conversiontest" [] {:status 200
                             :headers {"Content-Type" "application/ogg"}
                             :body (response (piped-input-stream
                                    #(io/copy (database/flac->mp3-test) % :buffer-size 1)))})  
  (resources "/")
  (not-found "Not Found"))

(def app (wrap-middleware #'routes))
