(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)
(require 'net.sourceforge.waterfront.ide.services.services)
(refer 'net.sourceforge.waterfront.ide.services)



(defn- set-font [f widget]
  (try 
    (println "Setting font of " (class widget) " to " (.getName f))
    (.setFont widget f)
    (catch Throwable e (.printStackTrace e))))

(defn- set-fonts [app]
  (let [f (java.awt.Font. (app :font-name) (app :font-style) (app :font-size))]
    (println "New font detected:" (select-keys app [:font-name :font-style :font-size]))
    (set-font f (app :output-area))
    (set-font f (app :area))
    (set-font f (app :doc-area))
    (let [sas (javax.swing.text.SimpleAttributeSet.)
          is-on (fn [a b] (if (zero? (bit-and a b)) false true))]
      (javax.swing.text.StyleConstants/setFontFamily sas (app :font-name))
      (javax.swing.text.StyleConstants/setFontSize sas (app :font-size))
      (javax.swing.text.StyleConstants/setBold sas (is-on java.awt.Font/BOLD (app :font-style)))
      (javax.swing.text.StyleConstants/setItalic sas (is-on java.awt.Font/ITALIC (app :font-style)))
      (.setParagraphAttributes (app :area) sas true))
    (assoc app :active-font f) ))

 
(defn- update-font [old-app new-app]
  (if (or 
        (not (new-app :font-assigned)) 
        (maps-differ-on old-app new-app :font-size :font-name :font-style :area))
      (assoc ((new-app :enqueue) new-app set-fonts) :font-assigned true) ))

(fn [app] 
    (add-observers app update-font) )


