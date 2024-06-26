(ns flights.db
  (:require [pod.babashka.go-sqlite3 :as sqlite]
            [honeysql.core :as sql]
            [flights.fuzzy :as fz]
            [clojure.string :as str]))

(def db "db/global_airports_sqlite.db")
           
(defn- make-query-space [db]
  (reduce (fn [q-space entry]
            (let [{:keys [name city country]} entry]
              (when name
                (assoc q-space
                       (str/join " " [name city country]) ;; presuming each query is unique
                       {:name name
                        :city city
                        :country country}))))

          {}
          (sqlite/query db ["Select name, city, country from airports"])))

(defn- suggest-match [query candidates]
  (let [[best-match cooef]
        (->>
         (reduce (fn [results candidate]
                   (assoc results candidate
                          (fz/coefficient query candidate)))
                 {}
                 candidates)
         (sort-by val)
         (reverse)
         first)]
    (when (pos? cooef)
      best-match)))

(defn snake->kebab
  "Very loose function, used becase of known snake-case values in source data."
  [key]
  (let [name (name key)]
    (->
     (str/replace name "_" "-")
     keyword)))

(defn transform-keys-to-kebab-case
  "For the sake of consistency in api outputs."
  [m]
  (let [ks (map snake->kebab (keys m))]
    (zipmap ks (vals m))))


(defn get-data-by-query [db query]
  (let [lookup              (make-query-space db)
        match               (->> (keys lookup)
                                 (remove #(re-find #"N/A" %)) ;; Filtering out :name with "N/A", since these appear to have 0,0 as lat/lng
                                 (suggest-match query))
        {:keys [name city country]} (lookup match)
        q                   (sql/format
                             {:select [:name :city :country :lat_decimal :lon_decimal]
                              :from   [:airports]
                              :where  [:and
                                       [:= :name name]
                                       [:= :city city]
                                       [:= :country country]]})]
    (-> (sqlite/query db q)
        first
        transform-keys-to-kebab-case)))

(defn airports-by-city-name [db city-query]
  (let [candidates (map :city (sqlite/query db "Select city from airports"))
        city       (suggest-match city-query candidates)
        query      (sql/format
                    {:select [:name]
                     :from   [:airports]
                     :where  [:and [:= :city city] [:not= :name "N/A"]]})]
    {:city city
     :airports
     (->> (sqlite/query db query)
          (mapv (comp str/capitalize :name)))}))

(defn cities-by-country-name [db country-query]
  (let [candidates (map :country (sqlite/query db "Select country from airports"))
        country    (suggest-match country-query candidates)
        query      (sql/format
                    {:select [:city]
                     :from   [:airports]
                     :where  [:and [:= :country country] [:not= :name "N/A"]]})]
    {:country country
     :cities
     (->> (sqlite/query db query)
          (mapv (comp str/capitalize :city)))}))


(def query-find (partial get-data-by-query db))

(def query-airports (partial airports-by-city-name db))

(def query-cities (partial cities-by-country-name db))
