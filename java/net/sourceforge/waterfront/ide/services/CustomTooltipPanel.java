//  Copyright (c) Itay Maman. All rights reserved.
//  The use and distribution terms for this software are covered by the
//  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
//  which can be found in the file epl-v10.html at the root of this distribution.
//  By using this software in any fashion, you are agreeing to be bound by
//  the terms of this license.
//  You must not remove this notice, or any other, from this software.

package net.sourceforge.waterfront.ide.services;

import java.awt.event.MouseEvent;

import javax.swing.JPanel;

public class CustomTooltipPanel extends JPanel
{
  private static final long serialVersionUID = 5407622795457277282L;

  public interface Provider
  {
    public String getText(MouseEvent me);
  }
  
  private final Provider p;
  public CustomTooltipPanel(Provider p_)
  {
    p = p_;
  }
  
  public String getToolTipText(MouseEvent me)
  {
    return p.getText(me);    
  }
}
