(ns om-tutorial.core
  (:require-macros [devcards.core :as dc :refer [defcard deftest]])
  (:require [goog.dom :as gdom]
            [goog.crypt :as gcrypt]
            [cognitect.transit :as t]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [datascript.core :as d]
            [cljs.pprint :as pprint])
  (:import [goog.crypt Sha256]))

(enable-console-print!)
(def conn (d/create-conn {}))

(d/transact! conn
  [{:db/id -1
    :app/title "Hello, DataScript!"
    :app/count 0}
   {:db/id -2
    :app/title "Another thing"
    :app/count 50}])

(defmulti read om/dispatch)

(defmethod read :app/counters
  [{:keys [state selector]} _ _]
  {:value (d/q '[:find [(pull ?e ?selector) ...]
                 :in $ ?selector
                 :where [?e :app/title]]
            (d/db state) selector)})

(defmulti mutate om/dispatch)

(defmethod mutate 'app/increment
  [{:keys [state]} _ {:keys [db/id] :as entity}]
  {:action
   (fn []
     (d/transact! state [(update-in entity [:app/count] inc)]))})

(defui Counter
  static om/Ident
  (ident [this {:keys [db/id]}]
    [:counter/by-id id])
  static om/IQuery
  (query [this]
    [:db/id :app/title :app/count])
  Object
  (render [this]
    (let [{:keys [app/title app/count] :as entity} (om/props this)]
      (dom/div nil
        (dom/h2 nil title)
        (dom/span nil (str "Count: " count))
        (dom/button
          #js {:onClick
               (fn [e]
                 (om/transact! this
                   `[(app/increment ~entity)]))}
          "Click me!")))))

(def counter (om/factory Counter {:keyfn :db/id}))

(defui CounterList
  Object
  (render [this]
    (println "Render CounterList" (-> this om/path))
    (let [list (om/props this)]
      (apply dom/ul nil
        (map counter list)))))

(def list-view (om/factory CounterList))

(defui App
  static om/IQuery
  (query [this]
     `[{:app/counters ~(om/get-query Counter)}])
  Object
  (render [this]
    (list-view (:app/counters (om/props this)))))

(def parser (om/parser {:read read :mutate mutate}))

(def reconciler (om/reconciler {:state conn :parser parser}))

(defn main []
  (println "Running main")
  (if-let [node (gdom/getElement "app")]
    (om/add-root! reconciler App node)))

(main)

(comment

(d/q '[:find ?e ?title ?count
       :where
       [?e :app/title ?title]
       [?e :app/count ?count]] (d/db conn))

)
