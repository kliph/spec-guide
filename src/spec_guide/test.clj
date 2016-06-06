(ns spec-guide.test
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [spec-guide.cards :as cards]))

(gen/generate (s/gen integer?))

(gen/sample (s/gen string?))

(gen/sample (s/gen (s/cat :k keyword? :ns (s/+ number?))))

;;; NOTE: This is how you refer to `s/def`s from other namespaces
(gen/generate (s/gen ::cards/player))

(gen/generate (s/gen ::cards/game))

(s/def ::ident (s/and vector? (s/cat :ident keyword? :value #(not (coll? %)))))

(gen/generate (s/gen ::ident))

(s/exercise (s/cat :k keyword? :ns (s/+ number?)))

(s/def ::kws (s/and keyword? #(= (namespace %) "my.domain")))
(s/valid? ::kws :my.domain/name)
(gen/sample (s/gen ::kws))

(def kw-gen (s/gen #{:my.domain/name :my.domain/occupation :my.domain/id}))
(gen/sample kw-gen 5)

(def kw-gen-2 (gen/fmap #(keyword "my.domain" %) (gen/string-alphanumeric)))
(gen/sample kw-gen-2 5)

(def kw-gen-3 (gen/fmap #(keyword "my.domain" %)
                        (gen/such-that #(not= % "")
                                       (gen/string-alphanumeric))))
(gen/sample kw-gen-3 5)

(defn boolean? [x]
  (instance? Boolean x))
(s/def ::boolean (s/with-gen boolean? #(gen/boolean)))

(gen/sample (s/gen ::boolean))

(defn uuid? [x]
  (instance? java.util.UUID x))
(s/def ::uuid (s/with-gen uuid? #(gen/uuid)))

(gen/sample (s/gen ::uuid))

(s/def ::roll (s/with-gen (s/and integer? #(<= 0 % 10))
                #(gen/choose 0 10)))
(gen/sample (s/gen ::roll))
