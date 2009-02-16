

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



(defn read-objects-from-file [f]
  (let [stream (new clojure.lang.LineNumberingPushbackReader (new java.io.FileReader f))]
    (try
      (. clojure.lang.Var pushThreadBindings { 
          clojure.lang.Compiler/SOURCE f, 
          clojure.lang.Compiler/SOURCE_PATH f,
          clojure.lang.Compiler/LINE_BEFORE (.getLineNumber stream),
          clojure.lang.Compiler/LINE_AFTER (.getLineNumber stream) })
      (loop [result ()]
        (let [lb (.getLineNumber stream) 
              o (read stream false nil)
              la (.getLineNumber stream)]
          (.set clojure.lang.Compiler/LINE_BEFORE la)
          (if (nil? o)
            (reverse result)
            (recur (cons (with-meta o { :line lb }) result)) )))
      (finally (. clojure.lang.Var popThreadBindings)) )))



        (def eval-objects (fn [app objects]  
          (binding [*app* (assoc app :program objects) ]
            (doseq [i objects]
              (println (eval i)) ))))

        (def run-program (fn [app text]   
          "run a program. return a two element list: first element is the output, second element is the exception
           that stopped the program (nil if completed normally)"
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
                (eval-objects app (read-objects-from-file (.getAbsolutePath src-file))) 
                (str stream)
                (catch Exception e 
                  (println e) 
                  (.printStackTrace e print-writer)
                  (str stream) )
                (finally (.delete src-file) (. clojure.lang.Var popThreadBindings))  )))))


(defn- add-output [output app]
  (if output
    (assoc app :output-text output)
    app ))

(fn [app] 
  (let [a (atom {})
        change-func (fn[key val] (swap! a (fn [curr-app] (assoc curr-app key val))))]
    (add-to-menu (load-plugin app "menu-observer.clj" "check-syntax.clj") "Run" 
      { :name "Eval" :key KeyEvent/VK_E  :on-context-menu true :action (fn m-run [app] 
                          (.setText (app :output-label) (str "Evaluation #" (app :eval-count)))
                          (let [t0 (. System currentTimeMillis) 
                                sel-text (get-selected-text app (.getText (app :area)))
                                output (run-program (assoc app :change change-func) sel-text)]
                            (add-output output (assoc (merge app @a)
                              :output-title (str "Evaluation #" (app :eval-count) " - Completed in " (- (. System currentTimeMillis) t0) "ms") 
                              :output output 
                              :eval-count (inc (app :eval-count) ))))) })))














