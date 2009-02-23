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
        temp (merge default-config (read-stored-config fallback-context) a overriding-config) 
        result temp]
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
  

(defn- new-log-file []
  (let [t (java.io.File/createTempFile "wfr" ".log")]
    (.deleteOnExit t)
    (java.io.PrintWriter. (java.io.FileWriter. t) true) ))

(defn launch-waterfront [argv fallback-context]
    (later (fn []
      (try 

        (try (. UIManager (setLookAndFeel (. UIManager getSystemLookAndFeelClassName)))
          (catch Exception e nil) ) ;Intentionally ignore look & feel failure

        (let [file-to-load 
                (if (empty? argv)
                      nil
                      (let [f (.getAbsoluteFile (java.io.File. (first argv)))]
                        (if (.exists f)
                          f
                          nil)))
              m (if file-to-load 
                  { :file-name-to-load (.getAbsolutePath file-to-load) }
                  {} )]
          (new-waterfront-window fallback-context (merge { :window-counter (atom 0) :log (new-log-file) :title-prefix "" } m)))
        (catch Throwable t (.printStackTrace t)) ))))

; different thread for execution
; stop running button
; ns, line numbers in error messages
; syntax coloring
; highlight current line
; scrapbook
; syntax checking also on selected text
; fix line numbers when running/syntax checking selected text
; View menu: show Output window, show Prolbems window
; jump to matching paren
; incremental search
; eval just like in REPL
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
; run.clj depends on get-selected-text as a library function from another plugin. refactor into kit.clj

; Add "run expression" history to the run menu
; New proxy wizard: choose which ctor (of super-class) to call. Choose which methods you want to override
; Highlight full width of current line
; New window (File->New) should inherit the divider location
; surround with try catch
; gen. overloading: ask arities, generate forwarding

; show only first exc.
; show stack trace of exc.
; quick relaunch of new windows (remembber init. funcs. of plugins)
; relfect - show java structure.
; File changed, reload?
; Check what happens if the file is read-only
; read-only indication
; Remove the red/green indicator

; +(29) Reflect
; +(28) Tooltip on red markers
; +(27) Eval-as-you-type status is now persistent
; +(26) Indicator color is green when starting with eval-as-you-type disabled
; +(24) Bad column/line when opening a new window
; +(22) Indicator should be red when there are syntax errors
; +(21) Check syntax as part of eval as you type
; +(10) Red marker on the errorneus line (line-number pane)
; + (9) Red markers on syntax errors
; +(20) Make status bar a little bit taller than its contents
; +(18) Line:col indicator should not jump
; +(13) uncomment does not trigger on line evaluation
; +(19) Eliminate auto-completion if too many completions
; +(17) Make sure the chooser adds *.clj
; +(15) File chooser should show *.clj files by default
; + (6) undo after replace-all erases the document and the pastes
; + (5) undo after replace erases and then pastes
; +(14) Write to log
; +(16) Command line args
; + (8) Allow the user to disable on-line evaluation
; + (7) menu items should be disabled (undo, redo) when action is not applicable
; + (4) proxy: gen method names
; + (3) click on error -> jump to location
; + (2) make undo less agressive
; + (1) Mnemonics on buttons of forms
; ? (0) Font issue
; <-- Most important




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
; 12-Feb-09: Input-form: ESCAPE => Cancel, Return => OK
; 13-Feb-09: Search box shows a combo-box with search history
; 14-Feb-09: Replace
; 14-Feb-09: Show a "phrase not found" message
; 14-Feb-09: Opens a "Discard changes?" dialog when reverting a dirty file
; 15-Feb-09: Smart proxy generation (generates methods signatures based on user-supplied super types)
; 16-Feb-09: A form dialog (e.g.: Find dialog) is now placed relative its owner
; 16-Feb-09: Uses setParagraphAttributes for setting the font of the editor pane
; 16-Feb-09: Syntax error (problem window) are now double-clickable: jumps to corresponding line
; 16-Feb-09: File chooser uses *.clj by default
; 16-Feb-09: Loads a file from the command line (if specified)
; 16-Feb-09: Diagnostic messages are written to (app :log)
; 16-Feb-09: Eval as you type can be disabled (Run -> Eval as you type)
; 17-Feb-09: Disabling of menu items (undo, redo, increase/decrease font)
; 17-Feb-09: Eval menu item is now either "Eval File" or "Eval Selection"
; 17-Feb-09: Improved the undo behavior of replace, replace-all
; 19-Feb-09: In save-as a *.clj extension is added if none specified
; 19-Feb-09: Auto-completion only shows the first N entries
; 19-Feb-09: Periodic text observer added => The greed/red indicator is repsonsive to uncomment, undo, replace, etc.
; 19-Feb-09: Improved looks of the lower status bar
; 22-Feb-09: Tooltip on red markers
; 23-Feb-09: Reflect java classes 
 

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



