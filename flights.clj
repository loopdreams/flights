#!/usr/bin/env bb

(require '[flights.core :as core])
(require '[flights.message :as message])
(require '[babashka.cli :as cli])
(require '[cheshire.core :as json])

(comment
  (apply core/flight-message *command-line-args*))

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
(def help
  (str "To use, provide an <origin> and a <destination>.\n\n"
       "For example: flights 'new york' 'paris'\n\n"
       "Alternatively, search for airport and city names: \n"
       (cli/format-opts {:spec spec})
       "\n\nExample: flights -a 'New York' (returns list of new york airports)\n"
       "Example: flights -d json 'new york' 'paris' (returns flight info in json format)\n"
       "Example: flights -d edn -c 'germany' (returns cities in Germany in edn format)"))

;; (defn re-order-args [opts])

(defn handle-data-format [fmt data]
  (case fmt
    :edn data
    :json (json/generate-string data)
    (println "Not a valid data format, please use either :json or :edn")))


(defn handle-opts [opts args]
  (cond
    (:cities opts)   (let [cities (core/cities-data (:cities opts))
                           data   (:data opts)]
                       (if data
                         (println (handle-data-format data cities))
                         (println (message/city-names-msg cities))))
    (:airports opts) (let [airports (core/airports-data (:airports opts))
                           data     (:data opts)]
                       (if data
                         (println (handle-data-format data airports))
                         (println (message/airport-names-msg airports))))
    (:data opts)     (let [fmt (:data opts)
                           data (core/flight-data (:args args))]
                       (println (handle-data-format fmt data)))
    :else            nil))




(defn -main [input]
  (if (empty? input) (println help)
      (let [opts (cli/parse-opts input {:spec spec})
            args (cli/parse-args input)]
        (cond
          (seq opts)                    (handle-opts opts args)
          (not= (count (:args args)) 2) (println (str "Please provide an origin and a destination.\n"))
          :else                         (println (core/flight-message (:args args)))))))

(-main *command-line-args*)
