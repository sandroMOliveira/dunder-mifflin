(ns dunder-mifflin.lesson6
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [dunder-mifflin.db.config :as d.config]
            [dunder-mifflin.db.product :as d.product]
            [schema.core :as s]))

(d.config/erase-db!)
(s/set-fn-validation! true)

(def conn (d.config/connection!))
(d.config/create-schema! conn)
(d.config/create-sample-data conn)

(def all-products (d.product/all (d/db conn)))
(def first-product (first all-products))
(pprint all-products)
