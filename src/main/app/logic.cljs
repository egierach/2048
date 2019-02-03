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

  The element at index1 is overwritten with a double-valued version
  of the element at index2, and index2's spot is back-filled with a
  zero element.

  Example: (merge-dups [2 2 0 0] 0 1) => [4 0 0 0]"
  [row index1 index2]
  (let [value1 (:value (nth row index1))
        item2 (nth row index2)
        value2 (:value item2)]

    (if (and (< 0 (+ value1 value2))
             (= value1 value2))
      (assoc row
             index1 (update item2 :value * 2)
             index2 (board-element 0))
      row)))

(defn march-left
  "Returns a copy of row with non-zero elements marched toward
  the left and duplicates merged.
  
  Compound merges are not allowed, but parallel merges are.
  Examples:
  (march [2 2 4 2]) => [4 4 2 0] ;; not [8 2 0 0]
  (march [2 2 2 2]) => [4 4 0 0] ;; not [8 0 0 0]"
  [row]
  (-> row
      (left-justify)
      (merge-dups 0 1)
      (merge-dups 1 2)
      (merge-dups 2 3)
      (left-justify)))

(defn march-right
  "Returns a copy of row marched to the right (reverse of march-left)."
  [row]
  (-> row
      (reverse)
      (march-left)
      (reverse)))

(defn transpose-matrix [matrix]
  (apply mapv vector matrix))

(defn next-board
  "Resolves the input action on the current board to produce the next.

    0 0 2 2        0 0 2 2
    2 0 2 0        2 0 2 0
    2 0 4 0        2 0 4 0
    0 0 0 0        0 0 0 0

  + rightArrow   + downArrow
  -------------  ------------
    0 0 0 4        0 0 0 0
    0 0 0 4        0 0 0 0
    0 0 2 4        0 0 4 0
    0 0 0 0        4 0 4 2
  "
  [current-board action]
  (let [rows (partition 4 current-board)]
    (cond
      (= action enums/left)
      (flatten (map march-left rows))

      (= action enums/right)
      (flatten (map march-right rows))

      (= action enums/up)
      (->> rows
           (transpose-matrix)
           (map march-left)
           (transpose-matrix)
           (flatten))

      (= action enums/down)
      (->> rows
           (transpose-matrix)
           (map march-right)
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
