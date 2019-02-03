(ns app.db
  (:require [app.logic :as logic]))

(def default-db
  {:board (logic/initial-board)
   :score 0
   :last-key nil})
