(ns spec-guide.core
  (:require [clojure.spec :as s]))

(s/conform even? 1000)
