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


(defn- paint-line-numbers [tp lnp g startline endline starting_y fontHeight fontDesc fontAscent markers]
  (.setFont g (.getFont tp))
  (loop [line startline y starting_y]
    (when (<= line endline)
        (when (includes line markers)
          (.setColor g java.awt.Color/RED)
          (.fillRect g 0 (- y fontAscent) (.getWidth lnp) fontHeight) )
        (.setColor g java.awt.Color/BLACK)
      (.drawString g (str line) 0 y)
      (recur (inc line) (+ y fontHeight)) )))


(defn- paint-line-numbers-panel [sp tp lnp g dispatch] 
  (if (or (<= (.getWidth tp) 0) (<= (.getHeight tp) 0) (nil? (.modelToView tp 0)))
    nil
    (let [start (.viewToModel tp (.. sp (getViewport) (getViewPosition)))   
          xe (+ (.. sp (getViewport) (getViewPosition) x) (.getWidth tp))
          ye (+ (.. sp (getViewport) (getViewPosition) y) (.getHeight tp))
          end (.viewToModel tp (java.awt.Point. xe ye))
          doc (.getDocument tp)
          startline (inc (.. doc (getDefaultRootElement) (getElementIndex start)))
          endline (inc (.. doc (getDefaultRootElement) (getElementIndex end)))
          fontHeight (.. g (getFontMetrics (.getFont tp)) (getHeight))
          fontDesc (.. g (getFontMetrics (.getFont tp)) (getDescent))
          fontAscent (.. g (getFontMetrics (.getFont tp)) (getAscent))
          ignore-this-one (.modelToView tp start)
          ignore-this-two (.toString ignore-this-one)
          starting_y (+ fontHeight 
              (- (.. 
                tp 
                (modelToView start) 
                 y) 
              (.. sp (getViewport) (getViewPosition) y) 
             fontDesc) )]
      (dispatch (fn[app]
        (paint-line-numbers tp lnp g startline endline starting_y fontHeight fontDesc fontAscent (sort (app :markers)))))
      g )))
    
(defn create-line-numbers-components [app]
  (let [parts (atom nil)
        paint-numbers-wrapped (fn [g]
          (let [new-g (.create g)]
            (try
              (paint-line-numbers-panel (@parts :scroll-pane) (@parts :text-pane) 
                (@parts :line-number-panel) new-g (app :dispatch))
              (finally (.dispose new-g) ))))
        lnp (doto (new-custom-panel paint-numbers-wrapped)
              (.setMinimumSize (java.awt.Dimension. 40 30))
              (.setPreferredSize (java.awt.Dimension. 40 30)) )

        scroll-bar-ui (javax.swing.plaf.basic.BasicScrollBarUI.)
        tp (new-custom-text-pane (fn [g] (.repaint lnp)) )
        sp (javax.swing.JScrollPane. tp)
        composite (javax.swing.JPanel.)]

    (swap! parts (fn [x] { :text-pane tp :scroll-pane sp :line-number-panel lnp }))
    (doto composite
      (.setLayout (java.awt.BorderLayout.))
      (.add (@parts :scroll-pane) (java.awt.BorderLayout/CENTER))
      (.add (@parts :line-number-panel) (java.awt.BorderLayout/WEST)))
      
    (assoc @parts :composite composite) )) 
    
(defn layout-observer [old-app new-app]
  new-app )
  

(fn [app] 
  (let [sb (javax.swing.JPanel.)
        lower-sb (javax.swing.JPanel.)
        lower-win (javax.swing.JTabbedPane.)
        lnp-widgets (create-line-numbers-components app)
        area (lnp-widgets :text-pane)
        indicator (javax.swing.JLabel. "    ")
        output-label (javax.swing.JLabel. "")]

  (doto indicator 
    (.setOpaque true)
    (.setBorder (javax.swing.BorderFactory/createLoweredBevelBorder)))

  (.setLayout sb (java.awt.FlowLayout. java.awt.FlowLayout/LEFT 5 3))
  (.add sb indicator)
  (.add sb output-label)

  (.setLayout lower-sb (javax.swing.BoxLayout. lower-sb javax.swing.BoxLayout/LINE_AXIS))

  (.add (app :frame) lower-sb BorderLayout/SOUTH)
  
  (.add (app :frame) (doto (new JSplitPane (. JSplitPane VERTICAL_SPLIT) 
                                  (lnp-widgets :composite)
                                  (doto (javax.swing.JPanel.)
                                    (.setLayout (BorderLayout.))
                                    (.add sb BorderLayout/NORTH)
                                    (.add lower-win BorderLayout/CENTER)))
            (.setDividerLocation -1)
            (.setResizeWeight 1.0) )
          (. BorderLayout CENTER) )

  
  (add-observers (assoc app 
      :area area 
      :output-label output-label 
      :lower-window lower-win 
      :status-bar sb 
      :indicator indicator
      :lower-status-bar lower-sb) 
    layout-observer) ))



