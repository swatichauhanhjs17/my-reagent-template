(ns my-test-reagent.core
  (:require
    [reagent.core :as reagent :refer [atom]]
    [reagent.session :as session]
    [reitit.frontend :as reitit]
    [clerk.core :as clerk]
    [accountant.core :as accountant]))

;; -------------------------
;; Routes

(def router
  (reitit/router
    [["/" :index]
     ["/my-new-route" :my-new-route]
     ["/items"
      ["" :items]
      ["/:item-id" :item]]
     ["/about" :about]]))

(defn path-for [route & [params]]
  (if params
    (:path (reitit/match-by-name router route params))
    (:path (reitit/match-by-name router route))))

(path-for :about)
(path-for :my-new-route)
;; -------------------------
;; Page components

(defn home-page []
  (fn []
    [:span.main
     [:h1 "This is  my-test-reagent"]
     [:ul
      [:li [:a {:href (path-for :items)} "Items of my-test-reagent"]]
      [:li [:a {:href "/broken/link"} "Broken link"]]]]))

(def my-checkbox-values (reagent/atom {:red false :orange false :blue false}))
(def my-color-atom (reagent/atom "green"))
(def prev-color (reagent/atom []))
(def old-color (reagent/atom nil))

(defn show-color
  [col]
  (swap! prev-color conj col))

(defn show-all-values
  [prev-color]
  [:div
   [:ol
    "ALL COLOURS CHANGED:-"
    (for [item prev-color]
      ^{:key (str item)} [:li "Colour of the button is changed to :- "  item]
      )]])

(defn color-input [value]
  [:input {:type "text"
           :value @value
           :on-change #(reset! value (-> % .-target .-value))}])

(defn change-color []
  (fn []
    [:div
     [:p "Change the colour here: " [color-input my-color-atom]
      [:p "This is your new colour: " @my-color-atom]
      ]]))

(defn my-current-color [color]
  [:div
   " Current Color of button changed to :- " color]
  )

(defn my-button []
  (fn []
    [:div
     [:span {:style {:background-color @old-color}} " Click here to change the colour : "
      [:input {:type "button" :value "Click me!"
               :on-click  #(do (reset!  old-color  @my-color-atom)
                               (show-color @my-color-atom))}] ] ]))

(defn my-orange-button []
  (fn []
    [:div
     [:span  " Click here to change the colour : "
      [:input {:type "button" :value "ORANGE!"
               :on-click #(do (reset!  old-color   "orange")
                              (show-color "orange") )}] ]

     ] ) )


(defn my-blue-button []
  (fn []
    [:div
     [:span  " Click here to change the colour : "
      [:input {:type "button" :value "BLUE!"
               :on-click #(do (reset!  old-color   "blue")
                              (show-color "blue") )
               }] ]] ) )

(defn my-red-button []
  (fn []

    [:div

     [:span  " Click here to change the colour : "
      [:input {:type "button" :value "RED!"
               :on-click #(do (reset!  old-color   "red")
                              (show-color "red") )

               }] ]

     ]))




(defn check-box []
  (fn []
    [:div
     [:input {:type "checkbox", :id "col1", :name "red", :value "red"
              :on-click #(swap! my-checkbox-values assoc :red(-> % .-target .-checked))  }]
     [:label {:for "red"} " I have a red"]
     [:br]
     [:input {:type "checkbox", :id "col2", :name "orange", :value "orange"
              :on-click #(swap! my-checkbox-values assoc :orange(-> % .-target .-checked))}]
     [:label {:for "orange"} " I have a orange"]
     [:br]
     [:input {:type "checkbox", :id "col3", :name "blue", :value "Blue"
              :on-click #(swap! my-checkbox-values assoc :blue(-> % .-target .-checked))}]
     [:label {:for "blue"} " I have a blue"]
     [:br]
     [:br]
     [:input {:type "submit", :value "Submit"}]]

    ))

(defn show-buttons [my-checkbox-values]
   [:div
    (if (get @my-checkbox-values :red) [my-red-button]  nil)
    (if (get @my-checkbox-values :orange) [my-orange-button]  nil)
    (if (get @my-checkbox-values :blue) [my-blue-button]  nil)]

  )



(defn my-new-page []
  (fn [] [:span.main
          [:h1 "Welcome to my new page"] [change-color] [my-button]   [my-current-color @old-color] [show-all-values @prev-color] [check-box] [show-buttons my-checkbox-values]

          ]))

(defn items-page []
  (fn []
    [:span.main
     [:h1 "The items of my-test-reagent"]
     [:ul (map (fn [item-id]
                 [:li {:name (str "item-" item-id) :key (str "item-" item-id)}
                  [:a {:href (path-for :item {:item-id item-id})} "Item: " item-id]])
               (range 1 60))]]))


(defn item-page []
  (fn []
    (let [routing-data (session/get :route)
          item (get-in routing-data [:route-params :item-id])]
      [:span.main
       [:h1 (str "Item " item " of my-test-reagent")]
       [:p [:a {:href (path-for :items)} "Back to the list of items"]]])))


(defn about-page []
  (fn [] [:span.main
          [:h1 "About my-test-reagent"]
          ]))


;; -------------------------
;; Translate routes -> page components

(defn page-for [route]
  (case route
    :index #'home-page
    :about #'about-page
    :items #'items-page
    :item #'item-page
    :my-new-route #'my-new-page))


;; -------------------------
;; Page mounting component


(defn current-page []
  (fn []
    (let [page (:current-page (session/get :route))]
      [:div
       [:header
        [:p [:a {:href (path-for :index)} "Home"] " | "
         [:a {:href (path-for :about)} "About my-test-reagent"] " | "
         [:a {:href (path-for :my-new-route)} "About my-new-page"]]]
       [page]
       [:footer
        [:p "my-test-reagent was generated by the "
         [:a {:href "https://github.com/reagent-
         project/reagent-template"} "Reagent Template"] "."]]])))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (clerk/initialize!)
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (let [match (reitit/match-by-path router path)
             current-page (:name (:data  match))
             route-params (:path-params match)]
         (reagent/after-render clerk/after-render!)
         (session/put! :route {:current-page (page-for current-page)
                               :route-params route-params})
         (clerk/navigate-page! path)
         )
       )
     :path-exists?
     (fn [path]
       (boolean (reitit/match-by-path router path)))})
  (accountant/dispatch-current!)
  (mount-root))

