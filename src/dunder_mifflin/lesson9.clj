(ns dunder-mifflin.lesson9
 (:use clojure.pprint)
 (:require [datomic.api :as d]
           [dunder-mifflin.db.config :as d.config]
           [dunder-mifflin.db.product :as d.product]
           [schema.core :as s]
           [dunder-mifflin.db.sell :as d.sell]))

(d.config/erase-db!)
(s/set-fn-validation! true)

(def conn (d.config/connection!))
(d.config/create-schema! conn)
(d.config/create-sample-data conn)

(def all-products (d.product/all (d/db conn)))
(def first-product (first all-products))
(pprint first-product)

(def sell1 (d.sell/add conn (:product/id first-product) 5))
(def sell2 (d.sell/add conn (:product/id first-product) 20))




(pprint (d.sell/all (d/db conn)))

(pprint @(d.sell/cancel! conn sell2))

(->> (d.sell/all (d/db conn))
     (mapv #(:sell/id %))
     (mapv #(d.sell/cancel! conn %)))