(ns dunder-mifflin.lesson1
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
(pprint (d.db/all-products (d/db conn)))

(pprint (d.db/all-products-in-categories (d/db conn) ["Papers", "Cars"]))
(pprint (d.db/all-products-in-categories (d/db conn) ["Papers", "Printers"]))
(pprint (d.db/all-products-in-categories (d/db conn) []))
(pprint (d.db/all-products-in-categories (d/db conn) ["Cars"]))
(pprint (d.db/all-products-in-categories (d/db conn) ["Online"]))

(pprint (d.db/all-products-in-categories-only-digital (d/db conn) ["Online"] true))
(pprint (d.db/all-products-in-categories-only-digital (d/db conn) ["Online"] false))
(pprint (d.db/all-products-in-categories-only-digital (d/db conn) ["Papers", "Printers"] true))