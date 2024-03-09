(ns flights.distance
  (:require [flights.db :as db]
            [flights.carbon :as carbon]
            [clojure.string :as str]))


;; Calculating Distance between lat and lon

;; Earth's redius - 6,371km
(def R 6371e3)

(def rad-v (/ Math/PI 180))
;; Using this as a guide - https://www.movable-type.co.uk/scripts/latlong.html
(defn calculate-distance [lat1 lon1 lat2 lon2]
  (let [l1 (* lat1 rad-v)
        l2 (* lat2 rad-v)
        dlat (* (- lat2 lat1) rad-v)
        dlon (* (- lon2 lon1) rad-v)
        a (+ (Math/pow (Math/sin (/ dlat 2)) 2)
             (* (Math/cos l1)
                (Math/cos l2)
                (Math/pow (Math/sin (/ dlon 2)) 2)))
        c (* 2 (Math/atan2 (Math/sqrt a) (Math/sqrt (- 1 a))))]
    (* R c)))

(defn distance [location1 location2]
  (let [lat1 (:lat_decimal location1)
        lon1 (:lon_decimal location1)
        lat2 (:lat_decimal location2)
        lon2 (:lon_decimal location2)]
    (calculate-distance lat1 lon1 lat2 lon2)))

(comment
  (float
   (/
    (distance db/test-dublin db/test-paris)
    1000))

  (float
   (/
    (distance db/test-paris db/test-dublin)
    1000)))

;; Creating output strings:

(defn hours->hr-min [hours]
  (let [h (int hours)
        mins  (int (* (mod hours 1) 60))]
    (str h (if (= 1 h) " hour, " " hours, ")
         mins (if (= 1 mins) " minute" " minutes"))))

(defn flight-time-str [estimates]
  (let [[upr lwr] (map hours->hr-min estimates)]
    (str "Estimated Flight Time: Between " lwr
         " and "
         upr)))

(defn airport-location-str [{:keys [name city country]}]
  (str (str/capitalize name)
       " Airport in "
       (str/capitalize city)
       ", "
       (str/capitalize country)))


;; Average Air speed - 880–926 km/h

(def air-speed [740 900])

(def take-off-and-landing-time 0.5)

;; TODO refactor - take out string functions. Also include json return
(defn calculate-flight-time [location1 location2]
  (let [distance (-> (distance location1 location2)
                     (/ 1000)
                     float)
        co2-em (carbon/personal-co2-emissions distance)
        diff-recommended (- co2-em carbon/recommended-annual-avg)]
    {:origin      (airport-location-str location1)
     :destination (airport-location-str location2)
     :carbon      (str "Approximately " (int co2-em)
                       " kg of CO2 emitted, which is around "
                       (int (* 100 (/ co2-em carbon/avg-person-annual-emissions)))
                       "% of a typical EU person's average annual emissions and "
                       (int diff-recommended)
                       (if (pos? diff-recommended) "kg above " "kg below ")
                       "the necessary average annual emissions to stop climate change.")
                       
     :distance    (str "Approximate Distance: " (int distance) " km")
     :flight-time
     (flight-time-str
      (map #(+ (/ distance %) take-off-and-landing-time)
           air-speed))}))

(defn generate-flight-data [loc1 loc2]
  (let [distance         (-> (distance loc1 loc2)
                             (/ 1000)
                             float)
        co2-em           (carbon/personal-co2-emissions distance)
        diff-recommended (- co2-em carbon/recommended-annual-avg)]
    {:origin                     loc1
     :destination                loc2
     :co2-personal               (int co2-em)
     :co2-percentage-annual-avg  (int (* 100 (/ co2-em carbon/avg-person-annual-emissions)))
     :co2-difference-recommended diff-recommended
     :distance                   distance
     :flight-time                (map #(+ (/ distance %) take-off-and-landing-time)
                                      air-speed)}))
