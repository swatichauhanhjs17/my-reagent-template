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


(defn my-button []
  [:div
   "This is my trial-button:"
 [:div {:style {:background-color "white"
                ::stylefy/mode [[:before {:content "'CSS generated content'"}]
                                [:hover {:background-color "#ffedcf"}]
                                [:active {:background-color "blue" :color "white"}]]}}  [:input {:type "button" :value "Click me!"
                                                                                                        }]] ])
(defn my-new-page []
  (fn [] [:span.main
          [:h1 "Welcome to my new page"] [my-button]
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
