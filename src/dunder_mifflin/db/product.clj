(ns dunder-mifflin.db.product
  (:use clojure.pprint)
  (:require [datomic.api :as d]
            [dunder-mifflin.model.products :as d.model]
            [schema.core :as s]
            [clojure.set :as c.set]
            [dunder-mifflin.db.entity :as d.entity]))

(defn all-products-by-name
  [snapshot, name]
  (d/q '[:find ?entity
         :in $ ?name
         :where [?entity :product/name ?name]] snapshot name))

; the query order is from most restrictive to the least restrictive
(defn all-prices
  [snapshot min-price]
  (d/q '[:find (pull ?e [:product/name :product/price])
         :in $, ?min-price
         :where [?e :product/price ?price]
         [(> ?price ?min-price)]
         [?e :product/name ?name]] snapshot, min-price))

(defn all-products-by-key-word
  [snapshot key-word]
  (d/q '[:find (pull ?e [*])
         :in $, ?key-word
         :where [?e :product/key-word ?key-word]] snapshot, key-word))

(s/defn one :- (s/maybe d.model/Product)
  [snapshot, product-id :- java.util.UUID]
  (let [product (d/pull snapshot '[* {:product/category [:category/id :category/name]}] [:product/id product-id])
        product (d.entity/datomic-to-entity product)]
    (if (:product/id product)
      product
      nil)))

(s/defn one-product!! :- (s/maybe d.model/Product)
  [snapshot, product-id :- java.util.UUID]
  (let [product (one snapshot product-id)]
    (when (nil? product)
      (throw (ex-info "Entity not found" {:type :errors/not-found :product-id product-id})))
    product))

(defn- generate-db-adds
  [products category]
  (reduce (fn [db-adds product] (conj db-adds [:db/add
                                               [:product/id (:product/id product)]
                                               :product/category
                                               [:category/id (:category/id category)]])) [] products))

(s/defn all-categories :- [d.model/Category]
  [snapshot]
  (d.entity/datomic-to-entity (d/q '[:find [(pull ?e [*]) ...]
                            :where [?e :category/name]] snapshot)))


(s/defn upsert-products!
  ([conn products :- [d.model/Product]]
   (d/transact conn products))
  ([conn, products :- [d.model/Product], ip]
   (let [db-add-ip [:db/add "datomic.tx" :tx-data/ip ip]]
     (d/transact conn (conj products db-add-ip)))))

(s/defn add-categories! [conn categories :- [d.model/Category]]
  (d/transact conn categories))

(defn set-categories! [conn products category]
  (let [transact (generate-db-adds products category)]
    (d/transact conn transact)))

(defn all-products-with-categories
  [snapshot]
  (d/q '[:find ?name ?category-name
         :keys product category
         :where [?product :product/name ?name]
         [?product :product/category ?category]
         [?category :category/name ?category-name]] snapshot))


;backward navigation example
;with (_) to reference
(defn all-products-of-category [snapshot category-name]
  (d/q '[:find (pull ?category [:category/name {:product/_category [:product/name]}])
         :in $, ?name-category
         :where [?category :category/name ?name-category]] snapshot, category-name))

(defn product-resume
  [snapshot]
  (d/q '[:find (min ?price) (max ?price) (count ?price)
         :keys min, max, total
         :with ?e
         :where [?e :product/price ?price]] snapshot))


(defn product-resume-by-category
  [snapshot]
  (d/q '[:find ?name-category (min ?price) (max ?price) (count ?price) (sum ?price) (avg ?price)
         :keys category, min, max, total, sum, med
         :with ?e
         :where [?e :product/price ?price]
         [?e :product/category ?category]
         [?category :category/name ?name-category]] snapshot))

; 1 queries to get the product with max price
;we are using nested queries here
(defn all-products-expansive
  [snapshot]
  (d/q '[:find (pull ?product [*])
         :where [(q '[:find (max ?price)
                      :where [_ :product/price ?price]] $) [[?price]]]
         [?product :product/price ?price]] snapshot))

(defn all-products-cheap
  [snapshot]
  (d/q '[:find (pull ?product [*])
         :where [(q '[:find (min ?price)
                      :where [_ :product/price ?price]] $) [[?price]]]
         [?product :product/price ?price]] snapshot))

(defn all-products-by-ip
  [snapshot ip]
  (d/q '[:find (pull ?pr [:product/name, {:product/category [:category/name]}])
         :in $, ?ip
         :where [?tx :tx-data/ip ?ip]
         [?pr :product/id _ ?tx]] snapshot, ip))

(def rules '[[(inventory ?product ?inventory)
              [?product :product/inventory ?inventory]]
             [(inventory ?product ?inventory)
              [?product :product/digital true]
              [(ground 100) ?inventory]]
             [(can-sell? ?product)
              (inventory ?product ?inventory)
              [(> ?inventory 0)]]
             [(product-in-category ?product ?category-name)
              [?category :category/name ?category-name]
              [?product :product/category ?category]]])

;(s/defn all-products-with-inventory :- [d.model/Product]
;  [snapshot]
;  (d.entity/datomic-to-entity (d/q '[:find [(pull ?product [* {:product/category [*]}]) ...]
;                            :in $ %
;                            :where (inventory ?product ?inventory)
;                                   [(> ?inventory 0)]] snapshot rules)))
;
;(s/defn one-product-with-inventory :- (s/maybe d.model/Product)
;  [snapshot, product-id :- java.util.UUID]
;  (let [query '[:find (pull ?product [* {:product/category [*]}]) .
;                :in $ % ?product-id
;                :where [?product :product/id ?product-id]
;                       (inventory ?product ?inventory)
;                       [(> ?inventory 0)]]
;        result (d/q query snapshot rules product-id)
;        product (d.entity/datomic-to-entity result)]
;    (if (:product/id product)
;      product
;      nil)))

(s/defn all-products-salable :- [d.model/Product]
  [snapshot]
  (d.entity/datomic-to-entity (d/q '[:find [(pull ?product [* {:product/category [*]}]) ...]
                            :in $ %
                            :where (can-sell? ?product)] snapshot rules)))

(s/defn one-product-salable :- (s/maybe d.model/Product)
  [snapshot, product-id :- java.util.UUID]
  (let [query '[:find (pull ?product [* {:product/category [*]}]) .
                :in $ % ?product-id
                :where [?product :product/id ?product-id]
                (can-sell? ?product)]
        result (d/q query snapshot rules product-id)
        product (d.entity/datomic-to-entity result)]
    (if (:product/id product)
      product
      nil)))

(s/defn all-products-in-categories :- [d.model/Product] [db, categories :- [s/Str]]
  (d.entity/datomic-to-entity (d/q '[:find [(pull ?product [* {:product/category [*]}]) ...]
                            :in $ % [?category-name ...]
                            :where (product-in-category ?product ?category-name)] db rules categories)))

(s/defn all-products-in-categories-only-digital :- [d.model/Product] [db categories :- [s/Str] digital? :- s/Bool]
  (d.entity/datomic-to-entity (d/q '[:find [(pull ?product [* {:product/category [*]}]) ...]
                            :in $ % [?category-name ...] ?is-digital?
                            :where (product-in-category ?product ?category-name)
                            [?product :product/digital ?is-digital?]] db rules categories digital?)))

;bad function! This function may cause concurrency when changing product price
;(s/defn update-price
;  [conn product-id :- java.util.UUID old-price :- BigDecimal new-price :- BigDecimal]
;  (if (= old-price (:product/price (d/pull conn [*] (:product/id product-id))))
;    (d/transact conn [{:product/id product-id :product/price new-price}])
;    (throw (ex-info "Changed value between read and write" {:type :errors/value-already-change :price new-price}))))

(defn- tx-attributes [product-id old new attribute]
  [:db/cas [:product/id product-id] attribute (get old attribute) (get new attribute)])

(s/defn update-price
  [conn product-id :- java.util.UUID old-price :- BigDecimal new-price :- BigDecimal]
  (d/transact conn [[:db/cas [:product/id product-id] :product/price old-price new-price]]))

(s/defn update-product [conn old :- d.model/Product new :- d.model/Product]
  (let [product-id (:product/id old)
        attributes (c.set/intersection (set (keys old)) (set (keys new)))
        attributes (disj attributes :product/id)
        txs (map (partial tx-attributes product-id old new) attributes)]
    (d/transact conn txs)))

(s/defn add-variation! [conn product-id :- java.util.UUID variation :- s/Str price :- BigDecimal]
  (d/transact conn [{:db/id           "temp-variation"
                     :variation/id    (d.model/uuid)
                     :variation/name  variation
                     :variation/price price}
                    {:product/id         product-id
                     :product/variations "temp-variation"}]))

(defn total-of-products [db]
  (d/q '[:find [(count ?products)]
         ;:keys total
         :where [?products :product/name ?name]] db))

(s/defn remove-product [conn product-id :- java.util.UUID]
  (d/transact conn [[:db/retractEntity [:product/id product-id]]]))

;Danger! Hasn't atomicity
;(s/defn views-product [db product-id :- java.util.UUID]
;  (or (d/q '[:find ?v .
;             :in $ ?id
;             :where [?p :product/id ?id]
;             [?p :product/views ?v]] db product-id) 0))
;
;(s/defn visualization! [conn product-id :- java.util.UUID]
;  (let [views (views-product (d/db conn) product-id)
;        new-value (inc views)]
;    (d/transact conn [{:product/id product-id :product/views new-value}])))

(s/defn visualization! [conn product-id :- java.util.UUID]
  (d/transact conn [[:inc-views product-id]]))

;implicit pull
(s/defn all :- [d.model/Product]
  [snapshot]
  (d.entity/datomic-to-entity (d/q '[:find [(pull ?entity [* {:product/category [*]}]) ...]
                            :where [?entity :product/name]] snapshot)))
