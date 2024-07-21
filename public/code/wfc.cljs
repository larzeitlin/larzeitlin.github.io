(require '[reagent.core :as r]
         '[reagent.dom :as rdom]
         '[clojure.set :as set])

(def counter (atom 0))

(defn tile-grid [{:keys [tile-width-px n-columns]}
                 tiles]
  (let [style {:display "grid"
               :width (str (* tile-width-px n-columns) "px")
               :grid-template-columns (str "repeat(auto-fill, "
                                           tile-width-px
                                           "px)")}]
    (into [:div {:style style}] tiles)))

(defn tile-img [{:keys [tile-img-path
                        width-px
                        idx]}]
  [:img {:src tile-img-path
         :id (str "cell" idx)
         :style {:width (str width-px "px")
                 :height (str width-px "px")
                 :max-width "100%"}}])

(def tile-dir "assets/tiles/")

(def tile-paths
  {:water (str tile-dir "tile_10_1.png")
   :grass (str tile-dir "tile_4_12.png")
   :grass-coast-w (str tile-dir "tile_9_1.png")
   :grass-coast-s+w (str tile-dir "tile_9_2.png")
   :grass-coast-s (str tile-dir "tile_10_2.png")
   :grass-coast-e (str tile-dir "tile_11_1.png")
   :grass-coast-s+e (str tile-dir "tile_11_2.png")
   :grass-coast-n+e (str tile-dir "tile_11_0.png")
   :grass-coast-n (str tile-dir "tile_10_0.png")
   :grass-coast-n+w (str tile-dir "tile_9_0.png")
   :grass-coast-nw-corner (str tile-dir "tile_11_11.png")
   :grass-coast-ne-corner (str tile-dir "tile_9_11.png")
   :grass-coast-sw-corner (str tile-dir "tile_11_9.png")
   :grass-coast-se-corner (str tile-dir "tile_9_9.png")})

(def empty-tile (str tile-dir "tile_14_9.png"))

(defn random-tile []
  [tile-img
   {:tile-img-path (-> tile-paths vals rand-nth)
    :width-px 16}])

(defn world []
  (let [tiles (take 25 (repeatedly random-tile))]
    [tile-grid
     {:tile-width-px 16
      :n-columns 5}
     tiles]))

(rdom/render
 [world]
 (.getElementById js/document "random-world"))

(def all-tile-edges
  {:water {:n :water :ne :water :e :water :se :water :s :water :sw :water :w :water :nw :water}
   :grass {:n :grass :ne :grass :e :grass :se :grass :s :grass :sw :grass :w :grass :nw :grass}
   :grass-coast-n {:n :grass :ne :grass :e :water :se :water :s :water :sw :water :w :water :nw :grass}
   :grass-coast-e {:n :water :ne :grass :e :grass :se :grass :s :water :sw :water :w :water :nw :water}
   :grass-coast-s {:n :water :ne :water :e :water :se :grass :s :grass :sw :grass :w :water :nw :water}
   :grass-coast-w {:n :water :ne :water :e :water :se :water :s :water :sw :grass :w :grass :nw :grass}
   :grass-coast-ne-corner {:n :water :ne :grass :e :water :se :water :s :water :sw :water :w :water :nw :water}
   :grass-coast-nw-corner {:n :water :ne :water :e :water :se :water :s :water :sw :water :w :water :nw :grass}
   :grass-coast-se-corner {:n :water :ne :water :e :water :se :grass :s :water :sw :water :w :water :nw :water}
   :grass-coast-sw-corner {:n :water :ne :water :e :water :se :water :s :water :sw :grass :w :water :nw :water}
   :grass-coast-n+e {:n :grass :ne :grass :e :grass :se :grass :s :water :sw :water :w :water :nw :grass}
   :grass-coast-n+w {:n :grass :ne :grass :e :water :se :water :s :water :sw :grass :w :grass :nw :grass}
   :grass-coast-s+e {:n :water :ne :grass :e :grass :se :grass :s :grass :sw :grass :w :water :nw :water}
   :grass-coast-s+w {:n :water :ne :water :e :water :se :grass :s :grass :sw :grass :w :grass :nw :grass}})

(def edge->sockets
  {:n {:n :s :nw :sw :ne :se}
   :s {:s :n :sw :nw :se :ne}
   :e {:e :w :se :sw :ne :nw}
   :w {:w :e :sw :se :nw :ne}
   :ne {:ne :sw}
   :sw {:sw :ne}
   :nw {:nw :se}
   :se {:se :nw}})

(def invert-direction
  {:n :s
   :s :n
   :e :w
   :w :e
   :ne :sw
   :sw :ne
   :se :nw
   :nw :se})

(defn adjacent-tile-valid-sockets [input-tile-edges edge]
  (let [required-sockets (edge->sockets edge)]
    (->>
     (for [[from-edge to-edge] required-sockets]
       (let [tile-edge-type (from-edge input-tile-edges)]
         [to-edge tile-edge-type]))
     (into {}))))

(defn valid-tiles [valid-sockets tile-edges]
  (->> tile-edges
       (filter
        (fn [[_ edges]]
          (set/subset? (set valid-sockets)
                       (set edges))))
       (map first)
       set))

(def all-tile-types (keys tile-paths))

(defn init-grid [width]
  (vec
   (take (* width width)
         (repeat (set all-tile-types)))))

(defn find-lowest-entropy-idx [grid]
  (some->> grid
           (map-indexed (fn [idx poss-vals]
                          [idx (count poss-vals)]))
           shuffle
           (sort-by second)
           (drop-while #(< (second %) 2))
           first
           first))

(defn find-neighbours [grid-width idx]
  (let [first-row? (< idx grid-width)
        first-in-row? (zero? (mod idx grid-width))
        last-in-row? (= (dec grid-width)
                        (mod idx grid-width))
        last-row? (>= idx (* grid-width
                             (dec grid-width)))]
    (->>
     {:n (when-not first-row?
           (- idx grid-width))
      :ne (when-not
           (or first-row? last-in-row?)
            (- idx (dec grid-width)))
      :e (when-not last-in-row? (inc idx))
      :se (when-not (or last-in-row? last-row?)
            (+ idx grid-width 1))
      :s (when-not last-row?
           (+ idx grid-width))
      :sw (when-not (or first-in-row? last-row?)
            (+ idx grid-width -1))
      :w (when-not first-in-row?
           (dec idx))
      :nw (when-not (or first-row? first-in-row?)
            (- idx (inc grid-width)))}
     (filter val)
     (into {}))))

(defn remaining-possibilities
  [grid-width grid idx]
  (let [neighbours (find-neighbours grid-width idx)
        prev-possibilities (nth grid idx)
        possibilities-from-resolved-neighbours
        (->>
         (for [[direction nei-idx] neighbours
               :let [nei (nth grid nei-idx)]
               :when (= (count nei) 1)
               :let [nei-type (-> nei vec first)
                     nei-tile-edges (all-tile-edges nei-type)
                     valid-sockets (adjacent-tile-valid-sockets
                                    nei-tile-edges
                                    (invert-direction direction))]]
           (valid-tiles valid-sockets all-tile-edges))
         (apply set/intersection))]
    (or possibilities-from-resolved-neighbours
        prev-possibilities)))

(defn update-cell-reducer-fn [grid-width]
  (fn [grid idx]
    (let [prev-types (nth grid idx)]
      (if (= 1 (count prev-types))
        grid
        (assoc grid idx
               (remaining-possibilities grid-width grid idx))))))

(defn update-neighbours [grid grid-width idx]
  (let [neighbours (find-neighbours grid-width idx)]
    (reduce (update-cell-reducer-fn grid-width)
            grid
            (vals neighbours))))

(defn resolve-tile [possible-values]
  (->> possible-values
       vec
       shuffle
       (take 1)
       set))

(defn iterate-grid [grid-width {:keys [grid finished?]}]
  (let [lowest-entropy-idx (find-lowest-entropy-idx grid)]
    (if (or finished? (not lowest-entropy-idx))
      {:finished? true
       :grid grid}
      {:finished? false
       :grid (-> grid
                 (assoc lowest-entropy-idx
                        (resolve-tile
                         (remaining-possibilities grid-width
                                                  grid
                                                  lowest-entropy-idx)))
                 (update-neighbours grid-width lowest-entropy-idx))})))

(defn wfc-grid [n-columns grid]
  [tile-grid {:tile-width-px 16
              :n-columns n-columns}
   (map-indexed
    (fn [idx cell-possible-values]
      [tile-img
       {:idx idx
        :tile-img-path
        (if (= (count cell-possible-values) 1)
          (tile-paths (-> cell-possible-values vec first))
          empty-tile)
        :width-px 16}])
    grid)])

(defn interactive-map [n-columns]
  (let [state (r/atom {:grid (init-grid n-columns)
                       :finished? false})]
    (fn [_]
      [:div
       [wfc-grid n-columns (:grid @state)]
       [:div {:style {:display "flex"}}
        [:p [:button
             {:on-click #(swap! state
                                (partial iterate-grid n-columns))}
             "iterate"]]
        [:p [:button
             {:on-click #(while (not (:finished? @state))
                           (swap! state
                                  (partial iterate-grid n-columns)))}
             "finish"]]
        [:p [:button
             {:on-click #(reset! state
                                 {:grid (init-grid n-columns)
                                  :finished? false})}
             "reset"]]]])))

(rdom/render
 [interactive-map 20]
 (.getElementById js/document "interactive-map"))
