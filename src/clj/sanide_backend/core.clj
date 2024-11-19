(ns sanide-backend.core
  (:require [org.httpkit.server :as hk-server]
            [taoensso.timbre :as log]
            [sanide-backend.routes :as routes]
            [ring.middleware.reload :as reload]))


(defn reloading-ring-handler
  "Reload ring handler on each request."
  [f]
  (let [reload! (#'reload/reloader ["src"] true)]
    (fn
      ([request]
       (reload!)
       ((f) request))
      ([request respond raise]
       (reload!)
       ((f) request respond raise)))))


(defn run-server
  [{:keys [dev-mode? server-options]}]
  (let [create-handler-fn #(routes/app)
        handler* (if dev-mode?
                   (reloading-ring-handler create-handler-fn)
                   (create-handler-fn))]
    (hk-server/run-server handler* server-options)))

(defn -main [& _]
  (run-server {:dev-mode? true :server-options {:port 9000}})
  (log/info "Started server on http://localhost:9000")
  (log/info "Swagger UI available on http://localhost:9000/api-docs/"))