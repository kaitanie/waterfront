

(ns net.sourceforge.waterfront.ide)
(require 'net.sourceforge.waterfront.ide.ui)
(require 'net.sourceforge.waterfront.kit.kit)


(net.sourceforge.waterfront.kit/later net.sourceforge.waterfront.ide/launch-waterfront { 
  :font-name "Courier New"
  :font-size 16
  :font-style 0
  :ignore []
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
    "context-menu.clj"
    "file.clj"
    "recent-files.clj"
    "standard-observers.clj"
    "file-chooser-dir.clj"
    "output-window.clj"
    "problem-window.clj"
    "undo.clj"
    "basic-editing.clj"
    "goto.clj"
    "find.clj"
    "comments.clj"
    "show-doc.clj"
    "check-syntax.clj"
    "run.clj"
    "syntax-coloring.clj"
    "paren-matching.clj"
    "font-size.clj"
    "indent.clj"
    "load-recent-on-startup.clj"
    "check-syntax-online.clj"
    "line-column.clj"
    "templates.clj"]
  :recent-files []
  :startup (quote( fn [app] ((load-file "src/net/sourceforge/waterfront/ide/plugins/plugin-loader.clj") app)))})   








