
package net.sourceforge.waterfront.ide.services;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.DocumentEvent.EventType;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.AbstractDocument.DefaultDocumentEvent;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

public class SmartUndoManager extends UndoManager
{
  private static final long serialVersionUID = -2153921636042589352L;
  private int muteCount = 0;
  
  private UndoManager alternate = new UndoManager();
  
//  private final Document doc;
//  
//  public SmartUndoManager(Document doc_)
//  {
//    doc = doc_;
//  }
  
  public synchronized void mute()
  {
//    muteCount += 1;
    if(muteCount == 1)
      alternate.discardAllEdits();
  }
  
  public synchronized void unmute()
  {
    muteCount -= 1;
    if(muteCount == 0)
    {
      while(alternate.canUndo())
        alternate.undo();
      alternate.discardAllEdits();
    }
  }
   
  public static String show(UndoableEdit e)
  {
    String s = String.valueOf(e);
    
    if(s.length() > 50)
    {
      if(e instanceof DefaultDocumentEvent)
      {
        DefaultDocumentEvent dde = (DefaultDocumentEvent) e;
        s = "DDE: " + dde.getType() + " @" + dde.getOffset() + ", len=" + dde.getLength();
      }
    }
    
    return s;
  }
  
  

  @Override
  public void undoableEditHappened(UndoableEditEvent e)
  {
    if(muteCount > 0)
      alternate.undoableEditHappened(e);
    else
      super.undoableEditHappened(e);
  }

  @Override
  public synchronized boolean addEdit(UndoableEdit anEdit)
  {    
    if (!(anEdit instanceof DefaultDocumentEvent))
      throw new AssertionError("Bad class: " + anEdit.getClass().getName());
    
    if(edits.isEmpty())
      return super.addEdit(anEdit);
    
    DefaultDocumentEvent first = (DefaultDocumentEvent) lastEdit();    
    DefaultDocumentEvent second = (DefaultDocumentEvent) anEdit;
    
    DefaultDocumentEvent e = combine(first, second);
    if(e == null)
      return super.addEdit(anEdit);
    
    edits.remove(edits.size() - 1);
    
    boolean result = super.addEdit(e);
    return result;
  }
  
  public DefaultDocumentEvent combine(DefaultDocumentEvent first, 
    DefaultDocumentEvent second)
  {
    if(first.getDocument() != second.getDocument())
      return null;
    
    
    if(first.getType() == EventType.CHANGE && 
      second.getType() == EventType.CHANGE)
    {
      return combine(first.getOffset(), first.getLength(), first.getType(), 
        first, second);      
    }
    
    if(first.getType() == EventType.REMOVE && 
      second.getType() == EventType.INSERT &&
      first.getOffset() == second.getOffset())
    {
      return combine(first.getOffset(), Math.max(first.getLength(), 
        second.getLength()), first.getType(), first, second);
    }
    
    if(second.getType() != first.getType())
      return null;
    
    int lastEndPos = first.getOffset() + first.getLength();
    if(first.getType() == EventType.REMOVE)
      lastEndPos = first.getOffset() - first.getLength();
    
    if(lastEndPos != second.getOffset())
      return null;

    return combine(first.getOffset(), first.getLength() + second.getLength(), 
      second.getType(), first, second);
  }

  private DefaultDocumentEvent combine(int offset, int length, EventType type,
    DefaultDocumentEvent first, DefaultDocumentEvent second)
  {      
    AbstractDocument doc = (AbstractDocument) first.getDocument();
    DefaultDocumentEvent result = doc.new DefaultDocumentEvent(offset, length,
      type)
    {
      private static final long serialVersionUID = 4389847344133511031L;

      @Override
      public String toString()
      {
        return getType() + " @" + getOffset() + " length=" + getLength();
      }
    };
    
    if(!result.addEdit(first))
      return null;
    
    if(!result.addEdit(second))
      return null;
    
    result.end();
    return result;
  }        
}