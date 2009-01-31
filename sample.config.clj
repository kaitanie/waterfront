{ 
  :plugin-path "src/net/sourceforge/waterfront/ide/plugins/"
  :recent-files [ 
    "C:\\local\\workspace\\waterfront\\src\\net\\sourceforge\\waterfront\\ide\\uistarter.clj"
    "C:\\local\\workspace\\waterfront\\src\\net\\sourceforge\\waterfront\\ide\\plugins\\load-recent-on-startup.clj"
    "C:\\local\\workspace\\waterfront\\src\\net\\sourceforge\\waterfront\\ide\\plugins\\plugin-loader.clj"
    "C:\\local\\workspace\\waterfront\\src\\net\\sourceforge\\waterfront\\ide\\plugins\\file.clj"
    "C:\\local\\workspace\\waterfront\\src\\net\\sourceforge\\waterfront\\ide\\plugins\\font-observer.clj"
    "C:\\local\\workspace\\waterfront\\src\\net\\sourceforge\\waterfront\\ide\\plugins\\indent.clj"
    "C:\\local\\workspace\\waterfront\\src\\net\\sourceforge\\waterfront\\ide\\plugins\\undo.clj"
    "C:\\local\\workspace\\waterfront\\src\\net\\sourceforge\\waterfront\\ide\\plugins\\comments.clj"
    "C:\\local\\workspace\\waterfront\\src\\net\\sourceforge\\waterfront\\ide\\services\\services.clj"
    "C:\\local\\workspace\\waterfront\\src\\net\\sourceforge\\waterfront\\kit\\kit.clj"
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
  :font-size 16
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
  :font-style 0
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
    "syntax-coloring.clj"
    "paren-matching.clj"
    "font-size.clj"
    "indent.clj"
    "load-recent-on-startup.clj"
  ]
  :font-name "Courier New"
  :ignore []
  :last-search "plugin"
}
