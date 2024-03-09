(ns flights.main
  (:require [flights.distance :as distance]
            [flights.db :as db]
            [flights.message :as message]
            [clojure.string :as str]
            [clojure.pprint :as pprint]))


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

(defn -main [query1 query2]
  (let [[loc1 loc2] (map db/query-find [query1 query2])]
    
    (if (and loc1 loc2)

      (let [{:keys [origin destination carbon distance flight-time]}
            (distance/calculate-flight-time loc1 loc2)]
        (do
          (println
           (make-message (distance/generate-flight-data loc1 loc2)))
          (clojure.pprint/pprint (distance/generate-flight-data loc1 loc2))))
      (println (str "Not Found: " (if loc1 query2 query2))))))

(comment
  (-main "john f kennedy" "dublin"))
