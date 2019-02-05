(ns app.subs
  (:require
   [re-frame.core :as rf]))

(rf/reg-sub
 ::board
 (fn [db _]
   (:board db)))

(rf/reg-sub
 ::score
 (fn [db _]
   (:score db)))

(rf/reg-sub
 ::is-playing?
 (fn [db _]
   (:playing db)))
