(ns clojure-media-server.middleware
  (:require [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [prone.middleware :refer [wrap-exceptions]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.json :refer [wrap-json-params wrap-json-body]]
            [ring.middleware.transit :refer [wrap-transit-response]]))

(defn wrap-middleware [handler]
  (-> handler
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))
      wrap-exceptions
      wrap-reload
      (wrap-transit-response {:encoding :json, :opts {}})))
