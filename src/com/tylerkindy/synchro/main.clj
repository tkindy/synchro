(ns com.tylerkindy.synchro.main
  [:require
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
   [ring.util.anti-forgery :refer [anti-forgery-field]]
   [hiccup.core :refer [html]]
   [compojure.core :refer [defroutes GET POST]]
   [compojure.route :refer [not-found]]])

(defn home []
  [:html
   [:body
    [:h1 "Home"]
    [:form {:method :post}
     [:label "Check it"
      [:input {:type :checkbox
               :name "is-checked"}]]
     (anti-forgery-field)
     [:button "Submit"]]]])

(defroutes app
  (GET "/" [] {:status 200
               :headers {"Content-Type" "text/html"}
               :body (html (home))})
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
