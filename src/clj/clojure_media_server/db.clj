(ns clojure-media-server.db
  (:require [hugsql.core :as hugsql]))

(def db
  {:classname   "org.sqlite.JDBC",
   :subprotocol "sqlite",
   :subname     "test.db"})
