(ns dunder-mifflin.web.server
  (:gen-class)
  (:require [io.pedestal.http :as http]
            [dunder-mifflin.web.service :as d.service]))

(defonce runnable-service (http/create-server d.service/service))

(defn run-dev
  "The entry-point for 'lein run-dev'"
  [& args]
  (println "\nCreating your [DEV] server...")
  (-> d.service/service ;; start with production configuration
      (merge {:env :dev
              ;; do not block thread that starts web server
              ::http/join? false
              ;; Routes can be a function that resolve routes,
              ;;  we can use this to set the routes to be reloadable
              ::http/routes #(deref #'d.service/routes)
              ;; all origins are allowed in dev mode
              ::http/allowed-origins {:creds true :allowed-origins (constantly true)}})
      ;; Wire up interceptor chains
      http/default-interceptors
      http/dev-interceptors
      http/create-server
      http/start))

;(defn -main
;  "The entry-point for 'lein run'"
;  [& args]
;  (println "\nCreating your server...")
;  (http/start runnable-service))
