(ns dunder-mifflin.lesson8
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

(pprint (d.sell/add conn (:product/id first-product) 5))
(pprint (d.sell/add conn (:product/id first-product) 20))

(pprint (d.sell/add conn (:product/id (second all-products)) 60))

(pprint (d.sell/all (d/db conn)))

(pprint (d.sell/total (d/db conn)))

(pprint (d.sell/total-by-product (d/db conn) (:product/id first-product)))
(pprint (d.sell/total-by-product (d/db conn) (:product/id (second all-products))))

(def all-sells (d.sell/all (d/db conn)))
(def sell1 (first all-sells))
(pprint sell1)
(pprint (d.sell/cost (d/db conn) (:sell/id sell1)))

(pprint first-product)
(pprint sell1)

(pprint (d.sell/add conn (:product/id first-product) 10))

(def sell1-n (first (d.sell/all (d/db conn))))
(pprint sell1)
