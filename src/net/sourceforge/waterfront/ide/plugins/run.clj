

(def *app* {})

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



(defn read-objects-from-file [f first-line-number]
  (let [stream (new clojure.lang.LineNumberingPushbackReader (new java.io.FileReader f))]
    (try
      (. clojure.lang.Var pushThreadBindings { 
          clojure.lang.Compiler/SOURCE f, 
          clojure.lang.Compiler/SOURCE_PATH f,
          clojure.lang.Compiler/LINE_BEFORE (.getLineNumber stream),
          clojure.lang.Compiler/LINE_AFTER (.getLineNumber stream) })
      (loop [lb (.getLineNumber stream)
             result ()]
        (let [o (read stream false nil)
              la (.getLineNumber stream)]
          (.set clojure.lang.Compiler/LINE_BEFORE la)
          (if (nil? o)
            (reverse result)
            (recur la (cons (with-meta o { :line (+ la first-line-number) }) result)) )))
      (finally (. clojure.lang.Var popThreadBindings)) )))



(defn- synthesize-exception [app temp-file-name ln e]
  (let [index (.indexOf (.getMessage e) (str "(" temp-file-name))
        msg (if (neg? index) (.getMessage e) (.substring (.getMessage e) 0 index))
        re (RuntimeException. (str msg "(sourcefile:" ln ")"))]
      (.setStackTrace re (.getStackTrace e))
      re ))
      

(defn eval-objects [app objects temp-file-name]  
  (binding [*app* (assoc app :program objects) ]
    (doseq [obj objects]
      (try
        (println (eval obj))
        (catch Exception e
          (throw (synthesize-exception app temp-file-name ((meta obj) :line) (clojure.main/repl-exception e))) )))))

(defn run-program 
  "run a program. return a two element list: first element is the output, second element is the exception
    that stopped the program (nil if completed normally)"
  ([app text]
  (run-program app text 0))

  ([app text first-line]    
  (let [src-file (get-temp-file) 
        stream (new java.io.StringWriter)
        print-writer (new java.io.PrintWriter stream true)]
    (binding [
              *err* print-writer 
              *out* print-writer]
      (write-file text src-file)
      (try 
        (. clojure.lang.Var pushThreadBindings 
          { clojure.lang.Compiler/SOURCE (.getName src-file),
            clojure.lang.Compiler/SOURCE_PATH (.getAbsolutePath src-file),
            clojure.lang.RT/CURRENT_NS (.get clojure.lang.RT/CURRENT_NS) })
        (eval-objects app (read-objects-from-file (.getAbsolutePath src-file) first-line) (.getName src-file)) 
        [(str stream) nil]
        (catch Exception e         
          (.printStackTrace e print-writer)
          [(str stream) e] )
        (finally (.delete src-file) (. clojure.lang.Var popThreadBindings))  )))))


(defn- add-output [output app]
  (if output
    (assoc app :output-text output)
    app ))


(defn- abs-val [x]
  (if (pos? x)
    x
    (- x)))


(defn- eval-menu-observer [old-app new-app]
  (if (maps-differ-on old-app new-app :caret-dot :caret-mark)
    (let [len (abs-val (- (new-app :caret-dot) (new-app :caret-mark)))
          item-name (if (zero? len) "Eval File" "Eval Selection")]
      (assoc new-app :menu (menu-assoc (new-app :menu) ["Run" :eval] :name item-name)) ))) 


(defn- eval-file-or-selection [a change-func app] 
  (.setText (app :output-label) (str "Evaluation #" (app :eval-count)))
  (let [t0 (. System currentTimeMillis) 
        sel-text (get-selected-text app (.getText (app :area)))
        output (first (run-program (assoc app :change change-func) sel-text))]
    (add-output output (assoc (merge app @a)
      :output-title (str "Evaluation #" (app :eval-count) " - Completed in " (- (. System currentTimeMillis) t0) "ms") 
      :output output 
      :eval-count (inc (app :eval-count) )))))


(fn [app] 
  (let [a (atom {})
        change-func (fn[key val] (swap! a (fn [curr-app] (assoc curr-app key val))))]
    (add-to-menu (load-plugin (add-observers app eval-menu-observer) "menu-observer.clj" "check-syntax.clj") "Run" 
      { :id :eval :name "Eval File" :key KeyEvent/VK_E :mnemonic KeyEvent/VK_E :on-context-menu true 
        :action (partial eval-file-or-selection a change-func) })))












