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
    [:form {:method :post}
     [:div
      [:label {:for "description"} "Description"]
      [:input {:id "description" :name "description"}]]
     [:div
      [:label {:for "creator-name"} "Your name"]
      [:input {:id "creator-name" :name "creator-name"}]]
     (anti-forgery-field)
     [:button "Submit"]]]])

(def main-css
  (css [:h1 {:color :red}]))

(defroutes app
  (GET "/" [] {:status 200
               :headers {"Content-Type" "text/html"}
               :body (html (home))})
  (GET "/main.css" [] {:status 200
                       :headers {"Content-Type" "text/css"}
                       :body main-css})
  (POST "/" [] {:status 200
                :headers {"Content-Type" "text/html"}
                :body (html [:html [:body [:p "Submitted"]]])})
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
