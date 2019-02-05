(ns app.events
  (:require
   [re-frame.core :as rf]
   [app.db :as db]
   [app.enums :as enums]
   [app.logic :as logic]))

(defn handle-game-start
  "Initializes the game state for a new game."
  [cofx _]
  {:db (db/start-game (:db cofx))
   :app.effects/capture-keyboard {}
   :app.effects/start-endgame-monitor {}})

(defn handle-keypress
  "Updates the game state based on an arrow key being pressed."
  [db [_ key-pressed]]
  (if (enums/arrows key-pressed)
    (assoc db
           :board
           (logic/evaluate-move (:board db)
                                key-pressed
                                #(rf/dispatch [::points-added %])))
    db))

(defn handle-points-added
  "Updates the score when the player causes tiles to be merged."
  [db [_ score]]
  (update db :score + score))

(defn handle-endgame-monitor-tick
  "Checks for endgame conditions and ends the game if met."
  [cofx _]
  (let [{:keys [board playing]} (:db cofx)
        win? (logic/win? board)
        game-over? (logic/game-over? board)]

    (if (and playing (or win? game-over?))
      {:db (db/end-game (:db cofx) win?)
       :app.effects/stop-endgame-monitor {}
       :app.effects/release-keyboard {}}
      {:db (:db cofx)})))

(defn handle-game-over-acknowledged
  "Closes 'game over' modal when player dismisses it."
  [db _]
  (assoc db :game-over false))

(rf/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(rf/reg-event-fx
 ::game-started
 handle-game-start)

(rf/reg-event-db
 ::on-keydown
 handle-keypress)

(rf/reg-event-db
 ::points-added
 handle-points-added)

(rf/reg-event-fx
 ::endgame-monitor-tick
 handle-endgame-monitor-tick)

(rf/reg-event-db
 ::game-over-acknowledged
 handle-game-over-acknowledged)
