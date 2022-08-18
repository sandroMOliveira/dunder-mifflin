(ns dunder-mifflin.lesson5
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

(def all-products (d.db/all-products (d/db conn)))
(def first-product (first all-products))
(pprint first-product)

(def inc-views
  #db/fn {:lang   :clojure
          :params [db product-id]
          :code
          (let [views (d/q '[:find ?v .
                            :in $ ?p-id
                            :where [?p :product/id ?p-id]
                            [?p :product/views ?v]] db product-id)
                actual (or views 0)
                new-total (inc actual)]
            [{:product/id    product-id
              :product/views new-total
              }])})

;install function in datomic
(pprint @(d/transact conn [{:db/ident :inc-views
                           :db/fn    inc-views
                           :db/doc   "Inc entity views attribute"}]))

(pprint first-product)

;(dotimes [n 10] (d.db/visualiztion! conn (:product/id first-product)))
;(d.db/one-product (d/db conn) (:product/id first-product))
