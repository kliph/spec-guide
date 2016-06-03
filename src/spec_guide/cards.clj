(ns spec-guide.cards
  (:require [clojure.spec :as s]))


(def suit? #{:club :diamond :heart :spade})
(def rank? (into  #{:club :diamond :heart :spade} (range 2 11)))
(def deck (for [suit suit? rank rank?] [rank suit]))

(s/def ::card (s/tuple rank? suit?))
(s/def ::hand (s/* ::card))

(s/def ::name string?)
(s/def ::score integer?)
(s/def ::player (s/keys :req [::name ::score ::hand]))

(s/def ::players (s/* ::player))
(s/def ::deck (s/* ::card))
(s/def ::game (s/keys :req [::players ::deck]))

(s/valid? ::player {::name "Kenny Rogers"
                    ::score 100
                    ::hand []})

(s/explain ::game
           {::deck deck
            ::players [{::name "Kenny Rogers"
                        ::score 100
                        ::hand [[2 :banana]]}]})
