(ns user
  (:require
   [blog-clojure.core :as blog-clojure]))

(defonce server (atom nil))

(defn go []
  (->> (blog-clojure/start-server)
       future
       (reset! server)))


