;;  Copyright (c) Itay Maman. All rights reserved.
;;  The use and distribution terms for this software are covered by the
;;  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;  which can be found in the file epl-v10.html at the root of this distribution.
;;  By using this software in any fashion, you are agreeing to be bound by
;;  the terms of this license.
;;  You must not remove this notice, or any other, from this software.



(ns net.sourceforge.waterfront.ide)
(require 'net.sourceforge.waterfront.ide.ui)
(require 'net.sourceforge.waterfront.kit.kit)


(net.sourceforge.waterfront.kit/later net.sourceforge.waterfront.ide/launch-waterfront *command-line-args* { 
  :font-name "Courier New"
  :font-size 16
  :font-style 0
  :ignore ["syntax-coloring.clj"]
  :keys-to-save [
    :keys-to-save
    :plugin-path
    :last-search
    :font-size
    :font-name
    :font-style
    :plugins
    :recent-files
    :ignore]
  :plugin-path "src/net/sourceforge/waterfront/ide/plugins/"
  :plugins [ 
    "custom-editor.clj"
    "output-window.clj"
    "context-menu.clj"
    "file.clj"
    "recent-files.clj"
    "file-chooser-dir.clj"
    "problem-window.clj"
    "undo.clj"
    "basic-editing.clj"
    "goto.clj"
    "line-column.clj"
    "find.clj"
    "comments.clj"
    "show-doc.clj"
    "reflect.clj"
    "check-syntax.clj"
    "run.clj"
    "paren-matching.clj"
    "font-size.clj"
    "indent.clj"
    "load-recent-on-startup.clj"
    "eval-as-you-type.clj"
    "templates.clj"
    "auto-complete.clj"]
  :recent-files []
  :startup (quote( fn [app] ((load-file "src/net/sourceforge/waterfront/ide/plugins/plugin-loader.clj") app)))})   








