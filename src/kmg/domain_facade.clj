(ns kmg.domain-facade
  (:require
    [datomic.api :as d]
    [taoensso.timbre.profiling :as p])
  (:use carica.core
        clojure.data
        kmg.domain
        kmg.datomic-helpers))

(defn users []
  (->> (d/q '[:find ?username
         :where
         [_ :user/name ?username]]
       (db))
      every-first))

(defn recommendations [user]
  (with-syncronized-db-do
    (fn [] (let [db (db)
                 recommend-ids (take 4 (recommendation-ids db user))]
    (map #(recommendation-data db %) recommend-ids)))))

(defn recommendations-completed [user]
  (with-syncronized-db-do
    (fn [] (let [db (db)
                 recommend-ids (take 10 (recommendations-completed-by-user db user))]
    (map #(recommendation-data db %) recommend-ids)))))

(defn mark-as-completed [user recommendation]
  (let [db (db)
        feedback (create-feedback user recommendation)]
    (p/p :transact/feedback @(d/transact (conn) feedback))))

;; (mark-as-completed "user1" "spec1_book4")

(defn children-specializations
  "This function supposed to be used from presentation layer"
  [spec]
  (let [db (db)
        children-spec-ids (children-specialization-ids db spec)]
    (map #(entity db %) children-spec-ids)))
;;(children-specializations "spec1")