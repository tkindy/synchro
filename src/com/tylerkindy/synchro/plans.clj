(ns com.tylerkindy.synchro.plans
  [:require
   [hiccup.core :refer [html]]
   [hiccup.util :refer [escape-html]]
   [com.tylerkindy.synchro.data :refer [plans]]
   [com.tylerkindy.synchro.css :refer [plan-css]]
   [clojure.string :as str]
   [ring.util.anti-forgery :refer [anti-forgery-field]]])

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
                           :dates dates
                           :people {}})
    {:status 303
     :headers {"Location" (str "/plans/" id)}}))

(def date-formatter (java.time.format.DateTimeFormatter/ofPattern "E, LLL d, u"))

(defn found-plan-page [{:keys [description dates people]}]
  (let [date-headers (map (fn [date] [:th (.format date date-formatter)])
                          dates)
        people-rows (map (fn [[name available-dates]]
                           (-> [:tr
                                [:td (escape-html name)]]
                               (concat (mapv (fn [date]
                                               [:td.date-checkbox-cell
                                                [:input
                                                 {:type :checkbox
                                                  :disabled ""
                                                  :checked (and (available-dates date) "")}]])
                                             dates))
                               vec))
                         people)]
    [:html
     [:head
      [:title (str (escape-html description) " | Synchro")]
      [:style plan-css]]
     [:body
      [:h1 description]
      [:form {:method :post}
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
            vec
            (conj (-> [:tr
                       [:td [:input {:type :text :name :person-name}]]]
                      (concat
                       (map (fn [date]
                              [:td.date-checkbox-cell
                               [:input {:type :checkbox :name (str "date-" date)}]])
                            dates))
                      vec
                      (conj [:td [:button "Submit"]]))))]
       (anti-forgery-field)]]]))

(defn found-plan-response [plan]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (html (found-plan-page plan))})

(defn plan-page [id]
  (let [plan (@plans id)]
    (if plan
      (found-plan-response plan)
      unknown-plan-page)))

(defn add-person [{:keys [game-id person-name] :as params}]
  (let [game-id (java.util.UUID/fromString game-id)]
    (if (@plans game-id)
      (let [dates (->> params
                       (filter (fn [[k]] (str/starts-with? (name k) "date-")))
                       (map (fn [[k]] (str/replace-first (name k) "date-" "")))
                       (map (fn [date] (java.time.LocalDate/parse date)))
                       set)]
        (swap! plans assoc-in [game-id :people person-name] dates)
        {:status 303
         :headers {"Location" (str "/plans/" game-id)}})
      unknown-plan-page)))
