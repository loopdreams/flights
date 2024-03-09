(ns flights.carbon)


;; Avg 12 liters per KM
(def avg-fuel-usage-per-km 12)

(def avg-fuel-usage-takeoff-landing 1100)

(def avg-no-passengers 300)

;; Source - https://www.statista.com/statistics/986460/co2-emissions-per-cap-eu/#:~:text=Greenhouse%20gas%20emissions%20per%20capita%20in%20the%20European%20Union%201990%2D2021&text=Per%20capita%20greenhouse%20gas%20(GHG,tons%20of%20carbon%20dioxide%20equivalent.
(def avg-person-annual-emissions 7.77e3)

;; Source - https://www.umweltbundesamt.de/daten/klima/treibhausgas-emissionen-in-der-europaeischen-union#hauptverursacher
(def recommended-annual-avg 0.6e3)

(defn- total-co2-emitted [distance]
  (*
   (+ (* distance avg-fuel-usage-per-km)
      avg-fuel-usage-takeoff-landing)
   3.1))

(defn- personal-co2-contribution [total-c02]
  (/ total-c02 avg-no-passengers))


(defn personal-co2-emissions [distance]
  (-> distance
      total-co2-emitted
      personal-co2-contribution))

