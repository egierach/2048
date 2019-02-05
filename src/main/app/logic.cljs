(ns app.logic
  (:require
   [app.enums :as enums]))

(def key-counter (atom 0))

(defn next-key []
  (swap! key-counter inc))

(defn board-element
  "Returns a map with an key and a value"
  [value]
  {:key (next-key) :value value})

(defn n-squares
  "Returns a vector of n board elements with value v."
  [n v]
  (vec (repeatedly n #(board-element v))))

(defn initial-board
  "Returns a 16 element vector of 0 elements with two randomly-placed 2's.
  Meant to be the initial board state of the game."
  []
  (let [board-positions (shuffle (range 0 16))
        [a b] (take 2 board-positions)]
    (-> (n-squares 16 0)
        vec
        (assoc a (board-element 2))
        (assoc b (board-element 2)))))

(defn left-justify
  "Returns a copy of row stripped of zero-valued elements and right-padded
  with zero-valued elements.

  Example: (left-justify [0 4 0 8]) => [4 8 0 0]"
  [row]
  (let [without-zeroes (vec (remove #(zero? (:value %)) row))]
    (vec (into without-zeroes
               (n-squares (- (count row) (count without-zeroes)) 0)))))

(defn merge-dups
  "Returns a copy of row with elements at index1 and index2 merged
  if they have equal values.

  If elements are merged, the merge-callback is called with the
  value of the merged cell.  Meant to aid in score-keeping.

  The element at index1 is overwritten with a double-valued version
  of the element at index2, and index2's spot is back-filled with a
  zero element.

  Example: (merge-dups [2 2 0 0] 0 1) => [4 0 0 0]"
  [row index1 index2 merge-callback]
  (let [item1 (nth row index1)
        item2 (nth row index2)]
    (if (and (< 0 (:value item1))
             (= (:value item1) (:value item2)))
      (do
        (merge-callback (* 2 (:value item2)))
        (assoc row
               index1 (update item2 :value * 2)
               index2 (board-element 0)))
      row)))

(defn all-neighbors
  "Returns a list of index pairs for all neighboring elements in row.

  Example: (all-neighbors [:a :b :c :d]) => ([0 1] [1 2] [2 3])"
  [row]
  (let [indices (range (count row))]
    (map vector indices (drop 1 indices))))

(defn march-left
  "Returns a copy of row with non-zero elements marched toward
  the left and duplicates merged.

  Compound merges are not allowed, but parallel merges are.
  Examples:
  (march-left [2 2 4 2]) => [4 4 2 0] ;; not [8 2 0 0]
  (march-left [2 2 2 2]) => [4 4 0 0] ;; not [8 0 0 0]"
  [row merge-callback]
  (let [row-as-vector (vec row)
        justified (left-justify row-as-vector)
        merged (reduce (fn [r [i1 i2]]
                         (merge-dups r i1 i2 merge-callback))
                       justified
                       (all-neighbors row))]
    (left-justify merged)))

(defn march-right
  "Returns a copy of row marched to the right (reverse of march-left)."
  [row merge-callback]
  (-> row
      (reverse)
      (march-left merge-callback)
      (reverse)))

(defn transpose-matrix
  "Takes a collection of collections representing a matrix as a series of rows
  and returns a series of vectors representing the matrix as columns.

  Example: (transpose-matrix [[1 2 3]     [[1 4 7]
                              [4 5 6]  =>  [2 5 8]
                              [7 8 9]])    [3 6 9]]"
  [matrix]
  (apply mapv vector matrix))

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
      (flatten (map #(march-left % merge-callback) rows))

      (= action enums/right)
      (flatten (map #(march-right % merge-callback) rows))

      (= action enums/up)
      (->> rows
           (transpose-matrix)
           (map #(march-left % merge-callback))
           (transpose-matrix)
           (flatten))

      (= action enums/down)
      (->> rows
           (transpose-matrix)
           (map #(march-right % merge-callback))
           (transpose-matrix)
           (flatten))

      :else
      current-board)))

(defn upgrade-random-zero
  "Returns a copy of board with a random zero-valued element's value increased
  to either 2 or 4.

  The replacement values for the chosen zero element will be 2's roughly 80%
  of the time."
  [board]
  (let [values-index (map-indexed vector (map :value board))
        zeroes (filter #(zero? (second %)) values-index)
        replacement-value (rand-nth [2 2 2 2 4])]
    (if (empty? zeroes)
      board
      (let [random-index (first (rand-nth zeroes))]
        (as-> random-index z
          (nth board z)
          (update z :value + replacement-value)
          (assoc (vec board) random-index z))))))

(defn evaluate-move
  "Return a new version of the board that has been altered according
  to the player input and the game's rules."
  [current-board key-pressed score-callback]
  (-> current-board
      (resolve-input key-pressed score-callback)
      (upgrade-random-zero)))

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
