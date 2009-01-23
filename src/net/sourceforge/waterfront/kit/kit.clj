(ns net.sourceforge.waterfront.kit)


(import 
  '(javax.swing JFrame JLabel JScrollPane JTextField JButton JTextArea UIManager JMenuItem JMenu JMenuBar)
  '(javax.swing JPopupMenu KeyStroke JSplitPane JOptionPane)
  '(javax.swing.event CaretEvent CaretListener)
  '(javax.swing.text DefaultStyledDocument StyleConstants StyleConstants$CharacterConstants SimpleAttributeSet)
  '(java.awt Color)
  '(java.awt.event ActionListener KeyEvent ActionEvent)
  '(java.awt GridLayout BorderLayout Font EventQueue)
  '(java.io File))


(defn assert-eq-aux [expected actual actual-as-text]
  (if (= expected actual)
    :ok
    (throw (new AssertionError (str "Assert failed.\nExpected: " expected "\n     Got: " actual "\n" actual-as-text)))))

(defmacro assert-eq
  "Evaluates expr and throws an exception if it does not evaluate to
 logical true."
  [expected, actual]
  `(assert-eq-aux ~expected ~actual (pr-str '~actual)))




; general purpose utilities

(defn defaults-to [a b]
  (if (nil? a)
    b
    a))

(defn includes [x coll]
  (if (or (nil? coll) (empty? coll))
    false 
    (if (= x (first coll))
      true
      (recur x (rest coll)))))


(defn remove-all
  { :test (fn []
    (assert-eq [] (remove-all [] [] [])) 
    (assert-eq [] (remove-all [] '(a) [])) 
    (assert-eq [] (remove-all ['a] '(a) [])) 
    (assert-eq [1] (remove-all [1] () [])) 
    (assert-eq [2] (remove-all [1 2] [1] [])) 
    (assert-eq [2] (remove-all [1 2 1] [1] [])) 
    (assert-eq [1 1] (remove-all [1 2 1] [2] [])) 
    (assert-eq [3 4 6 1] (remove-all [2 3 2 4 6 1] [2] [])) 
    (assert-eq [3 4 6 1] (remove-all [2 3 2 4 6 1] [2 5] [])) 
    (assert-eq [2 2] (remove-all [2 3 2 4 6 1] [3 4 1 6] [])) 
    (assert-eq [] (remove-all [2 3 2 4 6 1] [3 4 2 1 6] [])) 
    (assert-eq ["cd"] (remove-all ["ab" "cd"] ["ab"] [])) 
    )}
  [coll elements-to-remove res]
  (cond
    (or (nil? coll) (empty? coll))
    res

    (includes (first coll) elements-to-remove)
    (recur (rest coll) elements-to-remove res)

    :else
    (recur (rest coll) elements-to-remove (conj res (first coll))) ))

(test (var remove-all))


(defn not-nil? [x] (not (nil? x)))

(defn transform 
  { :test (fn []
    (assert (= { :a 1 :b 200 } (transform { :a 1 :b 2 } :b 1 (fn[x] (* 100 x)))))
    (assert (= { :a 1 :b 3 } (transform { :a 1 } :b 3 (fn[x] (* 100 x))))) )}
  [m k default-value f]
  (let [v (get m k)]
    (if v  
      (assoc m k (f v))
      (assoc m k default-value) )))


(test (var transform))


(defn maps-differ-on 
  { :test (fn [] 
      (assert (maps-differ-on { :a 1 :b 2 } { :a 1 :b "two" } :b))
      (assert (not (maps-differ-on { :a 1 :b 2 } { :a 1 :b "two" } :a)))
      (assert (maps-differ-on { :a 1 :b 2 } { :a 1 :b "two" } :b :a))
      (assert (maps-differ-on { :a 1 :b 2 } { :a 1 :b "two" } :a :b))
      (assert (maps-differ-on { :a 1 :b 2 } { :a 1 :b "two" } :c :d :b))
      (assert (not (maps-differ-on { :a 1 :b 2 } { :a 1 :b "two" } :a :c :x)))
      (assert (maps-differ-on { :a 1 :b 2 } { :a 1  } :b))
      (assert (not (maps-differ-on { :a 1 :b 2 } { :a 1 :b "two" }))) )}
  [m1 m2 & keys]
    (not= (select-keys m1 keys) (select-keys m2 keys)))

(test (var maps-differ-on))


    
(defn get-temp-file []
  (doto 
    (. java.io.File createTempFile "clj" ".tmp") 
    (.deleteOnExit)))

(defn path-to-file [path] 
  (. (if (string? path)
       (new java.io.File path)
       path) 
    getAbsoluteFile))
   
(defn write-file [text file]            
  (doto (new java.io.PrintWriter (new java.io.FileWriter (path-to-file file)))
    (.println text)
    (.close))
  (if (string? file)
    file
    (.getAbsolutePath file)))


(defn runnable [f & args]
  (proxy [Runnable] []
    (run [] 
      (try
        (apply f args)
        (catch Throwable t (.printStackTrace t)) ))))

; swing utlities

(defn later [f]
  (. javax.swing.SwingUtilities invokeLater (runnable f)) )


(defn graphics-wrapper [paint-handler]
  (fn [g]
    (let [local-g (.create g)]
      (try 
        (paint-handler local-g)
        (finally (.dispose local-g)) ))))

(defn new-custom-panel [f]
  (proxy [javax.swing.JPanel] []
    (paint [g]
      (proxy-super paint g)
      ((graphics-wrapper f) g) )))        

(defn new-custom-text-pane [f]
  (proxy [net.sourceforge.waterfront.ide.services.HighlightingTextPane] []
    (paint [g]
      (proxy-super paint g)
      (f g) )))
    

(defn new-action-listener [f]
  (proxy [ActionListener] []
    (actionPerformed [e] (f e)) ))

;;;; menu DSL

(defn change-menu [menu-name items-func menu-bar]
  (vec (map (fn[menu]
    (if (not= (menu :name) menu-name)
      menu
      (transform menu :children [] items-func)))
    menu-bar)))
    
(defn create-menu-from-desc 

  ([wrapper-func desc]
  (create-menu-from-desc (JMenuBar.) wrapper-func desc) )

  ([parent wrapper-func desc]
  (cond

    (or (nil? desc) (empty? desc))
    (do
      (when (pos? (.getItemCount parent))
        (.addSeparator parent) )
      parent)

    (vector? desc)
    (do 
      (doseq [curr desc]
        (create-menu-from-desc parent wrapper-func curr)) 
      parent)

    :else
    (do
      (let [res (if (desc :action) (JMenuItem. (desc :name)) (JMenu. (desc :name)))]
        (when (desc :mnemonic)
          (.setMnemonic res (desc :mnemonic)) )
        (when (desc :key)
          (.setAccelerator res (KeyStroke/getKeyStroke (desc :key) (if (desc :mask) (desc :mask) (ActionEvent/CTRL_MASK)))) )

        (if (desc :action)
          (.addActionListener res (new-action-listener (wrapper-func (desc :action))))
          (create-menu-from-desc res wrapper-func (desc :children)) )
        (.add parent res) 
        parent )))))



; inspection

   

(defn pretty-print)


(defn eol [nesting-level]
   (apply 
     str 
     (cons 
       \newline 
       (replicate 
         (* 2 nesting-level) 
         \space ))))
   
(defn pretty-print-map 
  [m indent]  
  (str "{ "
    (apply str 
      (reduce 
        (fn [res e] 
          (conj 
            res 
            (str 
              (eol (inc indent))
              (pretty-print (first e) (inc indent)) 
              " " 
              (pretty-print (second e) (inc indent)) 
              )))
        [] 
        m ))
    (eol indent) "}" ))

(defn pretty-print-vec 
  [m indent]  
  (str "[ " 
     (apply str 
      (reduce 
        (fn [res e] (conj res (str (eol (inc indent)) (pretty-print e (inc indent)) )))
        [] 
        m ))
    (eol indent) "]"))

(defn pretty-print-seq
  [m indent]  
  (str "( " 
     (apply str 
      (reduce 
        (fn [res e] (conj res (str (eol (inc indent)) (pretty-print e (inc indent)) )))
        [] 
        m ))
    (eol indent) ")"))
     
(defn pretty-print 
   ([x]
   (pretty-print x 0))
   
   ([x indent] 
   (cond 
      (map? x)
      (pretty-print-map x indent)
      
      (vector? x)
      (pretty-print-vec x indent)
      
      (seq? x)
      (pretty-print-seq x indent)
      
      :else
      (pr-str x))))


(defn pass [msg x]
   (println msg (pretty-print x))
   x)


