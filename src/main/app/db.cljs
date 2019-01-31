(ns app.db
  (:require [app.logic :as logic]))

(def default-db
  {:board (logic/initial-board)
   :last-key nil})
