(ns com.tylerkindy.synchro.main
  [:require
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [ring.util.anti-forgery :refer [anti-forgery-field]]
   [hiccup.core :refer [html]]
   [garden.core :refer [css]]
   [compojure.core :refer [defroutes GET POST]]
   [compojure.route :refer [not-found]]])

(defn home []
  [:html
   [:head
    [:link {:rel :stylesheet :href "/main.css"}]]
   [:body
    [:h1 "Synchro"]
    [:h2 "Make plans with friends"]
    [:form {:class "new-plan-form" :method :post}
     [:label {:for "description"} "Description"]
     [:input {:id "description" :name "description"}]

     [:label {:for "creator-name"} "Your name"]
     [:input {:id "creator-name" :name "creator-name"}]

     (anti-forgery-field)

     [:button "Submit"]]]])

(def main-css
  (css [:body {:margin "auto"
               :max-width "375px"}]
       [:h1 :h2 {:text-align :center}]
       [".new-plan-form" {:display :grid
                          :grid-template-rows "25px 25px 25px"
                          :grid-template-columns "auto auto"
                          :gap "10px"
                          :align-items :baseline}
        [:label {:text-align :end}]
        [:button {:grid-column "1 / span 2"}]]))

(defn create-plan [{:keys [description creator-name]}]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (html [:html [:body [:p (str description "," creator-name)]]])})

(defroutes app
  (GET "/" [] {:status 200
               :headers {"Content-Type" "text/html"}
               :body (html (home))})
  (GET "/main.css" [] {:status 200
                       :headers {"Content-Type" "text/css"}
                       :body main-css})
  (POST "/" req (create-plan (:params req)))
  (not-found nil))

(defonce server (atom nil))

(defn restart!
  ([] (restart! false))
  ([join?]
   (when @server (.stop @server))
   (reset! server (run-jetty (wrap-defaults app site-defaults)
                             {:port 3000
                              :join? join?}))))

(defn -main []
  (restart! true))
