
(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)

(require 'net.sourceforge.waterfront.ide.services.services)
(require 'net.sourceforge.waterfront.ide.services.selections)

(refer 'net.sourceforge.waterfront.ide.services)


(defn- set-find-status [app found?]
  (assoc app :find-status (if found? "" "String not found")) )

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

  ([app start-from x s cyclic]       
  (let [offset (.indexOf s x start-from )]
    (cond 
      
      (>= offset 0)
      [offset (count x)]

      (not cyclic)
      nil

      (zero? start-from)
      nil

      :else
      (find-next-offset app 0 x s cyclic) ))))
      
(defn- offset-finder [case-trnaslation cyclic x app start-from]       
  (let [offset (.indexOf (case-trnaslation (.getText (app :area))) (case-trnaslation x) start-from)]  

    (cond       
      (>= offset 0)
      [offset (count x)]

      (not cyclic)
      nil

      (zero? start-from)
      nil

      :else
      (offset-finder case-trnaslation cyclic x app 0) )))

(defn- scroll-to [app offset-length]
  (if (nil? offset-length)
    (set-find-status app false)
    (do 
      (select-and-scroll-to (app :area) (first offset-length) (second offset-length))
      (set-find-status app true) )))

(defn find-next [app]      
  (scroll-to app (find-next-offset app)) )



(defn- new-recent-list [app key new-item]
  (let [old (defaults-to (get app key) [])
        old-list (if (coll? old) old [old])
        dropped (take 9 (filter (fn [x] (not= x new-item)) old-list))
        result (if new-item
          (apply vector (cons new-item dropped))
          (apply vector dropped) )]
    result ))


(defn find-in-document [app] 
  (let [current-selection (get-selected-text app nil)
        old-settings (merge {} (app :search-settings))

        searches (new-recent-list app :last-search current-selection)
        search-settings (show-input-form 
            (app :frame)                   
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


(defn- replace-once [app replace-with reinvoke]
  (assert replace-with)
  (.replaceSelection (app :area) replace-with)
  (reinvoke)
  app )
 

(defn- find-all-locations [text find-what start-from results]
  (if (>= start-from (count text))
    results
    (let [offset (.indexOf text find-what start-from)]
      (if (neg? offset)
        results
        (recur text find-what (+ offset (count find-what)) (cons offset results)) ))))
  
(defn- copy-fragments [sb text prev-offset offsets find-what replace-with]
  (if (empty? offsets)
    (.append sb (.substring text prev-offset))    
    (do
      (.append sb (.substring text prev-offset (first offsets)))
      (.append sb replace-with)
      (recur sb text (+ (count find-what) (first offsets)) (rest offsets) find-what replace-with) )))

(defn- replace-all [app fix-case find-what replace-with start-from]
  (let [text (fix-case (.getText (app :area)))
        find-what-fixed (fix-case find-what)
        offsets (reverse (find-all-locations text find-what-fixed 0 nil))
        sb (StringBuilder.)]
    (when-not (empty? offsets)
      (copy-fragments sb (.getText (app :area)) 0 offsets find-what replace-with)
      (.setText (app :area) (str sb)) )))
      
    
 
(defn- replace-loop 
  ([app]
  (let [fix-case (partial make-lower-if-needed app) 
        cyclic (get (app :search-settings) "Wrap search")
        find-what (first (app :last-search)) 
        replace-with (first (app :last-replace-with)) 
        finder (partial offset-finder fix-case cyclic find-what)]
      ((app :later) (fn [app] (replace-loop app finder fix-case find-what replace-with)))
      app ))

  ([app finder fix-case find-what replace-with]
  (let [reinvoke (fn [] ((app :later) (fn [app] (replace-loop app finder fix-case find-what replace-with))))
        offset-length (finder app (.getSelectionEnd (app :area)))]
      (if (nil? offset-length)
        (set-find-status app false)
        (do
          (scroll-to app offset-length)
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
              (replace-once app replace-with reinvoke)
    
              (= reply 1) ; No
              (reinvoke)
    
              (= reply 2) ; All
              (replace-all app fix-case find-what replace-with 0)
    
              :else
              app )))))))

    
(defn replace-in-document [app] 
  (let [current-selection (get-selected-text app nil)
        old-settings (merge {} (app :search-settings))

        searches (new-recent-list app :last-search current-selection)
        replacements (new-recent-list app :last-replace-with nil)
        search-settings (show-input-form 
            (app :frame)                   
            { :title "Replace" :ok "Replace..." :cancel "Close" :width (old-settings :width) :height (old-settings :height) }
            (javax.swing.JLabel. "\n\n")
            (fn [model] nil)
            { :name "Find:" :value searches :validator (fn [x] (if (zero? (count x)) " " nil)) }
            { :name "Replace with:" :value replacements :validator (fn [x] nil) }
            { :name "Case sensitive" :value (get old-settings "Case sensitive") } 
            { :name "Wrap search" :value (get old-settings "Wrap search") })]
        (if (not search-settings)
          nil
          (let [find-what (get search-settings "Find:")
                replace-with (get search-settings "Replace with:")
                new-app (assoc app 
                  :search-settings search-settings 
                  :last-search (new-recent-list app :last-search find-what)
                  :last-replace-with (new-recent-list app :last-replace-with replace-with))]
            (replace-loop new-app) ))))

(fn [app] 
  (add-to-menu 
    (load-plugin 
      (assoc app :keys-to-save  (apply vector (distinct (cons :last-replace-with (cons :search-settings (app :keys-to-save))))))
      "menu-observer.clj" "find-indicator.clj") 
    "Edit" 
    {}
    { :name "Find" :mnemonic KeyEvent/VK_F :key KeyEvent/VK_F :action find-in-document  }
    { :name "Find Next" :mnemonic KeyEvent/VK_N :key KeyEvent/VK_F3 :mask 0 :action find-next } 
    { :name "Replace" :mnemonic KeyEvent/VK_R :key KeyEvent/VK_R :action replace-in-document  }))











