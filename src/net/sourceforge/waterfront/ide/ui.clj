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

(defn read-stored-config [fallback-context]
  (let [dir (path-to-file (. System getProperty "user.home"))
        file (new java.io.File dir ".waterfront.config.clj")]
    (if (not (.exists file))
      fallback-context
      (try
        (load-file (.getAbsolutePath file)) 
        (catch Exception e fallback-context) ))))

(defn save-config [app]
  (let [dir (path-to-file (. System getProperty "user.home"))
        file (new java.io.File dir ".waterfront.config.clj")]

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
          (catch Throwable e (.printStackTrace e) old-app) )
        new-app (if (nil? candidate-new-app) old-app candidate-new-app)]
                          
    (atom-assoc a :app new-app)
    (when (zero? (atom-assoc a :entrance (dec (atom-get a :entrance))))
      (atom-assoc a :app (run-observers-till-fixpoint old-app new-app))            
      (let [x (atom-get a :app)]
        (when-not (empty? (x :pending))               
          (dispatcher a (first (x :pending)) "???" (assoc x :pending (rest (x :pending)))) )))
    (atom-get a :app) )))


(defn- wrapper [ f & args ]
  (try
    (apply f args)
    (catch Throwable t
      (.printStackTrace t))))



(defn- register-periodic-observer [disp interval-millis observer]
  (let [at (atom nil)
        listener (proxy [java.awt.event.ActionListener] []
                    (actionPerformed [e] 
                      (let [new-app (disp (fn [curr-app] 
                                            (let [temp (observer @at curr-app)]
                                              (if temp temp curr-app) )))]
                        (swap! at (fn [x] new-app)) )))
        t (javax.swing.Timer. interval-millis listener)]
    (.start t)
    nil))

(defn- new-dispatcher [initial-app]
  (let [a (atom {})
        result (partial wrapper (partial dispatcher a)) ]
    (atom-assoc a :app (assoc initial-app 
                          :dispatch result           
                          :observers []
                          :later (fn [f] (later (fn [] (result f))))
                          :register-periodic-observer (partial register-periodic-observer result) 
                          :enqueue (fn [app f] (assoc app :pending (concat (app :pending) (list f)))) ))
    (atom-assoc a :entrance 0)
    result ))
    

(defn- build-context [fallback-context a] 
  (let [default-config { 
          :x0 100
          :y0 50
          :width0 850
          :height0 800
          :font-size 20
          :font-name "Courier New"
          :font-style Font/PLAIN
          :startup '(fn [app] app)
          :keys-to-save [:keys-to-save :file-name :last-search :font-size :font-name :font-style]
          :file-name :unknown }
          
        overriding-config {
          :menu [
            { :name "File" :mnemonic KeyEvent/VK_F :children []}
            { :name "Edit" :mnemonic KeyEvent/VK_E :children []}
            { :name "Source" :mnemonic KeyEvent/VK_S :children []}
            { :name "Run" :mnemonic KeyEvent/VK_R :children []} 
            { :name "View" :mnemonic KeyEvent/VK_V :children []}]
          :actions {}
          :eval-count 1 }
        result (merge default-config (read-stored-config fallback-context) a overriding-config) ]
      result ))

; main function
(defn new-waterfront-window [fallback-context initial-app-context]
  (let [frame (new JFrame "Waterfront")
        dispatch (new-dispatcher (assoc (build-context fallback-context initial-app-context) :frame frame))
        app (dispatch identity)]

    (swap! (app :window-counter) inc)  
    (doto frame
      (.setDefaultCloseOperation JFrame/DO_NOTHING_ON_CLOSE)
      (.setLayout (new BorderLayout))
      (.pack)
      (.setSize (app :width0) (app :height0))
      (.setLocation (app :x0) (app :y0))
      (.setVisible true))
    (dispatch (fn[x] (apply (eval (app :startup)) (list app))) "bootstrap" {}) ))
  

(defn launch-waterfront [fallback-context]
    (later (fn []
      (try 
        (. UIManager (setLookAndFeel (. UIManager getSystemLookAndFeelClassName)))
        (new-waterfront-window fallback-context { :window-counter (atom 0) :title-prefix ""})
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
; bug: (count (replicate 100 \a))
; multi-tab editor
; reformat source
; jump to decl.
; allow show doc (F1) on symbols which are not from the global namespace
; make load document an observer-driven action triggered by a new :file-name value
; rename 
; find unused variables
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
; change the font of the compilation result (upper status bar)
; Make online-syntax-check use a background thread (agent)
; Launch the eval on a different class-loader
; Bug: (def f 5). Then select only (def f 5) and do "run" (alt-w). then delete it
; Support separators in context menu
; Reimplement context menu: 
;     integrate with main-menu. 
;     Let main-menu use actions. 
;     When building main menu concatenate a list of actions. 
;     Then filter out non-context actions and put them in a context menu.
; Use a line-by-line syntax coloring ?!
; bug: focus jump to other window when searching, with multiple Waterfront windows
; smart proxy generation (generate names, signatures of methods) + check correct spelling of super-types
; Find: cyclic, lower-case
; Replace
; menu items should be disabled (undo, redo) when action is not applicable
; surround with try catch
; gen. overloading: ask arities, generate forwarding
; proxy: gen method names
; input-form: ESCAPE => Cancel, Return => OK
; Show a "search phrase not found" message
; run.clj depends on get-selected-text as a library function from another plugin. refactor into kit.clj

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
; ??-Feb-09: File -> Revert
; 03-Feb-09: Green/Red indicator shows evaluation status of the code - updated on the fly
; 04-Feb-09: Make syntax problems messages more descriptive
; 04-Feb-09: Status bar
; 05-Feb-09: TAB indents a selection
; 05-Feb-09: Improve next/prev heuristic in the presence of parenthesis/braces/brackets
; 05-Feb-09: Threads are now daemons
; 06-Feb-09: Shutdown the JVM when last window is closed
; 06-Feb-09: Source -> Generate: Proxy, Overloading, Try-Catch
; 06-Feb-09: A default .waterfront.config.clj file is generated if does not exist
; 07-Feb-09: Context menu
; 08-Feb-09: Auto-complete
; 08-Feb-09: Jump to errorneus line 
; 10-Feb-09: Bug fix: list of recently opened files in a new window
; 10-Feb-09: Bug fix: Updating of the Line-Col indicator in response to searching/jumping to an error
; 10-Feb-09: Asks whether to Save a dirty file before openning a file
; 12-Feb-09: New search options: cyclic, case sensitive

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






