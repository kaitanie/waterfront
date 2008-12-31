(ns net.sourceforge.waterfront.ide.plugins)

(refer 'net.sourceforge.waterfront.kit)



(defn to-vec [elements]
  (apply vector elements))


(defn add-history-tag [x]
  (with-meta x { :is-history-item true }) )

(defn recent-files-observer 
  "Maintain a history of opened files" 
  [old-app new-app]  
  (when (maps-differ-on old-app new-app :file-name)
    (transform new-app :recent-files [] 
      (fn [fs] 
        (to-vec 
          (take     ; Limit history size 
            10 
            (cons   ; Remove the file name from the list and push it on front
              (new-app :file-name)
              (filter (fn [x] (not= x (new-app :file-name))) fs) )))))))



(defn create-a-history-item [path ordinal]
  (add-history-tag
    { :name (str ordinal " " path)
      :mnemonic (+ ordinal java.awt.event.KeyEvent/VK_0)
      :action (fn [app] (load-document (set-current-document app path))) }))
   
   
(defn create-history-items [recent-files]
   (map create-a-history-item (take 9 recent-files) (iterate inc 1)) )

(defn recent-files-menu-observer [old-app new-app] 
  "Add menu items to open recently opened files"

  (when (or (maps-differ-on old-app new-app :recent-files) (= :unknown (old-app :file-name)))
    (let [result 
      (transform new-app :menu nil 
        (partial 
          change-menu 
          "File" 
          (fn [items] 
            (to-vec 
              (apply conj 
                (to-vec (filter (fn [x] (not (:is-history-item (meta x)))) items))
                (to-vec (cons (add-history-tag { }) (create-history-items (new-app :recent-files)))) )))))]            
      result)))
            

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
        add-observer (fn [app] 
                        (transform 
                          app 
                          :observers 
                          [] 
                          (fn [observers] 
                            (apply vector 
                              (concat  
                                [recent-files-observer recent-files-menu-observer]
                                observers )))))]
  (add-save-key (add-observer app)) ))





