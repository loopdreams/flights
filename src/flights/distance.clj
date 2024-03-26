(ns flights.distance
  (:require [flights.carbon :as carbon]))

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
  (let [lat1 (:lat-decimal location1)
        lon1 (:lon-decimal location1)
        lat2 (:lat-decimal location2)
        lon2 (:lon-decimal location2)]
    (calculate-distance lat1 lon1 lat2 lon2)))


;; Average Air speed - 880–926 km/h
;; Average Air speed - 885-965 km/h
;; But, some distance is covered in take-off and landing and would need
;; to be factored in here ...
;; So, randomly reduced a little
(def air-speed [740 850])

(def take-off-and-landing-time 0.5)

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
                                      (reverse air-speed))}))
