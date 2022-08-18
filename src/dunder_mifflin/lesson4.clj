(ns dunder-mifflin.lesson4
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

(dotimes [n 10] (d.db/visualization! conn (:product/id first-product)))

(d.db/one-product (d/db conn) (:product/id first-product))
