//  Copyright (c) Itay Maman. All rights reserved.
//  The use and distribution terms for this software are covered by the
//  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//  which can be found in the file epl-v10.html at the root of this distribution.
//  By using this software in any fashion, you are agreeing to be bound by
//  the terms of this license.
//  You must not remove this notice, or any other, from this software.

package net.sourceforge.waterfront.ide.services; 

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.LabelView;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class Main
{
  static final SimpleAttributeSet BLUE = new SimpleAttributeSet();
  static final SimpleAttributeSet PLAIN = new SimpleAttributeSet();
  static final SimpleAttributeSet RED = new SimpleAttributeSet();
  
  static
  {
    StyleConstants.setForeground(BLUE, Color.BLUE);
    StyleConstants.setBold(BLUE, true);

    StyleConstants.setForeground(RED, Color.RED);
  }

  JTextPane lower = new JTextPane();
  final DefaultStyledDocument doc = new DefaultStyledDocument();
  final JTextPane tp = new JTextPane();  
  private final GroupingUndoListener um = new GroupingUndoListener();
  private final MyKit myKit = new MyKit();
  
  private void print(Throwable t)
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    
    t.printStackTrace(pw);
    pw.flush();
    
    print(sw.toString());
  }
  
  private void print(String s)
  {
    lower.setText(s);
  }
  
  private class MyViewFactory implements ViewFactory
  {
    private final ViewFactory inner;
    
    public MyViewFactory(ViewFactory inner_)
    {
      inner = inner_;
    }

    
    public View create(final Element elem)
    {
      View result = inner.create(elem);
      if(!(result instanceof LabelView))
        return result;
     
      System.out.println("elem=" + elem.getStartOffset() + ".." + elem.getEndOffset());
//      if(highlighted < 0)
//        return result;
      
      return new Hack(elem);      
    }
  }

  public class Hack extends LabelView 
  {    
    public Hack(Element elem)
    {
      super(elem);
      System.out.println("Created for " + elem);
    }

//    @Override
//    public Color getForeground()
//    {
//      if(highlighted >= 0)
//        return Color.BLUE;
//      return Color.BLACK;
//    }
  }
  
  private class MyKit extends StyledEditorKit
  {
    private static final long serialVersionUID = 2852116985186677072L;
    private ViewFactory vf;
    
    public MyKit()
    {
      ViewFactory inner = super.getViewFactory();
      vf = new MyViewFactory(inner);
    }

    @Override
    public ViewFactory getViewFactory()
    {
      return vf;
    }
  }
  
  void go() throws Exception
  {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    JFrame f = new JFrame();
    f.setLayout(new BorderLayout());
           
    doc.addUndoableEditListener(um);
    
    tp.setDocument(doc);
    tp.setEditorKit(myKit);
    
    tp.setFont(new Font("Courier New", Font.PLAIN, 16));
    f.add(new JScrollPane(tp), BorderLayout.CENTER);
    
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setSize(500, 900);
    f.setVisible(true);
    
    lower.setMinimumSize(new Dimension(100, 300));
    lower.setPreferredSize(lower.getMinimumSize());
    lower.setSize(lower.getMinimumSize());
    f.add(new JScrollPane(lower), BorderLayout.SOUTH);
    
    JMenuBar bar = new JMenuBar();
    JMenu editMenu = new JMenu("Edit");
    bar.add(editMenu);
    
    editMenu.setMnemonic(KeyEvent.VK_E);
    
    
    JMenuItem mi = new JMenuItem("Undo");
    editMenu.add(mi);
    mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));    
    mi.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        try
        {
          if(um.canUndo())
            um.undo();
          print("Undo man=" + um);                  
        }
        catch(Throwable t)
        {
          print(t);  
          t.printStackTrace();
        }
      }      
    });

    mi = new JMenuItem("Redo");
    editMenu.add(mi);
    mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));    
    mi.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if(um.canRedo())
          um.redo();
        print("Undo man=" + um);                            
      }      
    });
    
    editMenu.addSeparator();
    
    

    mi = new JMenuItem("Paint");
    editMenu.add(mi);
    mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK));    
    mi.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {        
        String s = tp.getText();        
        int len = s.length();
        
        SimpleAttributeSet sas = new SimpleAttributeSet();
        StyleConstants.setForeground(sas, Color.red);
        StyleConstants.setBold(sas, true);
        
        SimpleAttributeSet plain = new SimpleAttributeSet();
        try
        {
          for(int i = 0; i < len; ++i)
          {
            int asc = (int) s.charAt(i);
            if(asc % 5 == 0)
              doc.setCharacterAttributes(i, 1, sas, true);
            else
              doc.setCharacterAttributes(i, 1, plain, true);
          }
        }
        catch(Throwable t)
        {
          print(t);
        }
      }      
    });
    
    f.setJMenuBar(bar);
    

    tp.addCaretListener(new CaretListener()
    {
      public void caretUpdate(CaretEvent e)
      {
        highlightLater();
      }            
    });
    
    doc.addDocumentListener(new DocumentListener()
    {

      public void changedUpdate(DocumentEvent e)
      {
//        highlightLater();      
      }

      public void insertUpdate(DocumentEvent e)
      {
        highlightLater();      
      }

      public void removeUpdate(DocumentEvent e)
      {
        highlightLater();      
      }      
    });
    
    String s = "";
    for(Scanner sc = new Scanner(new File("src/net/sourceforge/waterfront/ide/services/Main.java")); sc.hasNext(); )
      s += sc.nextLine() + "\n";
    
    tp.setText(s);
    for(int i = 0; i < s.length(); ++i)
      doc.setCharacterAttributes(i, 1, RED, true);
//      doc.setCharacterAttributes(i, 1, i % 2 == 0 ? PLAIN : RED, true);
    um.discardAllEdits();
  }
  
  protected void highlightLater()
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        highlight();        
      }
    });
  }

  private int highlighted = -1;
    
  public void highlight()
  {
    boolean on = highlighted >= 0;
    highlightImpl();
    
    boolean on2 = highlighted >= 0;
    if(on != on2)
      tp.repaint();
    
  }
  public void highlightImpl()
  {
    System.out.println("Clearing from "  + highlighted + "!!!");
    int os = tp.getCaretPosition() - 1;
    if(os == highlighted)
      return;
    
    highlighted = -1;
    String text = tp.getText();
    if(os < 0 || os >= text.length())
      return;
    
    char c = text.charAt(os);
    if(c != '(' && c != ')' && c != '{' && c != '}')
      return;
    
    highlighted = os;
  }
  
  public Element getCharacterElement(int pos) 
  {
    Element e = null;
    System.out.println("Searching for " + pos);
    e = doc.getDefaultRootElement(); 
    
    while(true)
    {
        int index = e.getElementIndex(pos);
        System.out.println("   -" + e.getStartOffset() + ".." + e.getEndOffset());
        if(e.isLeaf())
          break;
        e = e.getElement(index);
    }
    return e;
  }
  
  
  public static void main(String[] args)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        try
        {
          new Main().go();
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    });
  }  
}
