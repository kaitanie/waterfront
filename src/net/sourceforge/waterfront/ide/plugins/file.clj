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
  (not= (.getText (app :area)) (app :initial-text)) )
  

; actions

(def save-directly (fn [app]
 (let [text (.getText (app :area))]                          
   (assert (not (unknown-document? app)))
   (.println (app :log) (print-str "saving to" (get-current-document app)))
   (write-file text (get-current-document app))
   (assoc app :initial-text text) )))
 
(defn- save-as [app]
  (when (and
      (= (. javax.swing.JFileChooser APPROVE_OPTION) (.showSaveDialog (app :file-chooser) (app :frame)))
      (.getSelectedFile (app :file-chooser)))
    (let [f (.. (app :file-chooser) (getSelectedFile) (getAbsoluteFile))
          name (.getName f)
          dot (.indexOf (inspect name) ".")
          fixed-name (if (neg? dot)
                        (str name ".clj")
                        name )
          fixed-file (java.io.File. (.getParent f) (inspect fixed-name))]
      (save-directly (set-current-document app fixed-file)) )))

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
     



(defn save-and-or-do-something [app do-something]
  (if (not (is-dirty app))
    (do-something app)
    (let [file-name (if (unknown-document? app) 
                          "Unnamed" 
                          (.getName (get-current-document app)))
          reply (JOptionPane/showOptionDialog (app :frame)
                    (str "'" file-name "' has been modified. Save changes?")
                    "Save File" JOptionPane/YES_NO_CANCEL_OPTION JOptionPane/QUESTION_MESSAGE, nil, (to-array ["Yes" "No" "Cancel"]), "Cancel"  )
          temp (if (= reply 0) ; Save! 
            (save-now app) 
            app)]
      (if (or (= reply JOptionPane/CLOSED_OPTION) (= reply 2)) ; Cancel!
        temp
        (do 
          ((app :later) do-something) 
          temp )))))


(defn- open-now [app]
  (if (not= javax.swing.JFileChooser/APPROVE_OPTION (.showOpenDialog (app :file-chooser) (app :frame)))
    app
    (load-document (set-current-document app (.getSelectedFile (app :file-chooser)))) ))

(defn open-file [app]
  (save-and-or-do-something app open-now) )


(defn- close-main-window [app]
  (save-config app)
  (.dispose (app :frame)) 
  (when (zero? (swap! (app :window-counter) dec))
    (try (.close (app :log))
      (catch Throwable t )) ; ignore failures in shutdown
    (System/exit 0) )
  app ) 


(defn exit-application [app] 
  (save-and-or-do-something app close-main-window) )

(defn- revert [app]
  (if (not (is-dirty app))
    (load-document app)
    (let [file-name (if (unknown-document? app) 
                          "Unnamed" 
                          (.getName (get-current-document app)))
          reply (javax.swing.JOptionPane/showOptionDialog 
                  nil
                  "You have unsaved changes. Do you want to discard them?" 
                  "Revert File" 
                  javax.swing.JOptionPane/YES_NO_OPTION 
                  javax.swing.JOptionPane/QUESTION_MESSAGE
                  nil 
                  (to-array ["Discard changes" "Cancel"]) 
                  "Cancel"  )]
          (if (= reply 0) ; Discard! 
            (load-document app)
            app ))))


(defn add-file-menu [app]
  (add-to-menu app "File" 
    { :name "New Window" :mnemonic KeyEvent/VK_N :key KeyEvent/VK_N :action (fn m-new [app] 
                              (new-waterfront-window app (merge app {
                                :title-prefix (app :title-prefix)
                                :file-name :unknown :initial-text "" :x0 (+ 40 (app :x0)) :y0 (+ 40 (app :y0)) }))
                              app) }
    { :name "Open" :mnemonic KeyEvent/VK_O :key KeyEvent/VK_O :action open-file }
    { :name "Revert" :action revert }
    { :name "Save" :mnemonic KeyEvent/VK_S :key KeyEvent/VK_S :action save-now }
    { :name "Save as..." :mnemonic KeyEvent/VK_A :action save-as }
    {}
    { :name "Exit" :mnemonic KeyEvent/VK_X :action exit-application } ))

(defn add-chooser [app]
  (let [chooser (javax.swing.JFileChooser. (. System getProperty "user.dir"))
        filter (javax.swing.filechooser.FileNameExtensionFilter. "Clojure files" (into-array String ["clj"]))]
    (.setFileFilter chooser filter) 
    (assoc app :file-chooser chooser) ))

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





