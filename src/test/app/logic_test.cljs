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

(t/deftest n-zeroes
  (t/testing "Returned elements have unique keys."
    (let [board (subject/n-zeroes 16)]
      (t/is (= 16
               (count (set (map :key board))))
            "contains sixteen unique keys"))))

(defn run-march
  "Makes running tests of subject/march easier to read and write."
  [row]
  (->> row
       (map #(subject/board-element %))
       (subject/march-left)
       (mapv :value)))

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
          "merge separated by two zeroes works")))

(t/deftest march-right
  (t/testing "it marches to the right"
    (let [row [(subject/board-element 2)
               (subject/board-element 2)
               (subject/board-element 0)
               (subject/board-element 0)]]

      (t/is (= (mapv :value (subject/march-right row))
               [0 0 0 4])
            "row is right justified and twos are merged"))))

(defn run-next-board [board action]
  (as-> board b
    (map #(subject/board-element %) b)
    (subject/next-board b action)
    (map :value b)))

(t/deftest next-board
  (t/testing "up works"
    (t/is (= (run-next-board [0 0 2 8
                              0 2 2 4
                              8 0 0 0
                              4 2 4 4]
                             enums/up)
             [8 4 4 8
              4 0 4 8
              0 0 0 0
              0 0 0 0])
          "looks marched upward")))
