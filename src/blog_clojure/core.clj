(ns blog-clojure.core
  (:require
   [markdown.core :as markdown]
   [hiccup.page :as hiccup.page]
   [hiccup2.core :as hiccup]
   [stasis.core :as stasis]
   [ring.adapter.jetty :as jetty]))

(def blog-name "Conao3 Notes")
(def stasis-config {:stasis/ignore-nil-pages? true})

(def pages
  {"/index.html" {:title "Home" :content "Welcome to my static site!"}
   "/about.html" {:title "About" :content "This is a simple static site!"}
   "/hey.html" nil})

(defn render-page [{:keys [title body]}]
  (hiccup.page/html5
   [:head
    [:meta {:http-equiv "Content-Type" :content "text/html; charset=utf-8"}]
    [:title (format "%s - %s" title blog-name)]
    (hiccup.page/include-css "/assets/modern-css-reset.css")
    (hiccup.page/include-css "/assets/spectrum/color-palette.css")
    (hiccup.page/include-css "/assets/spectrum/typography.css")
    (hiccup.page/include-css "/assets/index.css")]
   [:body
    [:h2 blog-name]
    [:div {:style {:display "flex"}}
     [:a {:href "/"} "Home"]]
    (hiccup/raw body)]))

(defn render-content [{:keys [title body]}]
  (hiccup/html
   [:article
    [:h1 title]
    [:hr]
    (hiccup/raw body)]))

(defn site []
  (stasis/merge-page-sources
   {:contents (->> (stasis/slurp-directory "generated/contents" #"\.md$")
                   (map (fn [[k v]] (let [p (markdown/md-to-html-string-with-meta v :heading-anchors true)
                                          obj {:title (first (:title (:metadata p)))
                                               :body (:html p)}]
                                      [(str (subs k 0 (- (count k) 3)) ".html")
                                       (-> obj
                                          (assoc :body (render-content obj))
                                          render-page)])))
                   (into {}))
    :public (stasis/slurp-directory "resources/public" #"\.[^.]+$")
    :spectrum (-> (stasis/slurp-directory "generated/spectrum" #"\.[^.]+$")
                  (update-keys (partial str "/assets/spectrum")))}))

(defn export [& _args]
  (let [export-dir "./target"
        load-export-dir #(stasis/slurp-directory export-dir #"\.[^.]+$")
        old-files (load-export-dir)]
    (stasis/empty-directory! export-dir)
    (println "Exporting...")
    (stasis/export-pages (site) "target/" stasis-config)
    (println "Export complete:")
    (stasis/report-differences old-files (load-export-dir))))

(defn start-server [& _args]
  (jetty/run-jetty
    (stasis/serve-pages site stasis-config)
    {:port 8080}))
