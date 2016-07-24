(ns spec-guide.recipe
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]))

(s/def ::name string?)
(s/def ::description string?)
(s/def ::ingredients (s/* string?))
(s/def ::steps (s/* string?))
(s/def ::servings integer?)
(s/def ::first-name string?)
(s/def ::last-name string?)
(s/def ::author (s/keys :req-un [::first-name
                                 ::last-name]))
(s/def ::recipe (s/keys :req-un [::name
                                 ::author
                                 ::description
                                 ::ingredients
                                 ::steps
                                 ::servings]))
(gen/generate (s/gen ::recipe))

(defrecord Recipe
    [name
     author
     description
     ingredients
     steps
     servings])

(defrecord Author
    [first-name
     last-name])

(def pbj
  (->Recipe
   "Peanut Butter & Jelly"
   (->Author "Cliff" "String")
   "Delicious"
   ["Bread" "Peanuts" "Butter" "Jelly"]
   ["Apply peanut butter and jelly to the bread"]
   1))

(s/explain ::recipe pbj)

(s/conform ::recipe pbj)
;; => #spec_guide.recipe.Recipe{:name "Peanut Butter & Jelly", :author #spec_guide.recipe.Author{:first-name "Cliff", :last-name "String"}, :description "Delicious", :ingredients ["Bread" "Peanuts" "Butter" "Jelly"], :steps ["Apply peanut butter and jelly to the bread"], :servings 1}
