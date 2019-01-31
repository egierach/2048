(ns app.subs
  (:require
   [re-frame.core :as rf]))

(rf/reg-sub
 ::board
 (fn [db _]
   (:board db)))
