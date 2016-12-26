(ns clojure-media-server.middleware
  (:require [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.json :refer [wrap-json-params wrap-json-body]]
            [ring.middleware.transit :refer [wrap-transit-response]]))

(defn wrap-middleware [handler]
  (-> handler
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))
      (wrap-transit-response {:encoding :json, :opts {}})))
