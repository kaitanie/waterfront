(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)

(import 
  '(javax.swing JFrame JLabel JScrollPane JTextField JButton JTextArea UIManager JMenuItem JMenu JMenuBar)
  '(javax.swing JPopupMenu KeyStroke JSplitPane JOptionPane)
  '(javax.swing.event CaretEvent CaretListener)
  '(javax.swing.text DefaultStyledDocument StyleConstants StyleConstants$CharacterConstants SimpleAttributeSet)
  '(java.awt Color)
  '(java.awt.event ActionListener KeyEvent ActionEvent)
  '(java.awt GridLayout BorderLayout Font EventQueue)
  '(java.io File))

(defn get-selected-text [app result-if-selection-empty]
  (test (not (nil? app))) 
  (let [t (.getSelectedText (app :area))]
    (if (= 0 (count t))
      result-if-selection-empty
      t)))



(def find-next (fn [app]      
   (let [x (app :last-search)]
    (when (and (not-nil? x) (pos? (count x)))
      (let [s (.toLowerCase (.getText (app :area)))
            offset (.indexOf s (.toLowerCase x) (.getSelectionEnd (app :area)) )]
        (when (>= offset 0)
          (.scrollRectToVisible (app :area) (.modelToView (app :area) offset))
          (.select (app :area) offset (+ offset (.length x))) ))))
   app))

(defn find-in-document [app] 
  (let [current-selection (get-selected-text app nil)
        search-string (.  JOptionPane showInputDialog "Search for: " 
                            (if (nil? current-selection) 
                              (if (nil? (app :last-search)) 
                                "" 
                                (app :last-search) )  
                              current-selection ))
        new-app (assoc app :last-search search-string)]
    (find-next new-app) ))





(fn [app] 
  (transform app :menu nil 
    (partial change-menu "Edit" (fn [items] (conj items 
    nil
    { :name "Find" :mnemonic KeyEvent/VK_F :key KeyEvent/VK_F :action find-in-document  }
    { :name "Find Next" :mnemonic KeyEvent/VK_N :key KeyEvent/VK_F3 :mask 0 :action find-next } )))))






