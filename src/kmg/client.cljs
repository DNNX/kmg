(ns kmg.client
  (:require [enfocus.core :as ef :refer (set-attr from at get-prop do-> after
                                                  remove-node content substitute)]
            [enfocus.events :as events :refer (listen)]
            [ajax.core :refer [GET POST]])
  (:require-macros [enfocus.macros :as em]))

(def user (atom {:username "user1"}))

(def tmpl "/html/kmg.html")

(em/defsnippet kmg-header tmpl ".kmg-header" [])
(em/defsnippet kmg-content tmpl "#content" [])
(em/defsnippet kmg-sidebar tmpl "#sidebar" [])

(em/defsnippet user-choose-elem tmpl ".user-option-id" [user]
  ".user-option-id" (ef/do-> (ef/content user)
                             (ef/set-attr :value user)))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "somthing bad happened: " status " " status-text)))

;; TODO: How to avoid blinking of sample value for select?
;; May be make it hide by-default and make it shown after transformation
(defn show-user-choose [users]
  (ef/at "#users-id" (ef/content (map user-choose-elem users))))

(defn try-get-users []
  (GET "/user/list" {:handler show-user-choose
                     :error-handler error-handler}))

(em/defaction observe-change-user []
  ["#users-id"]
  (events/listen :change
                 #((do (reset! user {:username (ef/from "#users-id" (get-prop :value))})
                       #_(js/alert (str "User changed. Current user: " @user))))))

(defn start []
  (ef/at ".container"
         (ef/do-> (ef/content (kmg-header))
                  (ef/append (kmg-content))
                  (ef/append (kmg-sidebar))))
  (try-get-users)

  (observe-change-user))

(set! (.-onload js/window) #(em/wait-for-load (start)))

;;(set! (.-onload js/window) (js/alert "Hello, world"))