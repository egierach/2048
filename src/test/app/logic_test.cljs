(ns app.logic-test
  (:require
   [cljs.test :as t]
   [app.enums :as enums]
   [app.logic :as subject]))

(t/deftest initial-board
  (t/testing "Initial board contains fourteen zeroes and two twos."
    (let [board (subject/initial-board)]
      (t/is (= 16
               (count board))
            "contains sixteen total elements")

      (t/is (= #{0 2}
               (set (map :value board)))
            "contains only zeroes and twos")

      (t/is (= 4
               (reduce + (map :value board)))
            "contains exactly two twos"))))

(t/deftest n-squares
  (t/testing "Returned elements have unique keys and the right value."
    (let [board (subject/n-squares 16 0)]
      (t/is (= 16
               (count (set (map :key board))))
            "contains sixteen unique keys")

      (t/is (= #{0}
               (into #{} (map :value board)))))))

(defn run-march
  "Makes running tests of subject/march easier to read and write."
  ([row]
   (run-march row (constantly nil)))
  ([row callback]
   (as-> row r
     (map #(subject/board-element %) r)
     (subject/march-left r callback)
     (mapv :value r))))

(t/deftest march-left
  (t/testing "a whole bunch of cases"
    (t/is (= (run-march [4 8 16 32])
             [4 8 16 32])
          "an incompressible row is left as-is")

    (t/is (= (run-march [0 4 0 8])
             [4 8 0 0])
          "leading and interstitial zeros are squished out")

    (t/is (= (run-march [2 2 4 8])
             [4 4 8 0])
          "merge in first intersection works")

    (t/is (= (run-march [2 4 4 2])
             [2 8 2 0])
          "merge in second intersection works")

    (t/is (= (run-march [2 4 8 8])
             [2 4 16 0])
          "merge in third intersection works")

    (t/is (= (run-march [2 2 2 2])
             [4 4 0 0])
          "parallel merges work")

    (t/is (= (run-march [0 2 2 2])
             [4 2 0 0])
          "merge offset by leading zero works")

    (t/is (= (run-march [2 0 0 2])
             [4 0 0 0])
          "merge separated by two zeroes works")

    (t/is (= (run-march [4 2 2 2])
             [4 4 2 0])
          "doesn't chain merges")))

(t/deftest march-right
  (t/testing "it marches to the right"
    (let [row [(subject/board-element 2)
               (subject/board-element 2)
               (subject/board-element 0)
               (subject/board-element 0)]]

      (t/is (= (mapv :value (subject/march-right row (constantly nil)))
               [0 0 0 4])
            "row is right justified and twos are merged"))))

(defn run-resolve-input
  ([board action]
   (run-resolve-input board action (constantly nil)))
  ([board action callback]
   (as-> board b
     (map #(subject/board-element %) b)
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
  (let [old-board (map #(subject/board-element %) old)
        new-board (map #(subject/board-element %) new)]
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
                      (map #(subject/board-element %))
                      (subject/win?)))
          "you won!")

    (t/is (false? (->> [0 2 0 0
                        0 0 0 0
                        0 0 0 0
                        0 0 0 0]
                       (map #(subject/board-element %))
                       (subject/win?)))
          "you lost.")))

(t/deftest game-over?
  (t/testing "returns true only with a full, non-reducible board."
    (t/is (true? (->> [2  4  8  16
                       4  8  16 32
                       8  16 32 64
                       16 32 64 128]
                      (map #(subject/board-element %))
                      (subject/game-over?)))
          "you lost.")

    (t/is (false? (->> [2  4  8  16
                        4  8  16 32
                        8  16 32 64
                        16 32 64 0]
                       (map #(subject/board-element %))
                       (subject/game-over?)))
          "so you're saying there's a chance....")))
