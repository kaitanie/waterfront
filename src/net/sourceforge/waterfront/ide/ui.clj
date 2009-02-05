
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



(defn- atom-assoc [a key value]
  (swap! a (fn [m] (assoc m key value)))
   value)

(defn- atom-get [a key]
  (get @a key) )


(defn- dispatcher
  ([a action]
  (dispatcher a action "???"))
  
  ([a action name]
  (dispatcher a action name (atom-get a :app)))
  
  ([a action name old-app]
    (atom-assoc a :entrance (inc (atom-get a :entrance)))
    
    (let [candidate-new-app 
          (try 
            (action old-app)        
          (catch Exception e (.printStackTrace e) old-app) )
        new-app (if (nil? candidate-new-app) old-app candidate-new-app)]
                          
    (atom-assoc a :app new-app)
    (when (zero? (atom-assoc a :entrance (dec (atom-get a :entrance))))
      (atom-assoc a :app (run-observers-till-fixpoint old-app new-app))            
      (let [x (atom-get a :app)]
        (when-not (empty? (x :pending))               
          (dispatcher a (first (x :pending)) "???" (assoc x :pending (rest (x :pending)))) )))
    (atom-get a :app) )))

(defn- new-dispatcher [initial-app]
  (let [a (atom {})
        result (partial dispatcher a) ]
    (atom-assoc a :app (assoc initial-app :dispatch result))
    (atom-assoc a :entrance 0)
    result ))
    
; main function
(defn new-waterfront-window [cfg] 
  (let [
    frame (new JFrame "Waterfront")
    
    output-window (javax.swing.JPanel.)                                              
  
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
      :enqueue (fn [app f] (assoc app :pending (concat (app :pending) (list f))))
      :eval-count 1,
      :frame frame, 
      :menu [
        { :name "File" :mnemonic KeyEvent/VK_F :children []}
        { :name "Edit" :mnemonic KeyEvent/VK_E :children []}
        { :name "Source" :mnemonic KeyEvent/VK_S :children []}
        { :name "Run" :mnemonic KeyEvent/VK_R :children []} 
        { :name "View" :mnemonic KeyEvent/VK_V :children []}]
      :observers []
      :actions {} }
      
    dispatch (new-dispatcher (get-merged-config default-config cfg overriding-config))
    app (dispatch identity)]
  
    (doto frame
      (.setDefaultCloseOperation (. JFrame DO_NOTHING_ON_CLOSE))
      (.setLayout (new BorderLayout))
      (.pack)
      (.setSize (app :width0) (app :height0))
      (.setLocation (app :x0) (app :y0))
      (.setVisible true))
    (dispatch (fn[x] (apply (eval (app :startup)) (list app))) "bootstrap" {}) ))


(defn launch-waterfront []
  (later (fn []
    (try 
      (. UIManager (setLookAndFeel (. UIManager getSystemLookAndFeelClassName)))
      (new-waterfront-window { :title-prefix ""})
      (catch Throwable t (.printStackTrace t)) ))))




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
; Help -> Status shows current plugins
; Help -> Env show environment
; Scrapbook file
; remember position in each file
; document app functions
; Add a "Run tests" option to make on-the-fly checking run tests of functions
; make deafult .config.clj file loadable from the class-path
; change the font of the compilation result (upper status bar)

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
; 03-Feb-09: Green/Red indicator shows evaluation status of the code - updated on the fly
; 04-Feb-09: Make syntax problems messages are more descriptive
; 04-Feb-09: Status bar
; 05-Feb-09: TAB indents a selection
; 05-Feb-09: Improve next/prev heuristic in the presence of parenthesis/braces/brackets
; 05-Feb-09: Threads are now daemons

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












