package net.sourceforge.waterfront.ide.services;

import javax.swing.text.AbstractDocument;
import javax.swing.text.Element;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class NoWrapEditorKit extends StyledEditorKit
{
  private static final long serialVersionUID = -8471542705663511786L;
  
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
    
    @Override
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
}
