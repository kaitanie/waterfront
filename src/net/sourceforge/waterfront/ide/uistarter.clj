
; different thread for execution
; stop running button
; click on error -> jump to location
; ns, line numbers in error messages
; syntax coloring
; replace
; highlight current line
; scrapbook
; syntax checking also on selected text
; fix line numbers when running/syntax checking selected text
; View menu: show Output window, show Prolbems window
; jump to matching paren
; make undo/redo/copy/cut/paste/jump-to-matching enabled only when applicable
; recent searches drop box
; incremental search
; eval just like in REPL
; solve issues of window focus
; bug: (count (replicate 100 \a))
; multi-tab editor
; reformat source
; jump to decl.
; revert
; indent/unindent block
; allow show doc (F1) on symbols which are not from the global namespace
; make load document an observer-driven action triggered by a new :file-name value
; rename 
; find unused variables
; generate overloading
; extract function
; stop-and-inspect
; make window placement inside the frame a dynamic property (DSL specified by app, a-la :menu)
; change return value of (*app* :change)
; Status bar
; Help -> Status shows current plugins
; Help -> Env show environment
; Scrapbook file
; remember position in each file
; A green/red indicator to show compilation status - syntax 

; 28-Dec-08: plugins (setup function)
; 28-Dec-08: Bug fix - Exception in dispatch are now caught
; 28-Dec-08: show doc
; 28-Dec-08: goto
; 27-Dec-08: line wrapping
; 26-Dec-08: line end is always \n 
; 26-Dec-08: uncomment
; ??-Dec-08: interpret menu definitions
; ??-Dec-08: recently opened
; ??-Dec-08: make the list of app items that are saved part of app itself.
; 23-Jan-09: grouped undo/redo
; 25-Jan-09: make paren highlighting invisible WRT undo/redo
; 26-Jan-09: (app :change) is a functions that allow currently executing code to change app 
; 30-Jan-09: move word: stop at "-"
; 31-Jan-09: Load recent file on startup is now handled by a dedicated plugin


; Highlights:
;
; - spaces not tabs
; - comment lines
; - select and run
; - undo/redo
; - self reflection (ecosystem var.)
; - plugin loading
; - format code
; - true paren. matching
; - syntax coloring

(ns net.sourceforge.waterfront.ide)


(import 
  '(javax.swing JFrame JLabel JScrollPane JTextField JButton JTextArea UIManager JMenuItem JMenu JMenuBar)
  '(javax.swing JPopupMenu KeyStroke JSplitPane JOptionPane)
  '(javax.swing.event CaretEvent CaretListener)
  '(javax.swing.text DefaultStyledDocument StyleConstants StyleConstants$CharacterConstants SimpleAttributeSet)
  '(java.awt Color)
  '(java.awt.event ActionListener KeyEvent ActionEvent)
  '(java.awt GridLayout BorderLayout Font EventQueue)
  '(java.io File))


(require 'net.sourceforge.waterfront.kit.kit)
(refer 'net.sourceforge.waterfront.kit)


(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)

  
; domain specific 

(defn print-app [app]    
  (println "--------------------------------------------")
  (println "Observers:\n" (apply str (map (fn[x] (str "  -" x "\n")) (app :observers))))
  (println (pretty-print (select-keys app (conj (app :keys-to-save) :file-name :loaded-plugins))))
  (println "--------------------------------------------")
  app)

; custom text-pane
       


; config


(defn read-stored-config []
  (let [dir (path-to-file (. System getProperty "user.home"))
        file (new java.io.File dir ".ecosystem.config.clj")]   
    (if (not (.exists file))
      {}
      (load-file (.getAbsolutePath file)) )))

(defn get-merged-config [default-config cfg-1 cfg-2]
  (merge default-config (read-stored-config) cfg-1 cfg-2) )

(defn save-config [app]
  (let [dir (path-to-file (. System getProperty "user.home"))
        file (new java.io.File dir ".ecosystem.config.clj")]

    (write-file 
      (pretty-print (merge {} (sort (assoc (select-keys app (app :keys-to-save))
           :startup (cons 'quote (list (app :startup))) ))))
      file )))




(def show-ecosystem-window)




(defn run-observers [prev next observers]
  (if (or (nil? observers) (empty? observers))
    next
    (do
      (let [temp ((first observers) prev next)
          new-next (if temp temp next)]
         (recur prev new-next (rest observers)) ))))

(defn run-observers-till-fixpoint [prev next]
  (let [observers (next :observers)
        new-next (run-observers prev next observers)]
    (if (= new-next next)
      new-next
      (recur next new-next) )))

; main function
(defn show-ecosystem-window [cfg] (let [
  state (new java.util.HashMap)
  put-mutable (fn [key value] (.put state key value) value)
  get-mutable (fn [key] (.get state key))
  frame (new JFrame "Waterfront")
  
  output-window (javax.swing.JPanel.)
               
        dispatch (fn dispatch
          ([action]
          (dispatch action "???"))
          
          ([action name]
          (dispatch action name (get-mutable :ecosystem)))
 
          ([action name old-app]
          (put-mutable :entracne (inc (get-mutable :entracne)))          
          (when name
            (println (get-mutable :entracne) "dispatching " name))
            
            (let [candidate-new-app 
                  (try 
                    (action old-app)        
                  (catch Exception e (.printStackTrace e) old-app) )
                new-app (if (nil? candidate-new-app) old-app candidate-new-app)]
                                  
            (put-mutable :ecosystem new-app)
            (when (zero? (put-mutable :entracne (dec (get-mutable :entracne))))
              (put-mutable :ecosystem (run-observers-till-fixpoint old-app new-app))            
              (let [x (get-mutable :ecosystem)]
                (when-not (empty? (x :pending))               
                  (dispatch (first (x :pending)) "???" (assoc x :pending (rest (x :pending)))) )))
            (get-mutable :ecosystem) )))
                                

        show-popup (fn [popup-menu e] 
          (when (.isPopupTrigger e)       
            (. popup-menu show (.getComponent e) (.getX e) (.getY e)) ))

        build-context-menu-listener (fn [popup-menu] 
          (proxy [java.awt.event.MouseAdapter] []
            (mousePressed [e] (show-popup popup-menu e))
            (mouseReleased [e] (show-popup popup-menu e)) ))                              
       
        default-config { 
          :x0 100
          :y0 50
          :width0 800
          :height0 1000
          :font-size 20
          :font-name "Courier New"
          :font-style Font/PLAIN
          :startup '(fn [app] app)
          :keys-to-save [:keys-to-save :file-name :last-search :font-size :font-name :font-style]
          :file-name :unknown }
          
        overriding-config {
          :dispatch dispatch
          :enqueue (fn [app f] (assoc app :pending (concat (app :pending) (list f))))
          :eval-count 1,
          :frame frame, 
          :menu [
            { :name "File" :mnemonic KeyEvent/VK_F :children []}
            { :name "Edit" :mnemonic KeyEvent/VK_E :children []}
            { :name "View" :mnemonic KeyEvent/VK_V :children []}
            { :name "Source" :mnemonic KeyEvent/VK_S :children []}
            { :name "Run" :mnemonic KeyEvent/VK_R :children []} ]
          :observers []
          :actions {} }]

    (put-mutable :ecosystem (get-merged-config default-config cfg overriding-config))
    (put-mutable :entracne 0)
    (put-mutable :number-of-children 0)
            
    (let [app (get-mutable :ecosystem)]
      (doto frame
        (.setDefaultCloseOperation (. JFrame DO_NOTHING_ON_CLOSE))
        (.setLayout (new BorderLayout))
        (.pack)
        (.setSize (app :width0) (app :height0))
        (.setLocation (app :x0) (app :y0))
        (.setVisible true))
      (dispatch (fn[x] (apply (eval (app :startup)) (list (get-mutable :ecosystem)))) "bootstrap" {} )
         
   )))


(def run-func (fn []
  (println "tranform=" transform)
  (try 
    (. UIManager (setLookAndFeel (. UIManager getSystemLookAndFeelClassName)))
    (show-ecosystem-window { :title-prefix ""})
    (catch Throwable t (.printStackTrace t))) ))
