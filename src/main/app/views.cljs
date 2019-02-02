(ns app.views
  (:require
   [re-frame.core :as rf]
   [cljsjs.react-flip-move]
   [app.logic :as logic]))

(defn square [segment]
  [:div {:key (:key segment)
         :class (str "square square-bg square-" (:value segment))}
   (when (< 0 (:value segment))
     (:value segment))])

(defn board []
  [:div.board-surround
   [:> js/FlipMove
    {:duration 75 :easing "ease-out" :class "board board-flip"}
    (for [segment @(rf/subscribe [:app.subs/board])]
      [square segment])]
   [:div.board.board-bg
    (for [segment (logic/n-zeroes 16)]
      [square segment])]])

(defn main-panel []
  [:section.section
   [board]])
