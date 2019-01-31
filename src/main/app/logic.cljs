(ns app.logic)

(def key-counter (atom 0))

(defn next-key []
  (swap! key-counter inc))

(defn board-element
  "Returns a map with an key and a value"
  [key value]
  {:key key :value value})

(defn board-zero
  "Generates a 16 element list of 0's."
  []
  (repeatedly 16 #(board-element (next-key) 0)))

(defn initial-board
  "Generates a 16 element list of 0's with two randomly-placed 2's.
  Meant to be the initial board state of the game."
  []
  (let [board-positions (shuffle (range 0 16))
        [a b] (take 2 board-positions)]
    (-> (board-zero)
        vec
        (assoc a (board-element (next-key) 2))
        (assoc b (board-element (next-key) 2)))))

(defn next-board
  "Resolves the input action on the current board to produce the next."
  [current-board action]
  (initial-board))
