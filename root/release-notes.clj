

; 28-Dec-08: plugins (setup function)
; 28-Dec-08: Bug fix - Exception in dispatch are now caught
; 28-Dec-08: show doc
; 28-Dec-08: goto
; 27-Dec-08: line wrapping
; 26-Dec-08: line end is always \n 
; 26-Dec-08: uncomment
; ??-Dec-08: interpret menu definitions
; ??-Dec-08: recently opened
; ??-Dec-08: make the list of app items that are saved part of app itself.
; 23-Jan-09: grouped undo/redo
; 25-Jan-09: make paren highlighting invisible WRT undo/redo
; 26-Jan-09: (app :change) is a functions that allow currently executing code to change app 
; 30-Jan-09: move word: stop at "-"
; 31-Jan-09: Load recent file on startup is now handled by a dedicated plugin
; ??-Feb-09: File -> Revert
; 03-Feb-09: Green/Red indicator shows evaluation status of the code - updated on the fly
; 04-Feb-09: Make syntax problems messages more descriptive
; 04-Feb-09: Status bar
; 05-Feb-09: TAB indents a selection
; 05-Feb-09: Improve next/prev heuristic in the presence of parenthesis/braces/brackets
; 05-Feb-09: Threads are now daemons
; 06-Feb-09: Shutdown the JVM when last window is closed
; 06-Feb-09: Source -> Generate: Proxy, Overloading, Try-Catch
; 06-Feb-09: A default .waterfront.config.clj file is generated if does not exist
; 07-Feb-09: Context menu
; 08-Feb-09: Auto-complete
; 08-Feb-09: Jump to errorneus line 
; 10-Feb-09: Bug fix: list of recently opened files in a new window
; 10-Feb-09: Bug fix: Updating of the Line-Col indicator in response to searching/jumping to an error
; 10-Feb-09: Asks whether to Save a dirty file before openning a file
; 12-Feb-09: New search options: cyclic, case sensitive
; 12-Feb-09: Input-form: ESCAPE => Cancel, Return => OK
; 13-Feb-09: Search box shows a combo-box with search history
; 14-Feb-09: Replace
; 14-Feb-09: Show a "phrase not found" message
; 14-Feb-09: Opens a "Discard changes?" dialog when reverting a dirty file
; 15-Feb-09: Smart proxy generation (generates methods signatures based on user-supplied super types)
; 16-Feb-09: A form dialog (e.g.: Find dialog) is now placed relative its owner
; 16-Feb-09: Uses setParagraphAttributes for setting the font of the editor pane
; 16-Feb-09: Syntax error (problem window) are now double-clickable: jumps to corresponding line
; 16-Feb-09: File chooser uses *.clj by default
; 16-Feb-09: Loads a file from the command line (if specified)
; 16-Feb-09: Diagnostic messages are written to (app :log)
; 16-Feb-09: Eval as you type can be disabled (Run -> Eval as you type)
; 17-Feb-09: Disabling of menu items (undo, redo, increase/decrease font)
; 17-Feb-09: Eval menu item is now either "Eval File" or "Eval Selection"
; 17-Feb-09: Improved the undo behavior of replace, replace-all
; 19-Feb-09: In save-as a *.clj extension is added if none specified
; 19-Feb-09: Auto-completion only shows the first N entries
; 19-Feb-09: Periodic text observer added => The greed/red indicator is repsonsive to uncomment, undo, replace, etc.
; 19-Feb-09: Improved looks of the lower status bar
; 22-Feb-09: Tooltip on red markers
; 23-Feb-09: Reflect java classes 
; 24-Feb-09: (app :visit) added
; 24-Feb-09: Ported to Clojure's latest snapshot 
; 25-Feb-09: Side-by-side layout
; 25-Feb-09: If loading of a plugin fails, continue with other plugins
; 27-Feb-09: Changed default location of the divider
; 27-Feb-09: Default key mask (for accelerators) is now obtained from the Toolkit class
; 27-Feb-09: Java 1.5 compilance (prev. 1.6)
; 27-Feb-09: Added a space in the execution time between the digits and "ms". E.g., "34ms" --> "34 ms" 
; 27-Feb-09: Source -> Doc and Source -> Reflect consolidated into a single menu item
; 27-Feb-09: wf.sh file added 
; 27-Feb-09: wf.bat handles spaces in the path





