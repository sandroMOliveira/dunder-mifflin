(ns dunder-mifflin.db.entity
  (:require [clojure.walk :as walk]))

(defn dissoc-db-id [entity]
  (if (map? entity)
    (dissoc entity :db/id)
    entity))

(defn datomic-to-entity [entities]
  (walk/prewalk dissoc-db-id entities))
