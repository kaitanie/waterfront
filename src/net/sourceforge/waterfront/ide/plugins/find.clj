
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
    
(defn- find-next-offset
  ([app]      
  (let [cyclic (get (app :search-settings) "Wrap search")
        x (first (app :last-search))]
    (if (and (not-nil? x) (pos? (count x)))
      (let [s (make-lower-if-needed app (.getText (app :area)))]
        (find-next-offset app (.getSelectionEnd (app :area)) (make-lower-if-needed app x) s cyclic))
      nil )))

  ([app initial-offset x s cyclic]       
  (let [offset (.indexOf s x initial-offset )]
    (cond 
      
      (>= offset 0)
      [offset (count x)]

      (not cyclic)
      nil

      (zero? initial-offset)
      nil

      :else
      (find-next-offset app 0 x s cyclic) ))))
      
(defn- offset-finder [cyclic x s initial-offset]       
  (let [offset (.indexOf s x initial-offset)]  

    (cond       
      (>= offset 0)
      [offset (count x)]

      (not cyclic)
      nil

      (zero? initial-offset)
      nil

      :else
      (offset-finder cyclic x s 0) )))

(defn- get-locations 
  ([app] 
  (let [cyclic (get (app :search-settings) "Wrap search")
        x (first (app :last-search))]
    (if (and x (pos? (count x)))
      (let [s (make-lower-if-needed app (.getText (app :area)))
            finder (partial offset-finder cyclic (make-lower-if-needed app x) s)]
        (reverse (get-locations finder (.getSelectionEnd (app :area))  nil)) )
      nil )))

  ([finder start-from results]
  (let [offset-length (finder start-from)]
    (if (nil? offset-length)
      results
      (recur finder (apply + offset-length) (cons offset-length results)) ))))
  
      


(defn- scroll-to [app offset-length]
    (if (nil? offset-length)
      app
      (do 
        (select-and-scroll-to (app :area) (first offset-length) (second offset-length))
        app )))

(defn find-next [app]      
  (scroll-to app (find-next-offset app)) )



(defn- new-recent-list [app key new-item]
  (let [old (if (app get key) (app get key) [])
        old-list (if (coll? old) old [old])
        dropped (take 9 (filter (fn [x] (not= x new-item)) old-list))]
    (if new-item
      (apply vector (cons new-item dropped))
      (apply vector dropped) )))


(defn find-in-document [app] 
  (let [current-selection (get-selected-text app nil)
        old-settings (merge {} (app :search-settings))

        searches (new-recent-list app :last-search current-selection) 
        search-settings (show-input-form 
            nil                   
            { :title "Find" :ok "Find" :cancel "Close" :msg nil :width (old-settings :width) :height (old-settings :height) }
            (javax.swing.JLabel. "\n\n")
            (fn [model] nil)
            { :name "Find:" :value searches :validator (fn [x] (if (zero? (count x)) "too short" nil)) }
            { :name "Case sensitive" :value (get old-settings "Case sensitive") } 
            { :name "Wrap search" :value (get old-settings "Wrap search") })
        find-what (if search-settings (get search-settings "Find:") nil)
        new-app (if find-what 
         (assoc app :last-search (new-recent-list app :last-search find-what)) app)]
    (when (and find-what (not (empty? (new-app :last-search))))
      (find-next (assoc new-app :search-settings search-settings)) )))


(defn- replace-once [app replace-with repeat]
  (assert replace-with)
  (.replaceSelection (app :area) replace-with)
  (repeat)
  app )
  
  
(defn- replace-loop [app locations]
  "locations is a collection of [offest length] pairs"
  (let [repeat (fn [] ((app :later) (fn [app] (replace-loop app (rest locations)))) app)]
    (if (empty? locations)
      app
      (do
        (scroll-to app (first locations))
        (let [replace-with (get (app :search-settings) "Replace with:")
              reply (javax.swing.JOptionPane/showOptionDialog 
                      (app :frame)
                      (str "Replace with '" replace-with "' ?")
                      "Replace"
                      javax.swing.JOptionPane/YES_NO_CANCEL_OPTION
                      javax.swing.JOptionPane/QUESTION_MESSAGE
                      nil
                      (into-array ["Yes" "No" "Yes to All" "Cancel"])
                      "Yes")]
          (cond
            (= reply 0) ; Yes
            (replace-once app replace-with repeat)
  
            (= reply 1) ; No
            (repeat)
  
            (= reply 2) ; All
            (repeat)
  
            :else
            app ))))))

    
(defn replace-in-document [app] 
  (let [current-selection (get-selected-text app nil)
        old-settings (merge {} (app :search-settings))

        searches (new-recent-list app :last-search current-selection)
        replacements (new-recent-list app :last-replace-with nil)
        search-settings (show-input-form 
            nil                   
            { :title "Replace" :ok "Replace..." :cancel "Close" :width (old-settings :width) :height (old-settings :height) }
            (javax.swing.JLabel. "\n\n")
            (fn [model] nil)
            { :name "Find:" :value searches :validator (fn [x] (if (zero? (count x)) " " nil)) }
            { :name "Replace with:" :value replacements :validator (fn [x] nil) })]
        (if (not search-settings)
          nil
          (let [find-what (get search-settings "Find:")
                replace-with (get search-settings "Replace with:")
                new-app (assoc app 
                  :search-settings search-settings 
                  :last-search (new-recent-list app :last-search find-what)
                  :last-replace-with (new-recent-list app :last-replace-with replace-with))]
            (replace-loop new-app (get-locations new-app)) ))))




(fn [app] 
  (add-to-menu 
    (load-plugin 
      (assoc app :keys-to-save  (apply vector (distinct (cons :last-replace-with (cons :search-settings (app :keys-to-save))))))
      "menu-observer.clj") 
    "Edit" 
    {}
    { :name "Find" :mnemonic KeyEvent/VK_F :key KeyEvent/VK_F :action find-in-document  }
    { :name "Find Next" :mnemonic KeyEvent/VK_N :key KeyEvent/VK_F3 :mask 0 :action find-next } 
    { :name "Replace" :mnemonic KeyEvent/VK_R :key KeyEvent/VK_R :action replace-in-document  }))























