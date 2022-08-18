(ns dunder-mifflin.lesson2
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

;(d.db/upsert-products! conn [{:product/id (:product/id first-product) :product/price 20M}])

(d.db/one-product (d/db conn) (:product/id first-product))

(pprint @(d.db/update-price conn (:product/id first-product) 1500.50M 30M))
(pprint @(d.db/update-price conn (:product/id first-product) (:product/price first-product) 35M))
(pprint @(d.db/update-price conn (:product/id first-product) 30M 45M))
(pprint @(d.db/update-price conn (:product/id first-product) (:product/price first-product) 1500.89M))

;this is bad because we miss schema validations
;(d.db/update-product conn [:product/price 20M 30])

(def second-product (second all-products))
(pprint second-product)
(def to-update {:product/id (:product/id second-product) :product/price 3000M, :product/inventory 48})


(d.db/update-product conn second-product to-update)

;try again, but shouldn't work
(pprint (d.db/update-product conn second-product to-update))