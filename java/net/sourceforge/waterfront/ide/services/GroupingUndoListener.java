//  Copyright (c) Itay Maman. All rights reserved.
//  The use and distribution terms for this software are covered by the
//  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//  which can be found in the file epl-v10.html at the root of this distribution.
//  By using this software in any fashion, you are agreeing to be bound by
//  the terms of this license.
//  You must not remove this notice, or any other, from this software.

package net.sourceforge.waterfront.ide.services;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.event.DocumentEvent.EventType;
import javax.swing.text.AbstractDocument.DefaultDocumentEvent;
import javax.swing.undo.UndoableEdit;

public class GroupingUndoListener implements UndoableEditListener
{

  private static class Group
  {
    private final UndoableEdit ue;
    private final List<UndoableEdit> list = new ArrayList<UndoableEdit>();
    private final EventType type;
    
    public Group(UndoableEdit ue_)
    {
      ue = ue_;
      type = null;
    }
    
    public Group(DefaultDocumentEvent dde)
    {
      ue = null;
      list.add(dde);
      type = dde.getType();
    }
    
    public void undo()
    {
      if(ue != null)
      {
        ue.undo();
        return;
      }

      for(int index = list.size() - 1; index >= 0; --index)
      {
        UndoableEdit temp = list.get(index);
        temp.undo();        
      }
    }

    public void redo()
    {
      if(ue != null)
      {
        ue.redo();
        return;
      }

      for(UndoableEdit curr : list)
        curr.redo();        
    }
    
    @Override
    public String toString()
    {
      return ue == null ? "nothing" : ue.getPresentationName();
    }
    
    public void forceAdd(DefaultDocumentEvent e)
    {
      list.add(e);
    }

    public boolean add(DefaultDocumentEvent e)
    {
      if(ue != null)
        throw new AssertionError("Should only be invoked if ue == null");
      
      if(!EventType.CHANGE.equals(e.getType()))
      {
        if(!type.equals(e.getType()))
          return false;
        
        if(!adjacentRange(list.get(list.size() - 1), e))
          return false;
      }
      
      forceAdd(e);
      return true;
    }
    
    private static boolean adjacentRange(UndoableEdit a, DefaultDocumentEvent b)
    {
      if(!(a instanceof DefaultDocumentEvent))
        return false;
      
      DefaultDocumentEvent dde = (DefaultDocumentEvent) a;
      
      int fromA = dde.getOffset();
      int toA = fromA + dde.getLength();
      
      int fromB = b.getOffset();
//      int toB = fromB + b.getLength();
      
      
      if((Math.abs(fromA - fromB) <= 1) || (Math.abs(toA - fromB) <= 1))
        return true;
      
      return false;
    }

    public boolean forcedAdd(UndoableEdit e)
    {
      if(ue != null)
        throw new AssertionError("Should only be invoked if ue == null");
    
      list.add(e);
      return true;
    }
  }

  
  private ArrayList<Group> groups = new ArrayList<Group>();
  private int lastApplied = -1;
  private int lastPushed = -1;
  private boolean sticky = false;
      
  public void undoableEditHappened(UndoableEditEvent uee)
  {    
    UndoableEdit e = uee.getEdit();
    if(e instanceof DefaultDocumentEvent)
      add((DefaultDocumentEvent) e);
    else
      add(e);    
  }
  
  public void setSticky(boolean b)
  {
    sticky = b;
  }

  private void push(Group g)
  {
    ++lastApplied;
    lastPushed = lastApplied;
    groups.ensureCapacity(lastApplied + 10);
    if(lastApplied == groups.size())
      groups.add(null);
    groups.set(lastApplied, g);
  }
  
  private boolean hasApplied()
  {
    return lastApplied >= 0;
  }
  
  private Group getLastApplied()
  {
    if(!hasApplied())
      return null;
    
    return groups.get(lastApplied);
  }

  private void add(UndoableEdit e)
  {
    push(new Group(e));    
  }
  
  private void add(DefaultDocumentEvent e)
  {
    e.end();
    
    if(!hasApplied())
    {
      push(new Group(e));
      return;
    }
    
    Group prev = getLastApplied();
    if(sticky)
    {
      prev.forceAdd(e);
      return;
    }
    
    
    if(prev.add(e))
      return;
        
    push(new Group(e));
  }
  
  public void undo()
  {
    if(!hasApplied())
      return;
    
    Group g = groups.get(lastApplied);
    lastApplied -= 1;
    g.undo();
  }

  public void redo()
  {
    if(lastApplied >= lastPushed)
      return;
    
    ++lastApplied;
    Group g = groups.get(lastApplied);
    g.redo();
  }
  
  public boolean canUndo()
  {
    return hasApplied();
  }

  public boolean canRedo()
  {
    return lastApplied < lastPushed;
  }

  public void discardAllEdits()
  {
    lastApplied = -1;
    lastPushed = -1;
  }
  
  private static String show(int n)
  {
    return n < 0 ? "nothing" : "" + n;
  }
  
  @Override
  public String toString()
  {
    return show(lastApplied) + "/" + show(lastPushed);
  }

}
