(ns clojure-media-server.middleware
  (:require [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.json :refer [wrap-json-params wrap-json-body]]
            [ring.middleware.transit :refer [wrap-transit-response]]
            [ring.middleware.webjars :refer [wrap-webjars]]))

(defn wrap-middleware [handler]
  (-> handler
      (wrap-defaults (-> site-defaults
                         (assoc-in [:security :anti-forgery] false)
                         (assoc-in [:security :ssl-redirect] true)
                         (assoc :proxy true)
                         ))
      wrap-webjars
      (wrap-transit-response {:encoding :json, :opts {}})))
