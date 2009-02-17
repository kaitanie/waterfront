(ns net.sourceforge.waterfront.kit)


(import 
  '(javax.swing JFrame JLabel JScrollPane JTextField JButton JTextArea UIManager JMenuItem JMenu JMenuBar JCheckBoxMenuItem)
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

(defn inspect-aux [s v]
  (println (str s "=" (if v v "nil")))
  v)

(defmacro inspect [a]
  `(inspect-aux '~a ~a))

; general purpose utilities

(defn defaults-to [a b]
  (if a
    a
    b))


(defn get-num-occurences
  { :test (fn []
      (assert-eq 0 (get-num-occurences \a [\b]))
      (assert-eq 1 (get-num-occurences \a [\a]))
      (assert-eq 1 (get-num-occurences \a [\a \b]))
      (assert-eq 1 (get-num-occurences \b [\a \b]))
      (assert-eq 1 (get-num-occurences \b [\a \a \b]))
      (assert-eq 2 (get-num-occurences \a [\a \a \b]))
      (assert-eq 0 (get-num-occurences \a []))
      (assert-eq 0 (get-num-occurences \a nil)) )}
  ([element coll]
  (get-num-occurences element coll 0))
  
  ([element coll result]
  (cond
    (empty? coll)
    result

    (= (first coll) element)
    (recur element (rest coll) (inc result))

    :else
    (recur element (rest coll) result) )))

(test (var get-num-occurences))


(defn get-duplicates
  { :test (fn []
      (assert-eq [\b \a] (get-duplicates [\c \b \a \a \b]))
      (assert-eq [\a \b] (get-duplicates [\c \a \b \a \b]))
      (assert-eq [\a \b] (get-duplicates [\a \b \c \a \b]))
      (assert-eq [\b] (get-duplicates [\a \b \b]))
      (assert-eq [\a] (get-duplicates [\a \b \a]))
      (assert-eq nil (get-duplicates [\a \b]))
      (assert-eq nil (get-duplicates [\a]))
      (assert-eq nil (get-duplicates [])) 
)}
  [coll]
  (if (empty? coll)
    nil
    (distinct(filter (fn [x] (> (get-num-occurences x coll) 1)) coll)) ))



(test (var get-duplicates))

(defn includes [x coll]
  (if (or (nil? coll) (empty? coll))
    false 
    (if (= x (first coll))
      true
      (recur x (rest coll)))))



(defn- drop-first-aux [item coll result]
  (cond
    (empty? coll)
    (reverse result)

    (= (first coll) item)
    (concat (reverse result) (rest coll))

    :else
    (recur item (rest coll) (cons (first coll) result)) ))

(defn drop-first 
  { :test (fn []
      (assert (= nil (drop-first \x nil)))
      (assert (= '(\a) (drop-first \x [ \a ]))) 
      (assert (= nil (drop-first \x [ \x ]))) 
      (assert (= '(\a) (drop-first \x [ \a \x ]))) 
      (assert (= '(\a) (drop-first \x [ \x \a ]))) 
      (assert (= '(\a \x) (drop-first \x [ \x \a \x ]))) 
      (assert (= '(\x \a) (drop-first \x [ \x \x \a ]))) 
      (assert (= '(\a \b \c) (drop-first \x [ \a \b \x \c]))) 
      (assert (= '(\a \b \c \d) (drop-first \x [ \a \b \c \d ]))) )}
  [item coll]
  (drop-first-aux item coll nil))

(test (var drop-first))

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


(defn start-daemon [f & args]
  (doto (Thread. (runnable (apply partial (cons f args))))
    (.setDaemon true)
    (.start)))
    

; swing utlities

(defn later [f & args]
  (. javax.swing.SwingUtilities invokeLater (apply runnable (cons f args))) )


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
      (let [is-bool (not= :not-there (get desc :boolean-value :not-there))
            res (cond 
                  is-bool
                  (JCheckBoxMenuItem. (desc :name) (desc :boolean-value))

                  (desc :action) 
                  (JMenuItem. (desc :name)) 

                  :else
                  (JMenu. (desc :name)))]

        (when (desc :mnemonic)
          (.setMnemonic res (desc :mnemonic)) )

        (when (desc :key)
          (.setAccelerator res (KeyStroke/getKeyStroke (desc :key) (if (desc :mask) (desc :mask) (ActionEvent/CTRL_MASK)))) )

        (if (desc :action)
          (if is-bool
            (.addActionListener res (new-action-listener (wrapper-func (fn [app] ((desc :action) app (.getState res))))))
            (.addActionListener res (new-action-listener (wrapper-func (desc :action)))) )
          (create-menu-from-desc res wrapper-func (desc :children)) )
        (.add parent res) 
        parent )))))



; Pretty printing

   

(defn pretty-print)


(defn eol [nesting-level]
   (apply 
     str 
     (cons 
       \newline 
       (replicate 
         (* 2 nesting-level) 
         \space ))))
   
(def str-compare
  (proxy [java.util.Comparator] []
    (compare [a b] (compare (str a) (str b))) ))

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
        (sort str-compare m) ))
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
      (nil? x)
      "nil"

      (and (map? x) (empty? x))
      "{}"

      (and (vector? x) (empty? x))
      "[]"

      (and (seq? x) (empty? x))
      "()"

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


(defn- show-dlg [a b & xs]
  (let [sb (StringBuffer.)]
    (.append sb (str a ": " b))
    (doseq [x xs]
      (.append (.append sb (pretty-print x)) \newline))
    (javax.swing.JOptionPane/showMessageDialog nil (str sb)) )
    (if (empty? xs)
      nil
      (first xs)))

    

(defmacro break [a]
  `(do
      (show-dlg "before" '~a)
      (show-dlg "after " '~a ~a)))


; UI - Form


(defn- new-grid-constraints 
  ([gridx gridy fill-x?]
  (new-grid-constraints gridx gridy 1 1 fill-x? false))
  
  ([gridx gridy col-span row-span fill-x? fill-y?]
  (let [result (java.awt.GridBagConstraints. )]
    (set! (. result gridx) gridx)
    (set! (. result gridy) gridy)
    (set! (. result gridwidth) col-span)
    (set! (. result gridheight) row-span)
    (set! (. result weightx) (if fill-x? 1 0))
    (set! (. result weighty) (if fill-y? 1 0))
    (set! (. result insets) (java.awt.Insets. 3 10 3 15))
    (set! (. result anchor) java.awt.GridBagConstraints/LINE_START)
    (set! (. result fill) 
      (cond 
        (and fill-x? fill-y?)
        java.awt.GridBagConstraints/BOTH

        (and fill-x? (not fill-y?))
        java.awt.GridBagConstraints/HORIZONTAL

        (and (not fill-x?) fill-y?)
        java.awt.GridBagConstraints/VERTICAL

        :else
        java.awt.GridBagConstraints/NONE))
    result )))


(defn- add-input-area [checker p gy name default-value]
  (cond
    (coll? default-value)
    (let [widget (javax.swing.JComboBox. (into-array default-value))
          result (fn [] (.. widget (getEditor) (getEditorComponent) (getText)))
          label (javax.swing.JLabel. name)]
      (.setEditor widget (javax.swing.plaf.basic.BasicComboBoxEditor.))
      (.add p label (new-grid-constraints 0 gy false))
      (.add p widget (new-grid-constraints 1 gy true))
      (.setEditable widget true)
      (.addActionListener widget 
        (proxy [java.awt.event.ActionListener] []
          (actionPerformed [e] (checker)) ))
      (.. widget (getEditor) (getEditorComponent) (getDocument) (addDocumentListener  
        (proxy [javax.swing.event.DocumentListener] []
          (changedUpdate [e] (checker))
          (insertUpdate [e] (checker))
          (removeUpdate [e] (checker)) )))
      (.. widget (getEditor) (selectAll))
      result )

    (string? default-value)
    (let [widget (javax.swing.JTextField. default-value)
          result (fn [] (.getText widget))
          label (javax.swing.JLabel. name)]
      (.add p label (new-grid-constraints 0 gy false))
      (.add p widget (new-grid-constraints 1 gy true))
      (.addDocumentListener (.getDocument widget)
        (proxy [javax.swing.event.DocumentListener] []
          (changedUpdate [e] (checker))
          (insertUpdate [e] (checker))
          (removeUpdate [e] (checker)) ))
      (.selectAll widget)
      result )

    :else
    (let [widget (javax.swing.JCheckBox. name)
          result (fn [] (.isSelected widget))]
      (.add p widget (new-grid-constraints 1 gy true))
      (.setSelected widget (if default-value true false))
      result )))
        
(defn- new-button [title action]
  (let [result (javax.swing.JButton. title)]
    (.addActionListener result 
      (proxy [java.awt.event.ActionListener] []
        (actionPerformed [e] (action)) ))
    result ))



(defn- add-escape-handler [d escape-handler]
  (let [rootPane (.getRootPane d)
        stroke (javax.swing.KeyStroke/getKeyStroke "ESCAPE")
        actionListener (proxy [javax.swing.AbstractAction] []
          (actionPerformed [e] (escape-handler d)) )
        inputMap (.getInputMap rootPane javax.swing.JComponent/WHEN_IN_FOCUSED_WINDOW)]

    (.put inputMap stroke, "ESCAPE")
    (.. rootPane (getActionMap) (put "ESCAPE" actionListener)) )
  d )



(defn- set-mnemonics [buttons]
  (loop [bs buttons used-letters nil] 
    (when-not (empty? bs)
      (let [s (seq (.getText (first bs))) 
            letter (reduce (fn [v c] 
                              (cond      
                                v
                                v
  
                                (includes c used-letters)
                                nil
  
                                :else
                                c )) nil s)]
        (when letter
          (.setMnemonic (first bs) (char letter)))
        (recur (rest bs) (cons letter used-letters)) ))))



(defn show-input-form 
  "Display a modal dialog where the user can fill in fields (name-value pairs). Returns nil if the form's
   cancel button was clicked. Otherwise, returns a map which maps field names to their values.
   The returned map also maps :width and :height to the form's width and height (respectively, in pixels).
   The initial value of each field (as specified by the fields argument) determines the widget
   that will be used to render this field: a check box for a boolean values, a combox box for a collection,
   a text field for a string.
   owner - Owner widget. Must be a JFrame
   user-props - Various properties of the form. Supported keys (with defaults): 
                  :title - Dialog's title (\"\")
                  :ok - Text of the OK button (\"Ok\")
                  :cancel - Text of the cancel button (\"Cancel\")
                  :msg - A widget on which status messages will be shown, or a nil. (JLabel)
                  :width - Preferred width in pixel (200)
                  :height - Preferred height in pixel (150)

   heading-widget - A widget to be placed at the dialog's upper part. May be nil.
   ok-condition - a function taking a map (in the same structure as the return value) describing the 
      form's current state. Should return nil if all form values are legal, at which case, the OK 
      button is enabled. Otherwise, should returns an error message (string) which will be displayed
      on the form. This function is evaluated only if all validators (see fields, below) pass
   & fields - a sequence of maps describing the form's fields. Each map should have these associations:
      :name - Field name
      :value - Field's initial value
      :validator - a function taking a single argument representing the field's value. Should return nil if 
        the value is legal. Otherwise, should return an error message (string) which will be displayed on the form.

   Example
    (show-input-form 
        nil                   
        { :title \"Personal Details\" :cancel \"Close\" }    
        (javax.swing.JLabel. \"Fill in your first and last name\")
        (fn [model] (if (and (= (get model \"First name\") \"John\") (= (get model \"Last name\") \"Doe\")) \"This name is not allowed\" nil)) 
        { :name \"First name\" :value \"[first name here]\" :validator (fn [x] (if (zero? (count x)) \"too short\" nil)) }
        { :name \"Last name\" :value \"[last name here]\" :validator (fn [x] (if (zero? (count x)) \"too short\" nil)) } 
        { :name \"Favorite Color\" :value [ \"Red\" \"Green\" \"Blue\" ] }
        { :name \"Has cats?\" :value false } )))"
      
  [#^javax.swing.JFrame owner user-props heading-widget ok-condition & fields]
  (let [props (merge { :title "" :ok "Ok" :cancel "Cancel" :msg (javax.swing.JLabel.)} user-props)
        cancelled? (atom true)
        d (javax.swing.JDialog. owner true)
        upper-panel (javax.swing.JPanel.)
        button-panel (javax.swing.JPanel. (java.awt.FlowLayout. java.awt.FlowLayout/TRAILING))
        p (javax.swing.JPanel.)
        first-row (+ (if (props :msg) 1 0) (if heading-widget 1 0))]
    
    (.setTitle d (props :title))
    (.setLayout p (java.awt.GridBagLayout.))
    (when heading-widget
      (.add p heading-widget (new-grid-constraints 1 0 1 1 true false)) )

    (when (props :msg)
      (.add p (javax.swing.JLabel. " ") (new-grid-constraints 0 (dec first-row) 1 1 false false))
      (.add p (props :msg) (new-grid-constraints 1 (dec first-row) 1 1 true false)))

    (.add p (javax.swing.JLabel.) (new-grid-constraints 0 (+ first-row (count fields)) 2 1 true true))

    (.add d button-panel java.awt.BorderLayout/SOUTH)


    (let [model-atom (atom nil)
          ok-button (new-button (props :ok) (fn [] (swap! cancelled? (fn [x] false)) (.dispose d)))
          cancel-button (new-button (props :cancel) (fn [] (.dispose d))) 
          aux (fn [prefix s]
            (if s (str prefix s) nil))
          checker (fn []
            (let [err-msg (reduce 
                            (fn [v c] (if v 
                              v 
                              (aux (str (c :name) ": ") ((c :validator) ((c :reader))))))
                            nil
                            @model-atom)
                  fail-msg (if err-msg err-msg (ok-condition (reduce (fn [v c] (assoc v (c :name) ((c :reader)))) {} @model-atom)))]
              (when (props :msg)
                (.setText (props :msg) (if fail-msg fail-msg " ")) ) 
              (.setEnabled ok-button (not fail-msg)) ))
         
          model (doall (map 
                  (fn[x y] 
                    (let [v (defaults-to (x :validator) (fn [arg] nil))
                          rdr (add-input-area checker p y (x :name) (x :value))]
                      (assoc x :row y :reader rdr :validator v)) ) 
                  fields 
                  (iterate inc first-row)))]

      (set-mnemonics [ok-button cancel-button])
      (swap! model-atom (fn [x] model))            
      (.add button-panel cancel-button)
      (.add button-panel ok-button)
      (.add d p java.awt.BorderLayout/CENTER)

      (add-escape-handler d (fn [d] (.doClick cancel-button)))
      (.. d (getRootPane) (setDefaultButton ok-button))

      (when (or (props :width) (props :height))
        (.setPreferredSize d (java.awt.Dimension. (defaults-to (props :width) 200) (defaults-to (props :height) 150))))

      (checker)
      (.setLocationRelativeTo d owner)
      (.pack d)
      (.show d true)
      (if @cancelled?
        nil
        (reduce (fn [v c] (assoc v (c :name) ((c :reader)))) { :width (.getWidth d) :height (.getHeight d)} model) ))))


(defn main []
  (let [fr (javax.swing.JFrame.)]
    (.setSize fr 300 500)
    (.setVisible fr true)
 
    (show-input-form 
        nil                   
        { :title "Personal Details" :cancel "Close" }  
        (javax.swing.JLabel. "Fill in your personal details")
        (fn [model] (if (and (= (get model "First name") "John") (= (get model "Last name") "Doe")) "This name is not allowed" nil)) 
        { :name "Favorite Color" :value [ "Red" "Green" "Blue" ] :validator (fn [x] (if (zero? (count x)) "too short" nil)) }
        { :name "First name" :value "[first name here]" :validator (fn [x] (if (zero? (count x)) "too short" nil)) }
        { :name "Last name" :value "[last name here]" :validator (fn [x] (if (zero? (count x)) "too short" nil)) } 
        { :name "Has cats?" :value false } )))



; (net.sourceforge.waterfront.kit/main)





