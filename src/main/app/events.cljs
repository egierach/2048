(ns app.events
  (:require
   [re-frame.core :as rf]
   [app.db :as db]
   [app.enums :as enums]
   [app.logic :as logic]))

(rf/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(rf/reg-event-db
 ::on-keypress
 (fn [db event]
   (let [key-pressed (.-which event)]
     (if (enums/arrows key-pressed)
       (assoc db
              :board (logic/next-board (:board db) key-pressed)
              :last-key key-pressed)
       db))))
