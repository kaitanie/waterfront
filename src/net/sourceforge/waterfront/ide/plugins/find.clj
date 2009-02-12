
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


(defn find-next [app]      
  (let [x (app :last-search)]
    (when (and (not-nil? x) (pos? (count x)))
      (let [s (.toLowerCase (.getText (app :area)))
            offset (.indexOf s (.toLowerCase x) (.getSelectionEnd (app :area)) )]
        (when (>= offset 0)
          (select-and-scroll-to (app :area) offset (count x)) )))
  app ))


(defn find-in-document [app] 
  (let [current-selection (get-selected-text app nil)

        default-search-string (if current-selection
                                current-selection
                                (if (app :last-search)
                                  (app :last-search) 
                                  "" ))  
  
        search-settings (show-input-form 
            nil                   
            "Search"    
            (javax.swing.JLabel. " ")
            (fn [model] nil)
            { :name "Find:" :value default-search-string :validator (fn [x] (if (zero? (count x)) :bad nil)) }
            { :name "Case sensitive" :value false } 
            { :name "Wrap search" :value false }
            { :name "Whole word" :value false })
        search-string (if search-settings (get search-settings "Find:") nil)
        new-app (assoc app :last-search search-string)]
    (find-next new-app) ))

(fn [app] 
  (add-to-menu (load-plugin app "menu-observer.clj") "Edit" 
    {}
    { :name "Find" :mnemonic KeyEvent/VK_F :key KeyEvent/VK_F :action find-in-document  }
    { :name "Find Next" :mnemonic KeyEvent/VK_N :key KeyEvent/VK_F3 :mask 0 :action find-next } ))




