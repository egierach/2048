(ns app.logic)

(defn initial-board
  "Generates a 16 element list of 0's with two randomly-placed 2's.
  Meant to be the initial board state of the game."
  []
  (let [board-positions (shuffle (range 0 16))
        [a b] (take 2 board-positions)]
    (-> (repeat 16 0)
        vec
        (assoc a 2)
        (assoc b 2))))

(defn next-board
  "Resolves the input action on the current board to produce the next."
  [current-board action]
  (initial-board))
