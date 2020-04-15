(ns single-page-web-app.prod
  (:require [single-page-web-app.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
