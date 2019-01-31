(ns app.core
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [app.events :as events]
   [app.subs :as subs]
   [app.views :as views]
   [app.config :as config]))

(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (events/init-keyboard-event)
  (dev-setup)
  (mount-root))
