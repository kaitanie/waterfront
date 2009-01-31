(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)
(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)

(import 
  '(javax.swing JFrame JLabel JScrollPane JTextField JButton JTextArea UIManager JMenuItem JMenu JMenuBar)
  '(javax.swing JPopupMenu KeyStroke JSplitPane JOptionPane)
  '(javax.swing.event CaretEvent CaretListener)
  '(javax.swing.text DefaultStyledDocument StyleConstants StyleConstants$CharacterConstants SimpleAttributeSet)
  '(java.awt Color)
  '(java.awt.event ActionListener KeyEvent ActionEvent)
  '(java.awt GridLayout BorderLayout Font EventQueue)
  '(java.io File))


(defn unknown-document? [app] 
  (= :unknown (app :file-name)) )

(defn get-current-document [app]           
  (assert (not (unknown-document? app)))
  (path-to-file (app :file-name)))

(defn get-current-document-path [app]           
  (assert (not (unknown-document? app)))
  (.getAbsolutePath (get-current-document app)))

(defn set-current-document [app file-or-string]
  (assoc app :file-name (.getAbsolutePath (path-to-file file-or-string))) )

(defn file-name-exists [app]
  (and (not (unknown-document? app)) (.exists (get-current-document app))))

(defn is-dirty [app]
  (not= 
   (.getText 
      (app :area) ) 
   (app :initial-text)) )
  

; actions

(def save-directly (fn [app]
 (let [text (.getText (app :area))]                          
   (assert (not (unknown-document? app)))
   (println "saving to" (get-current-document app))
   (write-file text (get-current-document app))
   (assoc app :initial-text text) )))
 
(def save-as (fn [app]
 (when (= (. javax.swing.JFileChooser APPROVE_OPTION) (.showSaveDialog (app :file-chooser) (app :frame)))
   (save-directly (set-current-document app (.getSelectedFile (app :file-chooser)))) )))

(def save-now (fn [app]
 (let [text (.getText (app :area))]
   (if (unknown-document? app)
     (save-as app)
     (save-directly app) ))))

(def load-document (fn [app]
 (let [new-app (assoc app 
   :loaded-at (java.util.Date.)
   :initial-text (if (file-name-exists app)
                   (.replace (slurp (get-current-document-path app)) "\r\n" "\n")
                   ""))]
    (.setText (new-app :area) (new-app :initial-text))
    (later (fn [] 
      (.scrollRectToVisible (new-app :area) (.modelToView (new-app :area) 0))
      (.setCaretPosition (new-app :area) 0) ))
   new-app )))
     

(def open-file (fn [app]
  (if (not= (. javax.swing.JFileChooser APPROVE_OPTION) (.showOpenDialog (app :file-chooser) (app :frame)))
    app
    (load-document (set-current-document app (.getSelectedFile (app :file-chooser)))) )))

(def exit-application (fn [app] 
  (if (not (is-dirty app))
      (do 
        (save-config app)
        (.dispose (app :frame))
        app)
      (let [file-name (if (unknown-document? app) 
                         "Unnamed" 
                         (.getName (get-current-document app)))
            reply (. JOptionPane showOptionDialog (app :frame)
                    (str "'" file-name "' has been modified. Save changes?")
                    "Save File" (. JOptionPane YES_NO_CANCEL_OPTION) JOptionPane/QUESTION_MESSAGE, nil, (to-array ["Yes" "No" "Cancel"]), "Cancel"  )]
        (when (= reply 0) ; Save!
          (save-now app))
        (when (and (not= reply JOptionPane/CLOSED_OPTION) (not= reply 2)) ; not Cancel!
          (save-config app)
          (.dispose (app :frame)) )
         app ))))


(defn add-file-menu [app]
  (add-to-menu app "File" 
    { :name "New" :mnemonic KeyEvent/VK_N :key KeyEvent/VK_N :action (fn m-new [app] 
                              (show-ecosystem-window (merge app {
                                :title-prefix (app :title-prefix)
                                :file-name :unknown :initial-text "" }))
                              app) }
    { :name "Open" :mnemonic KeyEvent/VK_O :key KeyEvent/VK_O :action open-file }
    { :name "Save" :mnemonic KeyEvent/VK_S :key KeyEvent/VK_S :action save-now }
    { :name "Save as..." :mnemonic KeyEvent/VK_A :action save-as }
    {}
    { :name "Exit" :mnemonic KeyEvent/VK_X :action exit-application } ))


(defn add-chooser [app]
  (assoc app :file-chooser (javax.swing.JFileChooser. (. System getProperty "user.dir"))) )

(defn update-title [old-app, app]
  (.setTitle (app :frame)
    (str "Waterfront " (app :title-prefix) ": "
      (if (unknown-document? app)
        "Unnamed"
        (str (if (is-dirty app) "*" "") (.getName (get-current-document app)) " - " (get-current-document-path app)) )))
   app)


(fn [app] 
  (.addWindowListener (app :frame)
    (proxy [java.awt.event.WindowAdapter] []
      (windowClosing [e] 
        ((app :dispatch) exit-application) )))
  
    (transform (add-file-menu (add-chooser (add-observers (load-plugin app "menu-observer.clj") update-title))) :actions {}
      (fn[curr] (assoc curr :load-document load-document)) ))


