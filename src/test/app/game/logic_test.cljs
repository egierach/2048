(ns app.game.logic-test
  (:require
   [cljs.test :as t]
   [app.enums :as enums]
   [app.game.board :as board]
   [app.game.logic :as subject]))


(defn run-resolve-input
  ([board action]
   (run-resolve-input board action (constantly nil)))
  ([board action callback]
   (as-> board b
     (map #(board/board-element %) b)
     (subject/resolve-input b action callback)
     (map :value b))))

(t/deftest resolve-input
  (let [board [0 0 2 8
               0 2 2 4
               8 0 0 0
               4 2 4 4]]
    (t/testing "all directions work"
      (t/is (= (run-resolve-input board enums/up)
               [8 4 4 8
                4 0 4 8
                0 0 0 0
                0 0 0 0])
            "looks marched upward")

      (t/is (= (run-resolve-input board enums/down)
               [0 0 0 0
                0 0 0 0
                8 0 4 8
                4 4 4 8])
            "looks marched downward")

      (t/is (= (run-resolve-input board enums/left)
               [2 8 0 0
                4 4 0 0
                8 0 0 0
                4 2 8 0])
            "looks marched leftward")

      (t/is (= (run-resolve-input board enums/right)
               [0 0 2 8
                0 0 4 4
                0 0 0 8
                0 4 2 8])
            "looks marched rightward")))

  (t/testing "scoring works"
    (let [score (atom 0)
          score-callback (fn [s] (swap! score + s))
          board1 [0 0 0 2
                  4 0 0 2
                  0 0 0 0
                  4 0 0 0]
          _ (run-resolve-input board1 enums/up score-callback)
          score1 @score

          board2 [4 4 4 4
                  2 2 2 2
                  0 0 0 0
                  0 0 0 0]
          _ (run-resolve-input board2 enums/right score-callback)
          score2 @score]

      (t/is (= 12 score1)
            "score is 12 after first move")

      (t/is (= (+ 24 score1) score2)
            "score is 24 after second move"))))

(defn run-upgrade-random-zero [old new]
  (let [old-board (map #(board/board-element %) old)
        new-board (map #(board/board-element %) new)]
    (->> (subject/upgrade-random-zero old-board new-board)
         (map :value))))

(t/deftest upgrade-random-zero
  (t/testing "one fewer zero with same number of elements"
    (let [old [0 4 8 0]
          new [0 4 8 2]
          result (run-upgrade-random-zero old new)]

      (t/is (= (count new) (count result))
            "same number of elements")

      (t/is (> (reduce + result)
               (reduce + new))
            "result is greater than input")

      (t/is (= (+ 1 (count (filter zero? result)))
               (count (filter zero? new)))
            "one fewer zero in result")))

  (t/testing "does nothing with full board"
    (let [old [2 4 8 0]
          new [2 4 8 4]
          result (run-upgrade-random-zero old new)]

      (t/is (= new result)
            "input and result are the same")))

  (t/testing "does nothing when old and new boards are the same"
    (let [old [2 4 8 0]
          new [2 4 8 0]
          result (run-upgrade-random-zero old new)]

      (t/is (= new result)
            "input and result are the same"))))

(t/deftest win?
  (t/testing "returns true only when a 2048 square exists"
    (t/is (true? (->> [0 2048 0 0
                       0 0 0 0
                       0 0 0 0
                       0 0 0 0]
                      (map #(board/board-element %))
                      (subject/win?)))
          "you won!")

    (t/is (false? (->> [0 2 0 0
                        0 0 0 0
                        0 0 0 0
                        0 0 0 0]
                       (map #(board/board-element %))
                       (subject/win?)))
          "you lost.")))

(t/deftest game-over?
  (t/testing "returns true only with a full, non-reducible board."
    (t/is (true? (->> [2  4  8  16
                       4  8  16 32
                       8  16 32 64
                       16 32 64 128]
                      (map #(board/board-element %))
                      (subject/game-over?)))
          "you lost.")

    (t/is (false? (->> [2  4  8  16
                        4  8  16 32
                        8  16 32 64
                        16 32 64 0]
                       (map #(board/board-element %))
                       (subject/game-over?)))
          "so you're saying there's a chance....")))
