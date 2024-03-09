(ns flights.message
  (:require [clojure.string :as str]))


(defn- hours->hr-min [hours]
  (let [h (int hours)
        mins  (int (* (mod hours 1) 60))]
    (str h (if (= 1 h) " hour, " " hours, ")
         mins (if (= 1 mins) " minute" " minutes"))))


(defn- airport-location-str [{:keys [name city country]}]
  (str (str/capitalize name)
       " Airport in "
       (str/capitalize city)
       ", "
       (str/capitalize country)))

(defn flight-time-msg [{:keys [flight-time]}]
  (let [[upr lwr] (map hours->hr-min flight-time)]
    (str "Estimated Flight Time: Between " lwr
         " and "
         upr)))

(defn oirgin-and-destination-msg [{:keys [origin destination]}]
  (str/join "\n"
            [(str "Origin: " (airport-location-str origin))
             (str "Destination: " (airport-location-str destination))]))

(defn carbon-msg [{:keys [co2-personal co2-percentage-annual-avg
                          co2-difference-recommended]}]
  (str "Approximately " (int co2-personal)
       " kg of CO2 emitted, which is around "
       co2-percentage-annual-avg
       "% of a EU person's annual average emissions and "
       (int co2-difference-recommended)
       (if (pos? co2-difference-recommended) "kg above " "kg below ")
       "the necessary average annual emissions recommended to stop climate change."))
