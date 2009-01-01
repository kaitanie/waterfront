{ 
  :plugin-path "src/net/sourceforge/waterfront/ide/plugins/"
  :font-size 16
  :recent-files [ 
    "C:\\local\\workspace\\waterfront\\src\\net\\sourceforge\\waterfront\\ide\\plugins\\paren-matching.clj"
    "C:\\local\\workspace\\waterfront\\src\\net\\sourceforge\\waterfront\\ide\\plugins\\standard-observers.clj"
    "C:\\local\\workspace\\waterfront\\src\\net\\sourceforge\\waterfront\\ide\\plugins\\font-size.clj"
    "C:\\local\\workspace\\waterfront\\.ecosystem.config.clj"
    "C:\\local\\workspace\\waterfront\\src\\net\\sourceforge\\waterfront\\kit\\kit.clj"
    "C:\\local\\workspace\\waterfront\\src\\net\\sourceforge\\waterfront\\ide\\services\\lexer.clj"
    "C:\\local\\workspace\\waterfront\\src\\net\\sourceforge\\waterfront\\ide\\uistarter.clj"
    "C:\\local\\workspace\\waterfront\\src\\net\\sourceforge\\waterfront\\ide\\plugins\\syntax-coloring.clj"
    "C:\\local\\workspace\\waterfront\\src\\net\\sourceforge\\waterfront\\ide\\plugins\\undo.clj"
    "C:\\local\\workspace\\waterfront\\clojure-src\\clj\\clojure\\core.clj"
  ]
  :startup ( 
    quote
    ( 
      fn
      [ 
        app
      ]
      ( 
        ( 
          load-file
          "src/net/sourceforge/waterfront/ide/plugins/plugin-loader.clj"
        )
        app
      )
    )
  )
  :plugins [ 
    "custom-editor.clj"
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
    "font-size.clj"
    "syntax-coloring.clj"
    "paren-matching.clj"
  ]
  :font-style 0
  :font-name "Courier New"
  :ignore [ 
  ]
  :keys-to-save [ 
    :keys-to-save
    :plugin-path
    :last-search
    :font-size
    :font-name
    :font-style
    :plugins
    :recent-files
    :ignore
  ]
  :last-search "runnable"
}
