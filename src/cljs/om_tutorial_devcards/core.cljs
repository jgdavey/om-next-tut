(ns om-tutorial-devcards.core
  (:require [devcards.core :as dc :include-macros true]
            [om-tutorial.core :as tut]
            [sablono.core :as sab :include-macros true])
  (:require-macros [devcards.core :refer [defcard]]))

(enable-console-print!)
(dc/start-devcard-ui!)
