package com.daifukuamerica.wrxj.swing;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;

public class UndoEnabledTextArea extends JTextArea
{
  private static final long serialVersionUID = -6365950018967142372L;

  private UndoManager mpUndoManager;

  /*========================================================================*/
  /* Constructors                                                           */
  /*========================================================================*/
  /**
   * Default Constructor
   */
  public UndoEnabledTextArea()
  {
    addUndo();
  }

  /**
   * Constructor
   * 
   * @param text
   */
  public UndoEnabledTextArea(String text)
  {
    super(text);
    addUndo();
  }

  /**
   * Constructor
   * 
   * @param doc
   */
  public UndoEnabledTextArea(Document doc)
  {
    super(doc);
    addUndo();
  }

  /**
   * Constructor
   * 
   * @param rows
   * @param columns
   */
  public UndoEnabledTextArea(int rows, int columns)
  {
    super(rows, columns);
    addUndo();
  }

  /**
   * Constructor
   * 
   * @param text
   * @param rows
   * @param columns
   */
  public UndoEnabledTextArea(String text, int rows, int columns)
  {
    super(text, rows, columns);
    addUndo();
  }

  /**
   * Constructor
   * 
   * @param doc
   * @param text
   * @param rows
   * @param columns
   */
  public UndoEnabledTextArea(Document doc, String text, int rows, int columns)
  {
    super(doc, text, rows, columns);
    addUndo();
  }
  
  /*========================================================================*/
  /* Undo                                                                   */
  /*========================================================================*/

  /**
   * Add undo capability to the JTextArea
   */
  @SuppressWarnings("serial")
  private void addUndo()
  {
    mpUndoManager = new UndoManager();
    getDocument().addUndoableEditListener(new UndoableEditListener() {
      @Override
      public void undoableEditHappened(UndoableEditEvent e)
      {
        mpUndoManager.addEdit(e.getEdit());
      }
    });

    InputMap im = getInputMap(JComponent.WHEN_FOCUSED);
    ActionMap am = getActionMap();

    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Undo");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "Redo");

    am.put("Undo", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        if (mpUndoManager.canUndo())
        {
          mpUndoManager.undo();
        }
      }
    });
    am.put("Redo", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        if (mpUndoManager.canRedo())
        {
          mpUndoManager.redo();
        }
      }
    });
  }
}
