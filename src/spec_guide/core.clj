(ns spec-guide.core
  (:require [clojure.spec :as s]))

(s/conform even? 1000)
;; => 1000
(s/conform even? 1001)
;; => :clojure.spec/invalid


(s/valid? even? 10)
;; => true
(s/valid? even? 11)
;; => false

(import java.util.Date)

(s/valid? #(instance? Date %) (Date.))
;; => true

(s/conform #(instance? Date %) (Date.))
;; => #inst "2016-06-01T16:41:24.801-00:00"

(s/valid? #{:club :diamond :heart :spade} :club)
;; => true

(s/valid? #{:club :diamond :heart :spade} 42)
;; => false

(s/def ::date #(instance? Date %))
(s/def ::suit #{:club :diamond :heart :spade})

(s/def ::big-even (s/and integer? even? #(> % 1000)))
(s/valid? ::big-even :foo)
(s/valid? ::big-even 10)
(s/valid? ::big-even 10000)

(s/def ::name-or-id (s/or :name string?
                          :id integer?))
(s/valid? ::name-or-id "abc")
(s/valid? ::name-or-id 100)
(s/valid? ::name-or-id :foo)
(s/conform ::name-or-id "abc")

(s/valid? string? nil)
;; => false

(s/valid? (s/nilable string?) nil)
;; => true

(s/explain ::suit 42)

(s/explain ::name-or-id :foo)
(s/explain-str ::name-or-id :foo)
;; => "val: :foo fails at: [:name] predicate: string?\nval: :foo fails at: [:id] predicate: integer?\n"
(s/explain-data ::name-or-id :foo)
;; => {:clojure.spec/problems {[:name] {:pred string?, :val :foo, :via [], :in []}, [:id] {:pred integer?, :val :foo, :via [], :in []}}}


(s/def ::ingredient (s/cat :quantity number?
                           :unit keyword?))
(s/conform ::ingredient [2 :ingredient])
(s/explain ::ingredient [11 "peaches"])

(s/def ::seq-of-keywords (s/* keyword?))
(s/conform ::seq-of-keywords [:a :b :c])
(s/conform ::seq-of-keywords [10 20])
(s/explain-str ::seq-of-keywords [10 20])

(s/def ::odds-then-maybe-even (s/cat :odds (s/+ odd?)
                                     :even (s/? even?)))
(s/conform ::odds-then-maybe-even [1 3 5 100])
(s/conform ::odds-then-maybe-even [1])
(s/explain ::odds-then-maybe-even [100])

(defn boolean? [b] (instance? Boolean b))
(s/def ::opts (s/* (s/cat :opt keyword? :val boolean?)))
(s/conform ::opts [:silent? false :verbose? true])

(s/def ::config (s/*
                 (s/cat :prop string?
                        :val (s/alt :s string? :b boolean?))))

(s/conform ::config ["-server" "foo" "-verbose" true "-user" "joe"])

(s/describe ::config)

(s/def ::even-strings (s/& (s/* string?) #(even? (count %))))

(s/valid? ::even-strings ["a"])
(s/valid? ::even-strings ["a" "b"])

(s/def ::nested
  (s/cat :names-kw #{:names}
         :names (s/spec (s/* string?))
         :nums-kw #{:nums}
         :nums (s/spec (s/* number?))))
(s/conform ::nested [:names ["a" "b"] :nums [1 2 3]])

(s/def ::unnested
  (s/cat :names-kw #{:names}
         :names (s/* string?)
         :nums-kw #{:nums}
         :nums (s/* number?)))
(s/conform ::unnested [:names "a" "b" :nums 1 2 3])

(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")

(s/def ::email-type (s/and string? #(re-matches email-regex %)))
(s/def ::acctid integer?)
(s/def ::first-name string?)
(s/def ::last-name string?)
(s/def ::email ::email-type)

(s/def ::person (s/keys :req [::first-name ::last-name ::email]
                        :opt [::phone]))

(s/valid? ::person
          {::first-name "Elon"
           ::last-name "Muskie"
           ::email "elon@example.com"})

(s/explain ::person
           {::first-name "Elon"})

(s/def :unqualifed-key/person
  (s/keys :req-un [::first-name ::last-name ::email]
          :opt-un [::phone]))

(s/valid? :unqualifed-key/person
          {:first-name "Elon"
           :last-name "Muskie"
           :email "elon@example.com"})

(defrecord Person [first-name last-name email phone])

(s/explain :unqualifed-key/person
           (->Person "Elon" nil nil nil))

(s/conform (s/coll-of keyword? []) [:a :b :c])
(s/conform (s/coll-of number? #{}) #{5 10 2})

(s/def ::point (s/tuple float? float? float?)
  ;; (s/cat :x float? :y float? :z float?)
  )
(s/conform ::point [1.5 2.5 0.0])

(defn person-name
  [person]
  {:pre [(s/valid? ::person person)]
   :post [(s/valid? string? %)]}
  (str (::first-name person) " " (::last-name person)))
(person-name 42)
(person-name {::first-name "Elon"
              ::last-name "Muskie"
              ::email "elon@example.com"})

(defn- set-config [prop val]
  ;; dummy fn
  (println "set" prop val))

(defn configure [input]
  (let [parsed (s/conform ::config input)]
    (if (= parsed ::s/invalid)
      (throw (ex-info "Invalid input" (s/explain-data ::config input)))
      (map #(let [{prop :prop [_ val] :val} %]
              (set-config (subs prop 1) val)) parsed))))

(configure ["-server" "foo" "-verbose" true "-user" "joe"])

(defn ranged-rand
  "Returns random integer in range start <= rand < end"
  [start end]
  (+ start (rand-int (- start end))))

(s/fdef ranged-rand
        :args (s/and (s/cat :start integer? :end integer?)
                     #(< (:start %) (:end %)))
        :ret integer?
        :fn (s/and #(>= (:ret %) (-> % :args :start))
                   #(< (:ret %) (-> % :args :end))))

(s/instrument #'ranged-rand)

(ranged-rand 5 8)

(defn adder [x] #(+ x %))

((adder 5) 5)

(s/fdef adder
        :args (s/cat :x number?)
        :ret (s/fspec :args (s/cat :y number?)
                      :ret number?)
        :fn #(= (-> % :args :x) ((:ret %) 0)))
