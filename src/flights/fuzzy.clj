(ns flights.fuzzy
  (:require [clojure.string :as str]
            [clojure.set :as set]))

;; Functions taken from - https://github.com/Yomguithereal/clj-fuzzy

(defn n-grams
  "Lazily compute the n-grams of a sequence."
  [n s]
  (partition n 1 s))

(defn- letter-sets
  [n string]
  (set (n-grams n (-> (str/replace string #"\s+" "")
                      (str/upper-case)))))
;; Main functions
(defn coefficient
  "Compute the Dice coefficient between two [strings]."
  [string1 string2 & {:keys [n] :or {n 2}}]
  (cond (= string1 string2) 1.0
        (and (< (count string1) 2)
             (< (count string2) 2)) 0.0
        :else (let [p1 (letter-sets n string1)
                    p2 (letter-sets n string2)
                    sum (+ (count p1) (count p2))]
                (/ (* 2.0 (count (set/intersection p1 p2)))
                   sum))))
