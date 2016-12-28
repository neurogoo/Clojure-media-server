(ns clojure-media-server.middleware
  (:require [ring.middleware.defaults :refer [secure-site-defaults wrap-defaults]]
            [ring.middleware.json :refer [wrap-json-params wrap-json-body]]
            [ring.middleware.transit :refer [wrap-transit-response]]))

(defn wrap-middleware [handler]
  (-> handler
      (wrap-defaults (-> secure-site-defaults
                         (assoc-in [:security :anti-forgery] false)
                         (assoc-in [:security :ssl-redirect] true)
                         (assoc :proxy true)))
      (wrap-transit-response {:encoding :json, :opts {}})))
