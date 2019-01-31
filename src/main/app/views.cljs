(ns app.views
  (:require
   [re-frame.core :as rf]
   [cljsjs.react-flip-move]))

(defn tile [segment]
  [:div.tile])

(defn board []
  [:div.board
   {:on-key-press #(rf/dispatch [:app.events/on-keypress])}
   [:> js/FlipMove
    {:duration 750 :easing "ease-out"}
    (for [segment @(rf/subscribe [:app.subs/board])]
      [tile segment])]])

(defn main-panel []
  [:section.section
   [board]])
