(ns flights.core
  (:require [flights.distance :as distance]
            [flights.db :as db]
            [flights.message :as message]
            [clojure.string :as str]
            [cheshire.core :as json]
            [babashka.cli :as cli]))

(defn flight-data-message [{:keys [distance] :as data}]
  (let [loc-msg      (message/oirgin-and-destination-msg data)
        distance-msg (str "Approximate Distance: " (int distance) " km")
        time-msg     (message/flight-time-msg data)
        carbon-msg   (message/carbon-msg data)]
    (str "-------------------------------------------------------------------\n"
         (str/join "\n\n"
                   [loc-msg
                    distance-msg
                    time-msg
                    carbon-msg])
         "\n-------------------------------------------------------------------")))

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
  (flight-data-message (flight-data queries)))

(def spec {:airports {:ref   "<city>"
                      :desc  "Lookup airports based on <city> query"
                      :alias :a}
           :cities   {:ref   "<country>"
                      :desc  "Lookup cities that have airports based on <country> query"
                      :alias :c}
           :data     {:ref "<format>"
                      :desc  "Optional flag to return any of the above as data <format>, either edn or json"
                      :coerce :keyword
                      :alias :d}
           :help     {:desc "Print this help message"
                      :alias :h}})
;; CLI Handling
(def help
  (str "To use, provide an <origin> and a <destination>.\n\n"
       "For example: flights 'new york' 'paris'\n\n"
       "Alternatively, search for airport and city names: \n"
       (cli/format-opts {:spec spec})
       "\n\nExample: flights -a 'New York' (returns list of new york airports)\n"
       "Example: flights -d json 'new york' 'paris' (returns flight info in json format)\n"
       "Example: flights -d edn -c 'germany' (returns cities in Germany in edn format)"))

(defn handle-data-format [fmt data]
  (case fmt
    :edn data
    :json (json/generate-string data)
    (println "Not a valid data format, please use either :json or :edn")))

(defn handle-opts [opts args]
  (cond
    (:cities opts)   (let [cities (cities-data (:cities opts))
                           data   (:data opts)]
                       (if data
                         (println (handle-data-format data cities))
                         (println (message/city-names-msg cities))))
    (:airports opts) (let [airports (airports-data (:airports opts))
                           data     (:data opts)]
                       (if data
                         (println (handle-data-format data airports))
                         (println (message/airport-names-msg airports))))
    (:data opts)     (let [fmt (:data opts)
                           data (flight-data (:args args))]
                       (println (handle-data-format fmt data)))
    :else            "Nothing happened..."))

(defn -main [input]
  (if (empty? input) (println help)
      (let [opts (cli/parse-opts input {:spec spec})
            args (cli/parse-args input)]
        (cond
          (seq opts)                    (handle-opts opts args)
          (not= (count (:args args)) 2) (println (str "Please provide an origin and a destination.\n"))
          :else                         (println (flight-message (:args args)))))))

;; Init function
(-main *command-line-args*)
