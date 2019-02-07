(ns app.events-test
  (:require
   [cljs.test :as t]
   [app.db :as db]
   [app.game.enums :as enums]
   [app.game.board :as board]
   [app.game.logic :as logic]
   [app.events :as subject]))

(defn run-keypress [values key]
  (let [board (map #(board/board-element %) values)
        old-db {:board board}
        new-db (subject/handle-keypress old-db [nil key])]
    (map :value (:board new-db))))

(t/deftest handle-keypress
  (let [board-values [0 0 0 0
                      0 2 0 0
                      0 0 0 0
                      0 0 0 0]]

    (t/testing "it updates board when an arrow key is pressed"
      (t/is (not= board-values
                  (run-keypress board-values enums/up))
            "board values are modified when up arrow is pressed")

      (t/is (= 16
               (count (run-keypress board-values enums/up)))
            "board still valid in terms of element count")

      (t/is (< 2 (reduce + (run-keypress board-values enums/up)) 8)
            "total board value has increased, another indicator of validity"))

    (t/testing "it leaves the state intact when a non-arrow is pressed."
      (t/is (= board-values
               (run-keypress board-values "a"))
            "non-arrow keys leave board as-is"))))

(t/deftest handle-endgame-monitor-tick
  (t/testing "win condition causes end-game"
    (with-redefs [logic/win? (constantly true)
                  logic/game-over? (constantly false)]
      (let [old-db (db/start-game {})
            result (subject/handle-endgame-monitor-tick {:db old-db} nil)]
        (t/is (some? (:app.effects/stop-endgame-monitor result))
              "interval for monitoring the game is stopped")

        (t/is (some? (:app.effects/release-keyboard result))
              "keyboard intercept is stopped")

        (t/is (false? (get-in result [:db :playing]))
              "game is no longer in the playing state"))))

  (t/testing "game over condition causes end-game"
    (with-redefs [logic/win? (constantly false)
                  logic/game-over? (constantly true)]
      (let [old-db (db/start-game {})
            result (subject/handle-endgame-monitor-tick {:db old-db} nil)]
        (t/is (some? (:app.effects/stop-endgame-monitor result))
              "interval for monitoring the game is stopped")

        (t/is (some? (:app.effects/release-keyboard result))
              "keyboard intercept is stopped")

        (t/is (false? (get-in result [:db :playing]))
              "game is no longer in the playing state"))))

  (t/testing "lack of end-game condition does not change the game state."
    (with-redefs [logic/win? (constantly false)
                  logic/game-over? (constantly false)]
      (let [old-db (db/start-game {})
            result (subject/handle-endgame-monitor-tick {:db old-db} nil)]
        (t/is (= old-db (:db result))
              "game state does not change")))))
