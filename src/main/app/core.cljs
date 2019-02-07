(ns app.core
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as rf]
   [app.config :as config]
   [app.effects]
   [app.events]
   [app.subs]
   [app.views :as views]))

(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (rf/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (rf/dispatch-sync [:app.events/initialize-db])
  (dev-setup)
  (mount-root))
