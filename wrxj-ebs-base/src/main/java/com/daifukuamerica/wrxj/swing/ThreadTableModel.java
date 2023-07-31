package com.daifukuamerica.wrxj.swing;

import com.daifukuamerica.wrxj.util.UnreachableCodeException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

/**
 * Models thread information table.
 *
 * <p><b>Details:</b> <code>ThreadTableModel</code> is the Swing model for the
 * <code>JTable</code> rendered in <code>JavaRuntimeMonitorFrame</code> to
 * report thread information.  This model conveys information about each of the
 * threads in a separate row, one row per thread.</p>
 *
 * <p><code>ThreadTableModel</code> does not automatically update itself to
 * reflect changes in the current thread configuration.  In order to keep this
 * model current, you must call <code>poll()</code> from time to time.  Only
 * threads returned by <code>Thread.enumerate</code> are shown in the table.
 * Bear this in mind when deciding which thread will call this method, because
 * some threads may not be visible from other threads, depending on their thread
 * groups.</p>
 *
 * <p>The sorting of threads in this table model is stable.  Once a thread is
 * listed in a particular row in the table, it remains in that position until a
 * thread preceding it in the table dies and is deleted from the table.  Newly
 * discovered threads are always appended to the end of the table.  Thus,
 * threads at the top of the table have been around the longest.</p>
 *
 * <p>The columns are:</p>
 *
 * <ol>
 *   <li>row number, staring with 1</li>
 *   <li>thread name</li>
 *   <li>thread class name</li>
 *   <li>active ("yes") or not (""), as reported by
 *     <code>Thread.isActive()</code></li>
 * </ol>
 *
 * <p>This column specification is unstable and subject to change.  All cells
 * are returned as strings.</li>
 *
 * <p><b>Properties:</b></p>
 *
 * <ul>
 *   <li>{@link #setAggressiveUpdating(boolean) mzAggressiveUpdating}</li>
 * </ul>
 *
 * @author Sharky
 */
public final class ThreadTableModel extends AbstractTableModel
{
  private static final long serialVersionUID = 0L;


  /**
   * Represented thread list.
   *
   * <p><b>Details:</b> <code>mpThreads</code> is the current list of threads
   * represented in this table model.  Each thread corresponds to one row in the
   * table.  Threads are appended to and deleted from this list as
   * necessary.</p>
   */
  private final List<Thread> mpThreads = new ArrayList<Thread>();

  /**
   * Default constructor.
   *
   * <p><b>Details:</b> This constructor initializes a new instance with the
   * following default properties:</p>
   *
   * <table border>
   *   <tr>
   *     <th>property</th>
   *     <th>value</th>
   *   </tr>
   *   <tr>
   *     <td>{@link #setAggressiveUpdating(boolean) mzAggressiveUpdating}</td>
   *     <td><code>true</code></td>
   *   </tr>
   * </table>
   */
  public ThreadTableModel()
  {
  }

  /**
   * Aggressive updating property.
   *
   * @see #getAggressiveUpdating()
   * @see #setAggressiveUpdating(boolean)
   */
  private boolean mzAggressiveUpdating = true;

  /**
   * Reads mzAggressiveUpdating property.
   *
   * @return current value
   * 
   * @see #setAggressiveUpdating(boolean)
   */
  public boolean getAggressiveUpdating()
  {
    // Synchronization is not necessary for this getter.
    return mzAggressiveUpdating;
  }

  /**
   * Enables/disables aggressive updating.
   *
   * <p><b>Details:</b> Property <code>mzAggressiveUpdating</code> controls
   * whether or not table model listeners receive full table invalidations every
   * time <code>poll</code> is called.  This is the most accurate but also the
   * most expensive setting for modeling the threads.</p>
   *
   * <p>If you set this property to <code>false</code>, table model change
   * events will only receive change notifications when rows are appended or
   * deleted.  As a result, property changes in persistent threads, such as
   * changes in thread name, etc., may not be immediately observed by model
   * listeners.</p>
   * 
   * @param izAggressiveUpdating new value
   */
  public synchronized void setAggressiveUpdating(boolean izAggressiveUpdating)
  {
    mzAggressiveUpdating = izAggressiveUpdating;
  }

  /**
   * Updates current model from current thread configuration.
   *
   * <p><b>Details:</b> <code>poll</code> examines the current thread
   * configuration and updates the model to relect observed changes.  If a new
   * thread is discovered, it is appended to the end of the table.  If an old
   * thread has finished, it is deleted from the table.  In both of these cases,
   * update messages are sent to registered table model listeners.</p>
   */
  public synchronized void poll()
  {
    final Thread[] vppThreads = new Thread[Thread.activeCount()];
    final int vnLength = Thread.enumerate(vppThreads);
    for (int vnI = 0; vnI < vnLength; ++ vnI)
    {
      final Thread vpThread = vppThreads[vnI];
      if (! mpThreads.contains(vpThread))
        addRow(vpThread);
    }
    final List mpArrayAsList = Arrays.asList(vppThreads);
    for (int vnI = mpThreads.size() - 1; vnI >= 0; -- vnI)
    {
      Thread vpThread = mpThreads.get(vnI);
      if (! mpArrayAsList.contains(vpThread))
        removeRow(vnI);
    }
    if (mzAggressiveUpdating)
      fireTableChanged(new TableModelEvent(this));
  }

  /**
   * Appends row for thread.
   *
   * <p><b>Details:</b> <code>addRow</code> appends the given thread to both the
   * thread list and the table model and fires an appropriate table-<wbr>changed
   * event to this model's listeners.</p>
   *
   * @param ipThread thread to append
   */
  private synchronized void addRow(final Thread ipThread)
  {
    final int vnNewRowIndex = mpThreads.size();
    mpThreads.add(ipThread);
    if (! mzAggressiveUpdating)
      fireTableChanged(new TableModelEvent(this, vnNewRowIndex, vnNewRowIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
  }

  private Map<Integer, Integer> mpPendingRemovals = new HashMap<Integer, Integer>();
  
  /**
   * Deletes row by number.
   *
   * <p><b>Details:</b> <code>removeRow</code> deletes the thread corresponding
   * to the given row number from both the thread list and the table model and
   * fires an appropriate table-<wbr>changed event to this model's
   * listeners.</p>
   *
   * @param inRow thread to delete
   */
  private synchronized void removeRow(final int inRow)
  {
    Integer vpGrace = mpPendingRemovals.get(inRow);
    if (vpGrace == null)
      vpGrace = 5;
    else
      -- vpGrace;
    if (vpGrace <= 0)
    {
      mpPendingRemovals.remove(inRow);
      mpThreads.remove(inRow);
      if (! mzAggressiveUpdating)
        fireTableChanged(new TableModelEvent(ThreadTableModel.this, inRow, inRow, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE));
    }
    else
      mpPendingRemovals.put(inRow, vpGrace);
  }

  /**
   * Returns number of rows.
   *
   * <p><b>Details:</b> <code>getRowCount</code> satisfies the contract of
   * interface method <code>TableModel.getRowCount()</code> by returning the
   * number of rows in the current thread table.</p>
   *
   * @return number of rows
   */
  public synchronized int getRowCount()
  {
    return mpThreads.size();
  }

  /**
   * Column headings.
   *
   * <p><b>Details:</b> <code>gpsColumnNames</code> is the list of column
   * headings for this table model.  These values are publically accessible
   * through <code>getColumnName</code>.</p>
   */
  private static final String[] gpsColumnNames =
  { "#",
    "Name",
    "Class",
    "State"
  };

  /**
   * Returns number of columns.
   *
   * <p><b>Details:</b> <code>getColumnCount</code> satisifes the contract of
   * interface method <code>TableModel.getColumnCount</code> by returning the
   * number of columns in this thread table.  This number does not vary, and in
   * the current implementation the value returned is 4.</p>
   *
   * @return number of columns
   */
  public int getColumnCount()
  {
    return gpsColumnNames.length;
  }

  /**
   * Returns text for column heading.
   *
   * <p><b>Details:</b> <code>getColumnName</code> satisfies the contract of
   * interface method <code>TableModel.getColumnName(int)</code> by returning
   * the text that should be rendered in the column's header.</p>
   *
   * @param inCol column number
   * @return column name
   */
  @Override
  public String getColumnName(int inCol)
  {
    return gpsColumnNames[inCol];
  }

  /**
   * Returns text for column heading.
   *
   * <p><b>Details:</b> <code>getValueAt</code> satisfies the contract of
   * interface method <code>TableModel.getValueAt(int, int)</code> by returning
   * the text that should be rendered in the given row and column.</p>
   *
   * @param inRow cell's row number
   * @param inCol cell's column number
   * @return text in cell
   */
  public synchronized Object getValueAt(int inRow, int inCol)
  {
    if (inRow >= mpThreads.size())
      return "error";
    final Thread vpThread = mpThreads.get(inRow);
    switch (inCol)
    {
    case 0:
      return Integer.toString(inRow + 1);
    case 1:
      return vpThread.getName();
    case 2:
    {
      Class vpClass = vpThread.getClass();
      String vsName = vpClass.getName();
      int vnDot = vsName.lastIndexOf('.');
      String vsFirstPart = vsName.substring(vnDot + 1);
      String vsLastPart;
      if (vnDot < 0)
        vsLastPart = "";
      else
        vsLastPart = " (" + vsName.substring(0, vnDot) + ")";
      return vsFirstPart + vsLastPart;
    }
    case 3:
      return vpThread.getState();
    }
    throw new UnreachableCodeException();
  }

}

