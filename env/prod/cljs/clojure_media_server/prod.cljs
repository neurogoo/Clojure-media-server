(ns clojure-media-server.prod
  (:require [clojure-media-server.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
