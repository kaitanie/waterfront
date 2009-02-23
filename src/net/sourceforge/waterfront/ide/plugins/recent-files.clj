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


(defn to-vec [elements]
  (apply vector elements))


(defn add-history-tag [x]
  (with-meta x { :is-history-item true }) )

(defn- add-recent-to-file-menu [new-app]
  (transform new-app :recent-files [] 
    (fn [fs] 
      (to-vec 
        (take     ; Limit history size 
          10 
          (cons   ; Remove the current file name from the list and push it on front
            (new-app :file-name)
            (filter (fn [x] (not= x (new-app :file-name))) fs) ))))))


(defn recent-files-observer 
  "Maintain a history of opened files" 
  [old-app new-app]  
  
  (when (maps-differ-on old-app new-app :file-name)
    (add-recent-to-file-menu new-app) ))
             

(defn create-a-history-item [path ordinal]
  (add-history-tag
    { :name (str ordinal " " path)
      :mnemonic (+ ordinal java.awt.event.KeyEvent/VK_0)
      :action (fn [app] (save-and-or-do-something app (fn [app-tag] (load-document (set-current-document app-tag path))))) }))
   
   
(defn create-history-items [recent-files exclude]
   (map create-a-history-item (take 9 (filter (fn [x] (not= x exclude)) recent-files)) (iterate inc 1) ))


(defn recent-files-menu-observer [old-app new-app] 
  "Add menu items to open recently opened files"

   (when (or 
            (maps-differ-on old-app new-app :recent-files :file-name) 
            (nil? (new-app :recent-menu-created)))
    (let [result 
      (transform (assoc new-app :recent-menu-created true) :menu nil 
        (partial 
          change-menu 
          "File" 
          (fn [items] 
            (to-vec 
              (apply conj 
                (to-vec (filter (fn [x] (not (:is-history-item (meta x)))) items))
                (to-vec (cons (add-history-tag {}) (create-history-items (new-app :recent-files) (new-app :file-name)))) )))))]
      result )))
      
            

(fn [app] 
  (let [
        add-save-key (fn [app] 
                        (transform 
                          app 
                          :keys-to-save 
                          [] 
                          (fn [ks] 
                            (if (includes :recent-files ks)
                              ks
                              (conj ks :recent-files) ))))

      result (assoc (add-save-key          
          (add-observers (load-plugin app "menu-observer.clj") 
            recent-files-observer recent-files-menu-observer)) 
          :recent-menu-created nil)]
      result ))










