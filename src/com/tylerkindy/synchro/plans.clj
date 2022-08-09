(ns com.tylerkindy.synchro.plans
  [:require
   [hiccup.core :refer [html]]
   [hiccup.util :refer [escape-html]]
   [com.tylerkindy.synchro.data :refer [plans]]
   [clojure.string :as str]])

(def unknown-plan-page
  {:status 404
   :headers {"Content-Type" "text/html"}
   :body (html [:html [:body [:p "Unknown plan"]]])})

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

(defn found-plan-page [{:keys [description dates people]}]
  (let [date-headers (map (fn [date] [:th (.format date date-formatter)])
                          dates)
        people-rows (map (fn [[name available-dates]]
                           (-> [:tr
                                [:td (escape-html name)]]
                               (concat (map (fn [date] (if (available-dates date)
                                                         [:td "yes"]
                                                         [:td "no"]))
                                            dates))
                               vec))
                         people)]
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
       (-> [:tbody]
           (concat people-rows)
           vec)]]]))

(defn plan-page [id]
  (let [plan (@plans id)]
    (if plan
      {:status 200
       :headers {"Content-Type" "text/html"}
       :body (html (found-plan-page plan))}
      unknown-plan-page)))

(defn add-person [{:keys [game-id name] :as params}]
  (if (@plans game-id)
    (let [dates (->> params
                     (filter (fn [[k]] (str/starts-with? (name k) "date-")))
                     (map (fn [[k]] (str/replace-first (name k) "date-" "")))
                     (map (fn [date] (java.time.LocalDate/parse date)))
                     set)
          new-plan (swap! plans assoc-in [:people name] dates)]
      (found-plan-page new-plan))
    unknown-plan-page))
