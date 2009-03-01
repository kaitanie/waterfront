;;  Copyright (c) Itay Maman. All rights reserved.
;;  The use and distribution terms for this software are covered by the
;;  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;  which can be found in the file epl-v10.html at the root of this distribution.
;;  By using this software in any fashion, you are agreeing to be bound by
;;  the terms of this license.
;;  You must not remove this notice, or any other, from this software.

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


(defn- paint-line-numbers-panel [storage sp tp lnp g dispatch] 
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
        (swap! storage (fn [old-value] old-value { :startline startline :endline endline :starting_y starting_y :font-height fontHeight }))
        (paint-line-numbers tp lnp g startline endline starting_y fontHeight fontDesc fontAscent (sort (map (fn[x] (x :line)) (app :markers))))))
      g )))
    
(defn- new-panel [paint-hook tooltip-provider]
  (let [provider
        (proxy [net.sourceforge.waterfront.ide.services.CustomTooltipPanel$Provider] []
          (getText [#^java.awt.event.MouseEvent me] (tooltip-provider me)))

        result 
          (proxy [net.sourceforge.waterfront.ide.services.CustomTooltipPanel] [provider]

            (paint [g]
              (proxy-super paint g)
              ((graphics-wrapper paint-hook) g) ))]
    result ))




(defn- compute-tooltip [app storage local-atom mouse-event]  
  (let [state @storage
        line (int (+  1 (state :startline) (/ (- (.getY mouse-event) (state :starting_y)) (state :font-height))))]
    (if (and (>= line (state :startline)) (<= line (state :endline)))
      (do 
        ((app :dispatch) (fn [app-tag]
          (let [chosen (filter (fn [x] (= (x :line) line)) (app-tag :markers))]
            (if (empty? chosen)
              (swap! local-atom (fn [x] nil)) 
              (swap! local-atom (fn [x] (apply str (rest (apply concat (map (fn [x y] [x (y :msg)]) (repeat "; ") chosen)))))) ))
          app-tag ))
        @local-atom )
      nil )))

(defn create-line-numbers-components [app]
  (let [parts (atom nil)
        storage (atom {})
        paint-numbers-wrapped (fn [g]
          (let [new-g (.create g)]
            (try
              (paint-line-numbers-panel storage (@parts :scroll-pane) (@parts :text-pane) 
                (@parts :line-number-panel) new-g (app :dispatch))
              (finally (.dispose new-g) ))))
        lnp (new-panel paint-numbers-wrapped (partial compute-tooltip app storage (atom nil)))


        scroll-bar-ui (javax.swing.plaf.basic.BasicScrollBarUI.)
        tp (new-custom-text-pane (fn [g] (.repaint lnp)) )
        sp (javax.swing.JScrollPane. tp)
        composite (javax.swing.JPanel.)]

    (swap! parts (fn [x] { :text-pane tp :scroll-pane sp :line-number-panel lnp }))

    (.setToolTipText lnp "")
    (.setMinimumSize lnp (java.awt.Dimension. 40 30))
    (.setPreferredSize lnp (java.awt.Dimension. 40 30)) 
    
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
        area (lnp-widgets :text-pane)]

  (.setLayout sb (java.awt.FlowLayout. java.awt.FlowLayout/LEFT 5 3))

  (.setLayout lower-sb (javax.swing.BoxLayout. lower-sb javax.swing.BoxLayout/LINE_AXIS))

  (.add (app :frame) lower-sb BorderLayout/SOUTH)
  
  (.add (app :frame) (doto (JSplitPane. 
                              JSplitPane/HORIZONTAL_SPLIT
                              (lnp-widgets :composite)
                              (doto (javax.swing.JPanel.)
                                (.setLayout (BorderLayout.))
                                (.setMinimumSize (java.awt.Dimension. 300 10))
                                (.add lower-win BorderLayout/CENTER)))
            (.setDividerLocation -1)
            (.setResizeWeight 1.0) )
          (. BorderLayout CENTER) )

  
  (add-observers (assoc app 
      :area area 
      :lower-window lower-win 
      :status-bar sb 
      :lower-status-bar lower-sb) 
    layout-observer) ))








