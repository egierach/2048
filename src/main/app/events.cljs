(ns app.events
  (:require
   [re-frame.core :as rf]
   [app.db :as db]
   [app.enums :as enums]
   [app.logic :as logic]))

(defn init-keyboard-event
  "Adds an event listener for key presses to the root document."
  []
  (.addEventListener js/document
                     "keydown"
                     (fn [e]
                       (rf/dispatch [::on-keydown (.-key e)])
                       (.preventDefault e))))

(defn dispatch-score [s]
  (rf/dispatch [::score s]))

(defn next-board [board key-pressed]
  (-> board
      (logic/next-board key-pressed dispatch-score)
      (logic/upgrade-random-zero)))

(defn handle-keypress
  "Updates the game state based on an arrow key being pressed."
  [db [_ key-pressed]]
  (if (enums/arrows key-pressed)
    (assoc db
           :board (next-board (:board db) key-pressed)
           :last-key key-pressed)
    db))

(defn handle-score
  "Updates the game state based on scoring event occurring."
  [db [_ score]]
  (update db :score + score))

(rf/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(rf/reg-event-db
 ::on-keydown
 handle-keypress)

(rf/reg-event-db
 ::score
 handle-score)
