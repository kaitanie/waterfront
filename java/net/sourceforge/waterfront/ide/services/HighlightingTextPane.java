//  Copyright (c) Itay Maman. All rights reserved.
//  The use and distribution terms for this software are covered by the
//  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//  which can be found in the file epl-v10.html at the root of this distribution.
//  By using this software in any fashion, you are agreeing to be bound by
//  the terms of this license.
//  You must not remove this notice, or any other, from this software.

package net.sourceforge.waterfront.ide.services;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;

/**
 * A standard JTextPane that offers a more intuitive interface for the 
 * highlighting mechanism.
 * 
 * @author imaman
 */
public class HighlightingTextPane extends JTextPane implements KeyListener
{
  private static final long serialVersionUID = 3184757346089731111L;

  private List<Object> tags = new ArrayList<Object>();
  private Runnable indentAction = null;
  private Runnable unindentAction = null;
      
  
  public HighlightingTextPane()
  {
    addKeyListener(this);
  }
  
  public void setIndentAction(Runnable r)
  {
    indentAction = r;
  }
  
  public void setUnindentAction(Runnable r)
  {
    unindentAction = r;
  }
  
  public void clearHighlights()
  {
    Highlighter h = getHighlighter();
    if(tags.size() > 0)
    {
      for(Object tag : tags)
        h.removeHighlight(tag);
      tags.clear();      
    }      
  }
  
  
  public void addHighlights(Color c, int pos)
  {      
    Highlighter h = getHighlighter();
    try
    {
        tags.add(h.addHighlight(pos, pos + 1, new DefaultHighlightPainter(c)));
    }
    catch (BadLocationException ble)
    {
      ble.printStackTrace();
    }
  }

  public void keyPressed(KeyEvent e)  
  {
    if(e.getKeyCode() != KeyEvent.VK_TAB)
      return;
    
    boolean unindent = e.isShiftDown();
    if(unindent)
    {
      if(unindentAction == null)
        return;
      
      unindentAction.run();
      e.consume();
    }
    else
    {
      if(indentAction == null)
        return;
      
      
      if(getSelectionStart() == getSelectionEnd())
        return;
      
      indentAction.run();
      e.consume();      
    }
  }

  public void keyTyped(KeyEvent e)
  {
    // Do nothing
  }

  public void keyReleased(KeyEvent e)
  {
    // Do nothing
  }

  
  
}
