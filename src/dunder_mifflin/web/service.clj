(ns dunder-mifflin.web.service
  (:require [io.pedestal.http :as http]))

(defn hello-pedestal
  [request]
  (let [name (get-in request [:params :name] "Test")]
    {:status 200 :body (str "Hi " name "!\n")}))

(def routes
  #{["/greet" :get `hello-pedestal]})

(def service {:env                 :local
              ::http/routes        routes
              ::http/resource-path "/public"
              ::http/type          :jetty
              ::http/port          8080})