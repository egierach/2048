(ns app.effects
  (:require
   [re-frame.core :as rf]))

(defn keydown-listener
  "Handles the raw javascript event to keep re-frame pure."
  [e]
  (rf/dispatch [:app.events/on-keydown (.-key e)])
  (.preventDefault e))

(defn capture-the-keyboard!
  "Adds an event listener to the root document for keydown events."
  []
  (.addEventListener js/document
                     "keydown"
                     keydown-listener))

(defn release-the-keyboard!
  "Removes any event listener on keydown from the root document."
  []
  (.removeEventListener js/document
                        "keydown"
                        keydown-listener))


(def endgame-timer (atom nil))

(defn start-endgame-monitor!
  "Starts a process that monitors if endgame conditions have been met.
  The monitor checks every second."
  []
  (let [every-millis 1000
        timer-id (js/setInterval
                  #(rf/dispatch [:app.events/endgame-monitor-tick])
                  every-millis)]
    (reset! endgame-timer timer-id)))

(defn stop-endgame-monitor!
  "Stops the process that monitors for endgame conditions."
  []
  (when-let [timer-id @endgame-timer]
    (js/clearInterval timer-id)
    (reset! endgame-timer nil)))

(rf/reg-fx
 ::capture-keyboard
 capture-the-keyboard!)

(rf/reg-fx
 ::release-keyboard
 release-the-keyboard!)

(rf/reg-fx
 ::start-endgame-monitor
 start-endgame-monitor!)

(rf/reg-fx
 ::stop-endgame-monitor
 stop-endgame-monitor!)
