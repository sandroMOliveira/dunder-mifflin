(ns dunder-mifflin.lesson3
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [dunder-mifflin.db.dao :as d.db]
            [dunder-mifflin.model.products :as d.model]
            [schema.core :as s]))

(d.db/erase-db!)
(s/set-fn-validation! true)

(def conn (d.db/connection!))
(d.db/create-schema! conn)

(d.db/create-sample-data conn)

(pprint (d.db/all-categories (d/db conn)))
(def all-products (d.db/all-products (d/db conn)))
(def first-product (first all-products))
(pprint first-product)

(pprint @(d.db/add-variation! conn (:product/id first-product) "Recyclable" 120M))
(pprint @(d.db/add-variation! conn (:product/id first-product) "Recyclable and reforested" 150M))

(d.db/one-product (d/db conn) (:product/id first-product))

(d.db/total-of-products (d/db conn))

(pprint @(d.db/remove-product conn (:product/id first-product)))

(d.db/total-of-products (d/db conn))

(d/entity (d/db conn) [:product/id (:product/id first-product)])

(pprint (d/q '[:find ?name
               :where [_ :variation/name ?name]] (d/db {conn})))
