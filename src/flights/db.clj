(ns flights.db
  (:require [babashka.pods :as pods]
            [babashka.deps :as deps]
            [flights.fuzzy :as fz]
            [clojure.string :as str]))

(pods/load-pod 'org.babashka/go-sqlite3 "0.1.0")
(deps/add-deps '{:deps {honeysql/honeysql {:mvn/version "1.0.444"}}})

(require '[pod.babashka.go-sqlite3 :as sqlite]
         '[honeysql.core :as sql]
         '[honeysql.helpers :as helpers])

;; TODO Make configurable
(def db "db/global_airports_sqlite.db")

(defn make-query-space [db]
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

(comment
  (take 5 (make-query-space db)))

(defn suggest-match [query candidates]
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


;; TODO Expand search for airports/countries
;; TODO handle cases where multiple entries are returned
;; TODO Ignore entries where there is '0, 0' for lat long

(defn get-data-by-query [db query]
  (let [lookup              (make-query-space db)
        match               (suggest-match query (keys lookup))
        {:keys [name city country]} (lookup match)
        q                   (sql/format
                             {:select [:name :city :country :lat_decimal :lon_decimal]
                              :from   [:airports]
                              :where  [:and
                                       [:= :name name]
                                       [:= :city city]
                                       [:= :country country]]})]
    (-> (sqlite/query db q)
        first)))

(def query-find (partial get-data-by-query db))


;; TODO lookup airports by city name
;; TODO lookup cities by country name

(comment
  (get-data-by-query db "new york"))

