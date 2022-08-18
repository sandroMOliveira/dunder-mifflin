(ns dunder-mifflin.model.products
  (:import (java.util UUID))
  (:require [schema.core :as s]
            [dunder-mifflin.model.products :as d.model]))

(defn uuid
  []
  (UUID/randomUUID))

(def Category
  {:category/id   UUID
   :category/name s/Str})

(def Variation
  {:variation/id    UUID
   :variation/name  s/Str
   :variation/price BigDecimal})


(def Product
  {:product/id                          UUID
   (s/optional-key :product/name)       s/Str
   (s/optional-key :product/slug)       s/Str
   (s/optional-key :product/price)      BigDecimal
   (s/optional-key :product/key-word)   [s/Str]
   (s/optional-key :product/category)   Category
   (s/optional-key :product/inventory)  s/Int
   (s/optional-key :product/digital)    s/Bool
   (s/optional-key :product/variations) [Variation]
   (s/optional-key :product/views)      s/Int})

(def Sell
  {:sell/id                        UUID
   (s/optional-key :sell/product)  Product
   (s/optional-key :sell/quantity) s/Int})

(s/defn new-product :- d.model/Product
  ([name, slug, price]
   (new-product name slug price 0))
  ([name, slug, price, inventory]
   {:product/id        (d.model/uuid), :product/name name, :product/slug slug, :product/price price
    :product/inventory inventory :product/digital false}))

(defn new-categories
  [category]
  {:category/id (uuid), :category/name category})
