(ns dunder-mifflin.db.sell
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [dunder-mifflin.model.products :as d.model]))

(defn add [conn product-id quantity]
  (let [id (d.model/uuid)]
    (d/transact conn [{:db/id         "sell"
                       :sell/product  [:product/id product-id]
                       :sell/quantity quantity
                       :sell/id       id}])
    id))

(defn all [db]
  (d/q '[:find [(pull ?s [*, {:sell/product [:product/name :product/price]}]) ...]
         :where [?s :sell/quantity ?quantity]] db))

(defn- instant-of-sell [db sell-id]
  (d/q '[:find ?instant .
         :in $ ?id
         :where [_ :sell/id ?id ?tx true]
         [?tx :db/txInstant ?instant]] db sell-id))

(defn cost [db sell-id]
  (let [sell-instant (instant-of-sell db sell-id)]
    (d/q '[:find (sum ?price-per-product) .
           :in $ ?id
           :where [?s :sell/id ?id]
           [?s :sell/quantity ?quantity]
           [?s :sell/product ?p]
           [?p :product/price ?price]
           [(* ?price ?quantity) ?price-per-product]] (d/as-of db sell-instant) sell-id)))

(defn total [db]
  (d/q '[:find (sum ?quantity) .
         :where [?s :sell/quantity ?quantity]] db))

(defn total-by-product [db product-id]
  (d/q '[:find (sum ?quantity) .
         :in $ ?product-id
         :where [?s :sell/quantity ?quantity]
         [?s :sell/product ?product]
         [?product :product/id ?product-id]] db product-id))

(defn cancel! [conn sell-id]
  (d/transact conn [[:db/retractEntity [:sell/id sell-id]]]))