(ns my-test-reagent.prod
  (:require [my-test-reagent.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
