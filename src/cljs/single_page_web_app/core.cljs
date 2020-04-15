(ns single-page-web-app.core
  (:require
   [reagent.core :as reagent :refer [atom]]
   [reagent.session :as session]
   [reitit.frontend :as reitit]
   [clerk.core :as clerk]
   [accountant.core :as accountant]))

;; -------------------------
;; Routes

(def my-global-atom (reagent/atom "without passing value & using global argument"))
(def my-global-atom2 (reagent/atom "with passing refrenced value of the atom"))
(def atom3 (reagent/atom "with passing atom valueand not the refrenced value"))

(def atom4 (reagent/atom "my value can be changed"))

(defn text-input [value]
  (js/console.log " text-input rendered")
  [:input {:type "text"
           :value @value
           :on-change #(reset! value (-> % .-target .-value))}])

(defn change-text []
  (js/console.log " change-text rendered")
  (fn []
    [:div
     [:p "Change the text here: " [text-input atom4]
      [:p "This is your new text: " @atom4]
      ]]))

(defn with-passing-atom [value]
  (js/console.log "with-passing-atom rerendered")
  [:div
   " MY function type is:- " @value]
  )

(defn with-passing-arg [value]
  (js/console.log "with-passing-refrenced-value rerendered")
  [:div
   " MY function type is:- " value]
  )
(defn without-passing-arg []
  (js/console.log "without-passing-arg rerendered")
  [:div
   " MY function type is:- " @my-global-atom]
  )

(def router
  (reitit/router
   [["/" :index]
  ]))

(defn path-for [route & [params]]
  (if params
    (:path (reitit/match-by-name router route params))
    (:path (reitit/match-by-name router route))))

;; -------------------------
;; Page components

(defn home-page []
  (js/console.log "before the anonymous function in home page")
  (fn []
    (js/console.log "INSIDE the anonymous function in home page")
    [:span.main
     [:h1 "Welcome to single- page-web - app"]
     [with-passing-arg @my-global-atom2]
     [without-passing-arg ]
     [with-passing-atom atom3]
     [ change-text ]]))








;; -------------------------
;; Translate routes -> page components

(defn page-for [route]
  (case route
    :index #'home-page
    ))


;; -------------------------
;; Page mounting component

(defn current-page []
  (fn []
    (let [page (:current-page (session/get :route))]
      [:div
       [page]
])))

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
        ))
    :path-exists?
    (fn [path]
      (boolean (reitit/match-by-path router path)))})
  (accountant/dispatch-current!)
  (mount-root))
