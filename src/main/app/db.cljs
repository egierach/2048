(ns app.db
  (:require [app.game.board :as board]))

(def default-db
  {:board (board/n-board-elements 16 0)
   :score 0
   :playing false
   :game-over false
   :victory false})

(defn start-game [db]
  (assoc db
         :board (board/initial-board)
         :score 0
         :playing true
         :game-over false
         :victory false))

(defn end-game [db win?]
  (assoc db
         :playing false
         :game-over true
         :victory win?))
