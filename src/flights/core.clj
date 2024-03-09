(ns flights.core
  (:require [flights.distance :as distance]
            [flights.db :as db]
            [flights.message :as message]
            [clojure.string :as str]))


(defn make-message [{:keys [distance] :as data}]
  (let [loc-msg      (message/oirgin-and-destination-msg data)
        distance-msg (str "Approximate Distance: " (int distance) " km")
        time-msg     (message/flight-time-msg data)
        carbon-msg   (message/carbon-msg data)]
    (str "------------------------------------------------\n"
         (str/join "\n\n"
                   [loc-msg
                    distance-msg
                    time-msg
                    carbon-msg])
         "\n------------------------------------------------")))




(defn cities-data [query]
  (db/query-cities query))

(defn airports-data [query]
  (db/query-airports query))

(defn flight-data [queries]
  (let [[q1 q2] queries
        [loc1 loc2] (map db/query-find queries)]
    (if (and loc1 loc2)
      (distance/generate-flight-data loc1 loc2)
      (println (str "Not Found: " (if loc1 q2 q1))))))

(defn flight-message [queries]
  (make-message (flight-data queries)))





(comment
  (flight-message "john f kennedy" "dublin"))
