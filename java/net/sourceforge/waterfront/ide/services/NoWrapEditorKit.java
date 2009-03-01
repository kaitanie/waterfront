//  Copyright (c) Itay Maman. All rights reserved.
//  The use and distribution terms for this software are covered by the
//  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//  which can be found in the file epl-v10.html at the root of this distribution.
//  By using this software in any fashion, you are agreeing to be bound by
//  the terms of this license.
//  You must not remove this notice, or any other, from this software.

package net.sourceforge.waterfront.ide.services;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.UIManager;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.TextAction;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;


public class NoWrapEditorKit extends StyledEditorKit
{
  private static final long serialVersionUID = -8471542705663511786L;
  
  private static final Action[] ADDITIONAL_ACTIONS =
  {
    new NextWord(nextWordAction),
    new PrevWord(previousWordAction),
    new NextWord(selectionNextWordAction).select(),
    new PrevWord(selectionPreviousWordAction).select()
  };

  
  private final ViewFactory vf = new MyViewFactory(super.getViewFactory());
  
  
  private static class MyParagraphView extends ParagraphView
  {
    public MyParagraphView(Element elem)
    {
      super(elem);
    }

    @Override
    protected void layout(int width, int height)
    {      
      super.layout(java.lang.Short.MAX_VALUE, height);
    }
    
    @Override
    public float getMinimumSpan(int axis)
    {
      return super.getPreferredSpan(axis);
    }    
  }
  
  private static class MyViewFactory implements ViewFactory
  {
    private final ViewFactory inner;
    
    public MyViewFactory(ViewFactory inner_)
    {
      inner = inner_;
    }
    
    public View create(Element elem)
    {
      if(elem != null)
      {
        if(AbstractDocument.ParagraphElementName.equals(elem.getName()))
          return new MyParagraphView(elem);
      }
      return inner.create(elem);
    }    
  }
  
  public ViewFactory getViewFactory()
  {
    return vf;
  }
  
  @Override
  public Action[] getActions()
  {
    return TextAction.augmentList(super.getActions(), ADDITIONAL_ACTIONS);
  }
  
  static abstract class WordAction extends TextAction
  {
    private static final long serialVersionUID = -2741035942276450382L;
    
    private boolean select = false;

    public WordAction(String name)
    {
      super(name);
    }
    
    protected abstract int getNewOffset(String text, int len, int offset);
    protected abstract int resolve(int offset);
    
    public WordAction select()
    {
      select = true;
      return this;
    }

    public void actionPerformed(ActionEvent e)
    {
      JTextComponent target = getTextComponent(e);
      if (target == null)
      {
        UIManager.getLookAndFeel().provideErrorFeedback(target);
        return;
      }

      int offset = target.getCaretPosition();
      
      String text = target.getText();
      int len = text.length();
      if(len <= 0)
        return;
      
      int newOffset = getNewOffset(text, text.length(), offset);
      if(newOffset == offset)
        newOffset = resolve(offset);
      
      if (newOffset < 0 || newOffset > len)
        return;
      
      if(select)
        target.moveCaretPosition(newOffset);
      else
        target.setCaretPosition(newOffset);
    }
  }
  
  protected static boolean isOpen(char c)
  {
    return c == '(' || c == '[' || c == '{';
  }
  
  protected static boolean isClose(char c)
  {
    return c == ')' || c == ']' || c == '}';
  }
  
  protected static boolean isNonNewLineBlank(char c)
  {
    return Character.isWhitespace(c) && c != '\n';
  }
  
  
  static class NextWord extends WordAction
  {
    private static final long serialVersionUID = 8142626896026746229L;

    public NextWord(String name)
    {
      super(name);
    }

    protected int resolve(int offset)
    {
      return offset + 1;
    }
        
    protected int getNewOffset(String text, int len, int offset)
    {
      if(offset >= len)
        return len;
      
      int i;
      
      char c0 = text.charAt(offset);
      
      if(isOpen(c0))
      {
        for(i = offset + 1; i < len; ++i)
        {
          char c = text.charAt(i);
          if(!isOpen(c) && !isNonNewLineBlank(c))
            break;
        }
        
        return i;
      }

      if(isClose(c0))
      {
        for(i = offset + 1; i < len; ++i)
        {
          char c = text.charAt(i);
          if(!isClose(c) && !isNonNewLineBlank(c))
            break;
        }
        
        return i;
      }
      
      if(!Character.isJavaIdentifierPart(c0))
      {
        for(i = offset + 1; i < len; ++i)
        {
          char c = text.charAt(i);
          if(Character.isJavaIdentifierPart(c) || isOpen(c) || isClose(c))
            break;       
        }
        
        return i;
      }
      
      
      
      // Starting from a Java identifier letter
      for(i = offset + 1; i < len; ++i)
      {
        char c = text.charAt(i);
        if(!Character.isJavaIdentifierPart(c))
          break;        
      }

      for( ; i < len; ++i)
      {
        char c = text.charAt(i);
        if(c != '-')
          break;        
      }
      
      return Math.min(len, i);
    }
  }
    
  static class PrevWord extends WordAction
  {
    private static final long serialVersionUID = 4785408872375970781L;


    public PrevWord(String name)
    {
      super(name);
    }


    protected int resolve(int offset)
    {
      return offset - 1;
    }
    
    protected int getNewOffset(String text, int len, int offset)
    {      
      if(offset <= 1)        
        return 0;
      
      int i;
      
      offset -= 1;

      char c0 = text.charAt(offset);
      if(isOpen(c0))
      {
        for(i = offset - 1; i  >= 0; --i)
        {
          char c = text.charAt(i);
          if(!isOpen(c) && !isNonNewLineBlank(c))
            break;
        }
        
        return i + 1;
      }

      if(isClose(c0))
      {
        for(i = offset - 1; i  >= 0; --i)
        {
          char c = text.charAt(i);
          if(!isClose(c) && !isNonNewLineBlank(c))
            break;
        }
        
        return i + 1;
      }      
      
      if ((c0 != '-') && !Character.isJavaIdentifierPart(c0))
      {
        for(i = offset - 1; i >= 0; --i)
        {
          char c = text.charAt(i);
          if(Character.isJavaIdentifierPart(c) || isOpen(c) || isClose(c))
            return i + 1;       
        }
        
        return 0;
      }

      // Starting from a Java identifier letter
      for(i = offset - 1; i >= 0; --i)
      {        
        char c = text.charAt(i);
        if(c != '-')
          break;
      }
      
      for( ; i >= 0; --i)
      {
        char c = text.charAt(i);
        if(!Character.isJavaIdentifierPart(c))
        {
          i += 1;
          break;
        }         
      }
      
      return Math.min(len, Math.max(0, i));
    }
  }
}
