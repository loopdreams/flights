#!/usr/bin/env bb

(require '[flights.main :as main])

(do
  (apply main/-main *command-line-args*))
