(ns bezier.home
  (:require [reagent.dom :as rd]
            [reagent.core :as r]))

(def point-0 (r/atom [10 10]))
(def point-1 (r/atom [100 100]))
(def point-2 (r/atom [100 350]))
(def point-3 (r/atom [100 90]))

(defn cubed [x]
  (* x x x))
(defn squared [x]
  (* x x))


(defn vector-add [[x1 y1] [x2 y2]]
  [(+ x1 x2) (+ y1 y2)])

(defn vector-multiply [[x y] d]
  [(* d x) (* d y)])


(defn bezier-point [t p0 p1 p2 p3]
  (let [one-minus-t (- 1.0 t)
        term-0 (* one-minus-t one-minus-t one-minus-t)
        term-1 (* 3.0 (squared one-minus-t) t)
        term-2 (* 3.0 one-minus-t (squared t))
        term-3 (* 1.0 t t t)]
    (-> (vector-multiply p0 term-0)
        (vector-add (vector-multiply p1 term-1))
        (vector-add (vector-multiply p2 term-2))
        (vector-add (vector-multiply p3 term-3)))))

(def slider-value (r/atom 50))
(defn app []
  [:<>
   [:canvas#c {:height 800 :width 800
               :style {:border "1px solid blue"}}]
   [:div
    ;; [:input {:type "range"
    ;;          :id "slider"
    ;;          :min "0"
    ;;          :max "100"
    ;;          :defaultValue "50"
    ;;          :class "slider"
    ;;          :on-input  (fn [evt]
    ;;                       (reset!
    ;;                        slider-value
    ;;                        (double (. (. evt -target) -value))))}]
    ]
   [:p (str {:point-0 @point-0
             :point-1 @point-1
             :point-2 @point-2
             :point-3 @point-3})]])

(defn get-canvas []
  (.getElementById js/document "c"))

(defn get-canvas-ctx []
  (.getContext (get-canvas) "2d"))

(defn clear-canvas [ctx]
  (.clearRect ctx 0 0 800 800))

(defn draw-bezier [ctx bezier]
  (let [{:keys [point-0 point-1 point-2 point-3]} bezier
        [x0 y0] point-0
        [x1 y1] point-1
        [x2 y2] point-2
        [x3 y3] point-3]
    (.beginPath ctx)
    (.moveTo ctx x0 y0)
    (.bezierCurveTo ctx x1 y1 x2 y2 x3 y3)
    (.stroke ctx)))


(defn vec-magnitude [[x y]]
  (Math/sqrt (+ (squared x) (squared y))))

(defn coordinates [x y]
  [x y])
(defn get-x [coordinates]
  (first coordinates))
(defn get-y [coordinates]
  (second coordinates))

(defn draw-line [ctx p1 p2]
  (let [[x1 y1] p1
        [x2 y2] p2]
    (.beginPath ctx)
    (.moveTo ctx x1 y1)
    (.lineTo ctx x2 y2)
    (.stroke ctx)))

(defn draw-circle [ctx radius coordinates]
  (let [x (get-x coordinates)
        y (get-y coordinates)]
    (.beginPath ctx)
    (.arc ctx x y radius 0 (* 2 Math/PI))
    (.fill ctx)))



(defn get-events []
  ;; TODO
  )
(defn update-state []
  ;; TODO
  )


(defn draw []
  (let [ctx (get-canvas-ctx)]
    (clear-canvas ctx)
    (draw-circle ctx 5 @point-0)
    (draw-circle ctx 5 @point-1)
    (draw-circle ctx 5 @point-2)
    (draw-circle ctx 5 @point-3)
    (draw-line ctx @point-0 @point-1)
    (draw-line ctx @point-2 @point-3)
    (draw-bezier ctx {:point-0 @point-0
                      :point-1 @point-1
                      :point-2 @point-2
                      :point-3 @point-3})))


(defn on-point? [[x y] [point-x point-y]]
  (<
   (+ (squared (- x point-x))
      (squared (- y point-y)))
   (squared 5)  ;; Click radius 
   ))
(def point-moving (atom nil))
;; TODO put the points as paramters
(defn check-points [[x y]]
  (cond
    (on-point? [x y] @point-0)
    (reset! point-moving 0)
    (on-point? [x y] @point-1)
    (reset! point-moving 1)
    (on-point? [x y] @point-2)
    (reset! point-moving 2)
    (on-point? [x y] @point-3)
    (reset! point-moving 3)))

(defn atom-by-index [i]
  (case i
    0 point-0
    1 point-1
    2 point-2
    3 point-3))

(defn move [i [x y]]
  (let [atom-to-move (atom-by-index i)]
    (reset! atom-to-move [x y])))

(defn add-click-listener []
  (let [canv (get-canvas)]
    (.addEventListener js/document "mousedown"
                       (fn [evt] ;(.preventDefault evt)
                         (let [x (- (. evt -pageX)
                                    (. canv -offsetLeft))
                               y (- (. evt -pageY)
                                    (. canv -offsetTop))]
                           (check-points [x y]))))
    (.addEventListener js/document "mousemove"
                       (fn [evt]
                         (when @point-moving
                           (let [x (- (. evt -pageX)
                                      (. canv -offsetLeft))
                                 y (- (. evt -pageY)
                                      (. canv -offsetTop))]
                             (move @point-moving [x y])))))
    (.addEventListener canv "mouseup"
                       (fn [evt]
                         (reset! point-moving nil)))))



(defn game-loop []
  (let [evts (get-events)]
    (update-state)
    (draw)
    (.requestAnimationFrame js/window game-loop)))

(defn init []
  (rd/render [app]
             (.getElementById js/document "root"))
  (add-click-listener)
  (game-loop))
