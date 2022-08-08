(ns com.tylerkindy.synchro.plans
  [:require
   [hiccup.core :refer [html]]
   [hiccup.util :refer [escape-html]]
   [com.tylerkindy.synchro.data :refer [plans]]
   [clojure.string :as str]])

(defn create-plan [{:keys [description creator-name] :as params}]
  (let [id (random-uuid)
        dates (->> params
                   (filter (fn [[k v]] (and (str/starts-with? (name k) "date")
                                            (not (str/blank? v)))))
                   (map (fn [[_ v]] (java.time.LocalDate/parse v)))
                   sort)]
    (swap! plans assoc id {:description description
                           :creator-name creator-name
                           :dates dates})
    {:status 303
     :headers {"Location" (str "/plans/" id)}}))

(def date-formatter (java.time.format.DateTimeFormatter/ofPattern "E, LLL d, u"))

(defn found-plan-page [{:keys [description creator-name dates]}]
  (let [date-headers (map (fn [date] [:th (.format date date-formatter)])
                          dates)
        date-cells (map (fn [date] [:td "yes"])
                        dates)]
    [:html
     [:head
      [:title (str (escape-html description) " | Synchro")]]
     [:body
      [:h1 description]
      [:table
       [:thead
        (->
         (concat
          [:tr
           [:th "Name"]]
          date-headers)
         vec)]
       [:tbody
        (->
         (concat
          [:tr
           [:td (escape-html creator-name)]]
          date-cells)
         vec)]]]]))

(defn plan-page [id]
  (let [plan (@plans id)
        response (if plan
                   {:status 200
                    :body (html (found-plan-page plan))}
                   {:status 404
                    :body (html [:html [:body [:p "Unknown plan"]]])})]
    (assoc response :headers {"Content-Type" "text/html"})))
