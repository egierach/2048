(ns app.db
  (:require [app.logic :as logic]))

(def default-db
  {:board (logic/n-squares 16 0)
   :score 0
   :playing false
   :game-over false
   :victory false})

(defn start-game [db]
  (assoc db
         :board (logic/initial-board)
         :score 0
         :playing true
         :game-over false
         :victory false))

(defn end-game [db win?]
  (assoc db
         :playing false
         :game-over true
         :victory win?))
