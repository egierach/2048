(ns app.logic-test
  (:require
   [cljs.test :as t]
   [app.logic :as subject]))

(t/deftest initial-board
  (t/testing "Initial board contains fourteen zeroes and two twos."
    (let [board (subject/initial-board)]
      (t/is (= 16
               (count board))
            "contains sixteen total elements")

      (t/is (= #{0 2}
               (set board))
            "contains only zeroes and twos")

      (t/is (= 4
               (reduce + board))
            "contains exactly two twos"))))
