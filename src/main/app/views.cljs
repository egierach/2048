(ns app.views
  (:require
   [re-frame.core :as rf]
   [cljsjs.react-flip-move]
   [app.game.board :as board]))

(defn score []
  [:p.score "Score: "@(rf/subscribe [:app.subs/score])])

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
     [:p "Then, move the tiles with your arrow keys."]
     [:br]
     [:p "The object of the game is to make a single tile with the value '2048' in it."]]))

(defn board []
  [:div.board-surround
   [:> js/FlipMove
    {:duration 100 :class "board board-flip"}
    (for [segment @(rf/subscribe [:app.subs/board])]
      [square segment])]
   [:div.board.board-bg
    (for [segment (board/n-board-elements 16 "bg")]
      [square segment])]
   [instructions]])

(defn game-over-overlay []
  (let [winner @(rf/subscribe [:app.subs/did-player-win?])]
    [:div
     {:class (str "modal"
                  (if @(rf/subscribe [:app.subs/is-game-over?])
                    " is-active"
                    ""))}
     [:div.modal-background]
     [:div.modal-content.game-over-modal
      [:div.box
       [:p.header (if winner "You won!" "Game Over")]
       [:p (str "Your Score: " @(rf/subscribe [:app.subs/score]))]
       [:p (if winner
             "Studies show that winning is good for you.  Cheers to your health!"
             "You'll get it eventually.  Keep trying!")]
       [:br]
       [:p
        [:button.button.is-success
         {:on-click #(rf/dispatch [:app.events/game-over-acknowledged])}
         "Word"]]]]
     [:button.modal-close.is-large
      {:on-click #(rf/dispatch [:app.events/game-over-acknowledged])}]]))

(defn main-panel []
  [:div
   [control-panel]
   [board]
   [game-over-overlay]])
