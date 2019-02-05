(ns app.views
  (:require
   [re-frame.core :as rf]
   [cljsjs.react-flip-move]
   [app.logic :as logic]))

(defn score []
  [:p.control
   [:span.tags.has-addons
    [:span.tag.is-large.score "Score"]
    [:span.tag.is-large.is-success @(rf/subscribe [:app.subs/score])]]])

(defn new-game-button [title]
  [:p.control
   [:button.button.is-danger.is-large
    {:on-click #(rf/dispatch [:app.events/game-started])}
    title]])

(defn control-panel []
  [:div.control-panel.is-clearfix
   [:div.game-title
    "2048"]
   (if @(rf/subscribe [:app.subs/is-playing?])
     [:div.game-controls
      [score]]
     [:div.game-controls
      [new-game-button "Play Game"]])])

(defn square [segment]
  [:div {:key (:key segment)
         :class (str "square square-" (:value segment))}
   (when (< 0 (:value segment))
     (:value segment))])

(defn instructions []
  (when (not @(rf/subscribe [:app.subs/is-playing?]))
    [:div.notification.instructions
     [:p.header "To Play:"]
     [:p "Click the \"Play Game\" button above."]
     [:p "Then, move the tiles with your arrow keys."]]))

(defn board []
  [:div.board-surround
   [:> js/FlipMove
    {:duration 100 :class "board board-flip"}
    (for [segment @(rf/subscribe [:app.subs/board])]
      [square segment])]
   [:div.board.board-bg
    (for [segment (logic/n-squares 16 "bg")]
      [square segment])]
   [instructions]])

(defn main-panel []
  [:div
   [control-panel]
   [board]])
