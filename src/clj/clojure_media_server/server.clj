(ns clojure-media-server.server
  (:require [clojure-media-server.handler :refer [app]]
            [config.core :refer [env]]
            [mount.core :as mount]
            [clojure-media-server.database :refer [db]]
            [org.httpkit.server :refer [run-server]])
  (:gen-class))

(defn -main [& args]
  (mount/start #'clojure-media-server.database/db)
  (let [port (Integer/parseInt (or (env :port) "3000"))]
    (do
      (run-server app {:port port :join? false}))))
