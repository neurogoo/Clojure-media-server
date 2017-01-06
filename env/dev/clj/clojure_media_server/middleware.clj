(ns clojure-media-server.middleware
  (:require [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [prone.middleware :refer [wrap-exceptions]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.json :refer [wrap-json-params wrap-json-body]]
            [ring.middleware.transit :refer [wrap-transit-response]]
            [ring.middleware.webjars :refer [wrap-webjars]]))

(defn wrap-middleware [handler]
  (-> handler
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))
      wrap-exceptions
      wrap-reload
      wrap-webjars
      (wrap-transit-response {:encoding :json, :opts {}})))
