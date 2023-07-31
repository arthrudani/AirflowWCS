package com.daifukuamerica.wrxj.swing;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


/**
 *  A class to handle text field based mouse events.  In order for the code
 *  to be portable between Unix and Windows for popup menus, three mouse event
 *  methods had to be overridden: <code>mousePressed</code>,
 *  <code>mouseReleased</code> and <code>mouseClicked</code>.
 *
 * @author       K.A.
 * @version      1.0   7/13/05
 * 
 */

public class SKDCTextFieldListener extends MouseAdapter implements SKDCGUIConstants
{
	  private SKDCTextField originatingTextField;
	  private SKDCPopupMenu skPopupMenu;

	 /**
	  *  Required constructor.
	  *  @param source the table originating the event.
	  */
	  public SKDCTextFieldListener(SKDCTextField sourceTextField)
	  {
	    originatingTextField = sourceTextField;
	  }

	 /**
	  *  {@inheritDoc}
	  */
	  @Override
    public void mouseClicked(MouseEvent mEvent)
	  {
	    if (skPopupMenu == null) skPopupMenu = definePopup();
	    if (skPopupMenu != null) showPopup(mEvent);
	  }

	 /**
	  *  {@inheritDoc}
	  */
	  @Override
    public void mousePressed(MouseEvent mEvent)
	  {
	    showPopup(mEvent);
	  }

	 /**
	  *  {@inheritDoc}
	  */
	  @Override
    public void mouseReleased(MouseEvent mEvent)
	  {
	    showPopup(mEvent);
	  }
		
	 /**
	  * Defines contents of a popup menu.
	  * @return @{link com.daifukuamerica.wrxj.tool.swing#SKDCPopupMenu SKDCPopupMenu} to be
	  *         used as a reference in this object.
	  */
	  public SKDCPopupMenu definePopup()
		{
			SKDCPopupMenu menu = new SKDCPopupMenu();
			menu.add("Paste", PASTE_BTN, new FieldPasteListener());
			menu.add("Copy", COPY_BTN, new FieldCopyListener());
			return menu;
		}
	/*==========================================================================
	                         PRIVATE METHODS SECTION
	  ==========================================================================*/
	  private void showPopup(MouseEvent mEvent)
	  {
	    if (mEvent.isPopupTrigger() && skPopupMenu != null)
	    {
	      skPopupMenu.show(originatingTextField, mEvent.getX(), mEvent.getY());
	    }
	  }
		

	 /**
	  * Auxiliary class for pasting text into a text field.
	  * @author karmstrong
	  * @version 1.0 7/13/05
	  */
		private class FieldPasteListener implements ActionListener{
			
			public void actionPerformed(ActionEvent e) {
				pasteButtonPressed();
			}
		 /**
		  * Paste Clipboard contents to textfield
		  *
		  */
			void pasteButtonPressed() {
				Transferable content = 
					Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);
				if(content != null && content.isDataFlavorSupported(DataFlavor.stringFlavor))
				{
					try {
						String text = (String)content.getTransferData(DataFlavor.stringFlavor);
						originatingTextField.setText(text);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		/**
		  * Auxiliary class for copying text from a text field.
		  * @author karmstrong
		  * @version 1.0 7/18/05
		  */
			private class FieldCopyListener implements ActionListener{
				
				public void actionPerformed(ActionEvent e) {
					copyButtonPressed();
				}
			 /**
			  * Copy cell contents to Clipboard
			  *
			  */
				void copyButtonPressed() {
					String text = originatingTextField.getText();
					StringSelection content = new StringSelection(text);
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents(content,content);
				}
			}
}
