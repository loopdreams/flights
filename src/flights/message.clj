(ns flights.message
  (:require [clojure.string :as str]
            [flights.carbon :as carbon]))

;; Formatting for the main printed outputs

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
  (let [[lwr upr] (map hours->hr-min flight-time)]
    (str "Estimated Flight Time: Between " lwr
         " and "
         upr)))

(defn oirgin-and-destination-msg [{:keys [origin destination]}]
  (str/join "\n"
            [(str "Origin: " (airport-location-str origin))
             (str "Destination: " (airport-location-str destination))]))

(defn city-names-msg [{:keys [country cities]}]
  (str "The following cities have airports in " country
       ":\n- "
       (str/join "\n- " cities)))

(defn airport-names-msg [{:keys [city airports]}]
  (str "The following airports are in " city
       ":\n- "
       (str/join "\n- " airports)))


(def bar-symbol (char 9632))

(defn bar-string [percentage]
  (let [len (* 50 (/ percentage 100))]
    (str "[" (str/join (repeat len bar-symbol)) "]")))

(def co2-annual-avg-bar (str (bar-string 100)
                             " EU Person Annual Avg: "
                             (int carbon/avg-person-annual-emissions)
                             " kg"))

(defn personal-co2-usage-bar [co2-personal co2-percentage]
  (str
   (bar-string co2-percentage)
   " Carbon cost: " co2-personal " kg / "
   co2-percentage "% of annual EU avg"))


(defn recommended-co2-bar []
  (str
   (bar-string (* 100 (/ carbon/recommended-annual-avg carbon/avg-person-annual-emissions)))
   " Recommended annual avg: " carbon/recommended-annual-avg
   " kg"))

(defn carbon-msg [{:keys [co2-personal co2-percentage-annual-avg]}]
  (str/join "\n"
            [(personal-co2-usage-bar co2-personal co2-percentage-annual-avg)
             (recommended-co2-bar)
             co2-annual-avg-bar]))
