(ns app.game.logic
  "Functions that process player input, move the game forward,
  keep score, and test for win/loss conditions."
  (:require
   [app.game.enums :as enums]
   [app.game.board :as board]))


(defn resolve-input
  "Resolves the input action on the current board to produce the next.

  Assumes a 4x4 board.
  Example: (resolve-input [2 2 2 2        [4 4 0 0
                           0 2 2 2         4 2 0 0
                           0 0 2 2    =>   4 0 0 0
                           0 0 0 2]        2 0 0 0]
                          enums/left
                          callback)"
  [current-board action merge-callback]
  (let [rows (partition 4 current-board)]
    (cond
      (= action enums/left)
      (flatten (map #(board/march-left % merge-callback) rows))

      (= action enums/right)
      (flatten (map #(board/march-right % merge-callback) rows))

      (= action enums/up)
      (->> rows
           (board/transpose-matrix)
           (map #(board/march-left % merge-callback))
           (board/transpose-matrix)
           (flatten))

      (= action enums/down)
      (->> rows
           (board/transpose-matrix)
           (map #(board/march-right % merge-callback))
           (board/transpose-matrix)
           (flatten))

      :else
      current-board)))

(defn upgrade-random-zero
  "Returns a copy of new-board with a random zero-valued element's value increased
  to either 2 or 4.

  No upgrade will take place if previous-board and new-board are equal, or if
  there are no zero-valued elements left in new-board.

  The replacement values for the chosen zero element will be 2's roughly 80%
  of the time."
  [previous-board new-board]
  (let [values-index (map-indexed vector (map :value new-board))
        zeroes (filter #(zero? (second %)) values-index)
        replacement-value (rand-nth [2 2 2 2 4])]
    (if (or (= (map :value previous-board)
               (map :value new-board))
            (empty? zeroes))
      new-board
      (let [random-index (first (rand-nth zeroes))]
        (as-> random-index z
          (nth new-board z)
          (update z :value + replacement-value)
          (assoc (vec new-board) random-index z))))))

(defn evaluate-move
  "Return a new version of the board that has been altered according
  to the player input and the game's rules."
  [current-board key-pressed score-callback]
  (as-> current-board b
    (resolve-input b key-pressed score-callback)
    (upgrade-random-zero current-board b)))

(defn win?
  "Returns true if the board has any squares with value 2048, and false otherwise."
  [board]
  (some? (seq (filter #(= 2048 (:value %)) board))))

(defn game-over?
  "Returns true if there are no more possible moves, false otherwise."
  [board]
  (let [up-board (resolve-input board enums/up (constantly nil))
        down-board (resolve-input board enums/down (constantly nil))
        left-board (resolve-input board enums/left (constantly nil))
        right-board (resolve-input board enums/right (constantly nil))]
    (= up-board down-board left-board right-board)))
