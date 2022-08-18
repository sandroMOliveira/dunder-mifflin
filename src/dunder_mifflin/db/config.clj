(ns dunder-mifflin.db.config
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [dunder-mifflin.model.products :as d.model]
            [dunder-mifflin.db.product :as d.products]))

(def db-uri "datomic:dev://localhost:4334/dunder-mifflin")

(defn connection! []
  (d/create-database db-uri)
  (d/connect db-uri))

(defn erase-db!
  []
  (d/delete-database db-uri))

(def schema [{:db/ident       :product/id
              :db/valueType   :db.type/uuid
              :db/cardinality :db.cardinality/one
              :db/unique      :db.unique/identity
              :db/doc         "Uuid to find juices"}
             {:db/ident       :product/name
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "Product name"}
             {:db/ident       :product/slug
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/doc         "Path to access the product via http"}
             {:db/ident       :product/price
              :db/valueType   :db.type/bigdec
              :db/cardinality :db.cardinality/one
              :db/doc         "Price the product with monetary precision"}
             {:db/ident       :product/key-word
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/many}
             {:db/ident       :product/category
              :db/valueType   :db.type/ref
              :db/cardinality :db.cardinality/one}
             {:db/ident       :product/inventory
              :db/valueType   :db.type/long
              :db/cardinality :db.cardinality/one}
             {:db/ident       :product/digital
              :db/valueType   :db.type/boolean
              :db/cardinality :db.cardinality/one}
             {:db/ident       :product/variations
              :db/valueType   :db.type/ref
              :db/isComponent true
              :db/cardinality :db.cardinality/many}
             {:db/ident       :product/views
              :db/valueType   :db.type/long
              :db/cardinality :db.cardinality/one}

             ;variations
             {:db/ident       :variation/id
              :db/valueType   :db.type/uuid
              :db/cardinality :db.cardinality/one
              :db/unique      :db.unique/identity}
             {:db/ident       :variation/name
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one}
             {:db/ident       :variation/price
              :db/valueType   :db.type/bigdec
              :db/cardinality :db.cardinality/one}

             ;category
             {:db/ident       :category/id
              :db/valueType   :db.type/uuid
              :db/cardinality :db.cardinality/one
              :db/unique      :db.unique/identity}
             {:db/ident       :category/name
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one
              :db/noHistory   true}

             ;sell
             {:db/ident       :sell/id
              :db/valueType   :db.type/uuid
              :db/cardinality :db.cardinality/one
              :db/unique      :db.unique/identity}
             {:db/ident       :sell/product
              :db/valueType   :db.type/ref
              :db/cardinality :db.cardinality/one}
             {:db/ident       :sell/quantity
              :db/valueType   :db.type/long
              :db/cardinality :db.cardinality/one}

             ;transaction
             {:db/ident       :tx-data/ip
              :db/valueType   :db.type/string
              :db/cardinality :db.cardinality/one}])

(defn create-schema! [conn] (d/transact conn schema))

  ;(defn all-products
;explict pull
;  [snapshot]
;  (d/q '[:find (pull ?entity [:product/name :product/price :product/slug])
;         :where [?entity :product/name]] snapshot))

;forward navigation example
;without (_) to reference
;(defn all-products-of-category [snapshot category-name]
;  (d/q '[:find (pull ?product [:product/name :product/slug {:product/category [:category/name]}])
;         :in $, ?name-category
;         :where [?category :category/name ?name-category]
;                [?product :product/category ?category]] snapshot, category-name))



(defn create-sample-data [conn]
  (def papers (d.model/new-categories "Papers"))
  (def printers (d.model/new-categories "Printers"))
  (def online (d.model/new-categories "Online"))

  (d.products/add-categories! conn [papers, printers, online])

  (def paper-a4 (d.model/new-product "A4 Paper", "/paper/a4", 1500.50M, 10))
  (pprint paper-a4)
  (def butter-paper (d.model/new-product "Butter Paper", "/paper/butter", 5000.0M, 5))
  (def cheap-butter-paper (d.model/new-product "Cheap Butter Paper", "/paper/cheap/butter", 1.50M))
  (def epson-tx123 (d.model/new-product "Epson TX-123", "/printer/epson-tx123", 1510.50M))
  (def docs-online (assoc (d.model/new-product "Docs Online", "/online/docs", 100.0M) :product/digital true))

  (pprint @(d.products/upsert-products! conn [paper-a4, butter-paper, cheap-butter-paper, epson-tx123, docs-online] "0.0.0.0"))

  (d.products/set-categories! conn [paper-a4, butter-paper, cheap-butter-paper] papers)
  (d.products/set-categories! conn [epson-tx123] printers)
  (d.products/set-categories! conn [docs-online] online))
