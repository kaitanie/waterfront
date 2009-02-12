
(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)

(require 'net.sourceforge.waterfront.ide.services.services)
(require 'net.sourceforge.waterfront.ide.services.selections)

(refer 'net.sourceforge.waterfront.ide.services)


(defn get-selected-text [app result-if-selection-empty]
  (test (not (nil? app))) 
  (let [t (.getSelectedText (app :area))]
    (if (= 0 (count t))
      result-if-selection-empty
      t)))


(defn- make-lower-if-needed [app s]
  (let [case-sensitive (if (nil? (app :search-settings))
          false
          (get (app :search-settings) "Case sensitive" false))]
    (if case-sensitive
      s
      (.toLowerCase s) )))
    
(defn find-next 
  ([app]      
  (let [cyclic (get (app :search-settings) "Wrap search")
        x (first (app :last-search))]
    (if (and (not-nil? x) (pos? (count x)))
      (let [s (make-lower-if-needed app (.getText (app :area)))]
        (find-next app (.getSelectionEnd (app :area)) (make-lower-if-needed app x) s cyclic))
      app )))

  ([app initial-offset x s cyclic]       
  (let [offset (.indexOf s x initial-offset )]
    (cond 
      
      (>= offset 0)
      (do 
        (select-and-scroll-to (app :area) offset (count x))
        app )

      (not cyclic)
      app

      (zero? initial-offset)
      app 

      :else
      (find-next app 0 x s cyclic) ))))
      

(defn- new-recent-search-list [app search-string]
  (let [old (if (app :last-search) (app :last-search) [])
        old-list (if (coll? old) old [old])
        dropped (take 9 (filter (fn [x] (not= x search-string)) old-list))]
    (apply vector (cons search-string dropped))))


(defn find-in-document [app] 
  (let [current-selection (get-selected-text app nil)
        old-settings (merge {} (app :search-settings))

        searches (if current-selection (new-recent-search-list app current-selection) (app :last-search))  
        search-settings (show-input-form 
            nil                   
            { :title "Search" :ok "Find" :cancel "Close" }
            nil   
            (fn [model] nil)
            { :name "Find:" :value searches :validator (fn [x] (if (zero? (count x)) :bad nil)) }
            { :name "Case sensitive" :value (get old-settings "Case sensitive") } 
            { :name "Wrap search" :value (get old-settings "Wrap search") })
        search-result (if search-settings (get search-settings "Find:") nil)
        new-app (if search-result (assoc app :last-search (new-recent-search-list app search-result)) app)]
    (when (and search-result (not (empty? (new-app :last-search))))
      (find-next (assoc new-app :search-settings search-settings)) )))




(fn [app] 
  (add-to-menu 
    (load-plugin 
      (assoc app :keys-to-save  (apply vector (cons :search-settings (app :keys-to-save)))) 
      "menu-observer.clj") 
    "Edit" 
    {}
    { :name "Find" :mnemonic KeyEvent/VK_F :key KeyEvent/VK_F :action find-in-document  }
    { :name "Find Next" :mnemonic KeyEvent/VK_N :key KeyEvent/VK_F3 :mask 0 :action find-next } ))


