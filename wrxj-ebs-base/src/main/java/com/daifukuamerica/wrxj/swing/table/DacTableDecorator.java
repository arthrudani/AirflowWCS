package com.daifukuamerica.wrxj.swing.table;

import com.daifukuamerica.wrxj.jdbc.ColumnObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 *  Decorator class to help sort table data based on which column is selected.
 *
 *  @author       A.D.
 *  @version      1.0
 *  <BR>Created: 04-Mar-03<BR>
 *      Copyright (c) 2003<BR>
 *      Company:  Daifuku America Corporation
 */
public class DacTableDecorator implements TableModel
{
  public static int ASCENDING = 110;
  public static int DESCENDING = 111;
  private DacModel actualModel;
  private int[] sortIndex;
  private int   sortOrder;
  private int column = -1;

  public DacTableDecorator(DacModel model)
  {
    actualModel = model;
    actualModel.addTableModelListener(new TableModelListener()
    {
      public void tableChanged(TableModelEvent ev)
      {
        resetSortIndex();
      }
    });
    resetSortIndex();
  }

  public int getRowCount()
  {
    return(actualModel.getRowCount());
  }

  public int getColumnCount()
  {
    return(actualModel.getColumnCount());
  }

  public Object getValueAt(int inRow, int inColumn)
  {
    return(actualModel.getValueAt(sortIndex[inRow], inColumn));
  }

  public String getDBColumnName(int inViewColumn)
  {
    return(actualModel.getDBColumnName(inViewColumn));
  }

  public String getColumnName(int col)
  {
    return(actualModel.getColumnName(col));
  }
  
  public int getViewColumnIndex(String isColumnName)
  {
    return(actualModel.dbColumnNameToViewIndex(isColumnName));
  }

  /**
   * Set the value to display for a bad translation
   * 
   * @param isNoTranslation
   */
  public void setNoTranslationDisplay(String isNoTranslation)
  {
    actualModel.setNoTranslationDisplay(isNoTranslation);
  }
  
  public void setNonEditableRows(List<Integer> ipNonEditableRows)
  {
    actualModel.setNonEditableRows(ipNonEditableRows);
  }

  public void setEditableColumns(List<Integer> ipEditableColumns)
  {
    actualModel.setEditableColumns(ipEditableColumns);
  }

  public Class getColumnClass(int columnIndex)
  {
    return(actualModel.getColumnClass(columnIndex));
  }

  public void setValueAt(Object obj, int row, int col)
  {
    actualModel.setValueAt(obj, sortIndex[row], col);
  }

  public boolean isCellEditable(int row, int col)
  {
    if (row < 0)
    {
      return false;
    }
    return(actualModel.isCellEditable(sortIndex[row], col));
  }

  public void addTableModelListener(TableModelListener lsnr)
  {
    actualModel.addTableModelListener(lsnr);
  }

  public void removeTableModelListener(TableModelListener lsnr)
  {
    actualModel.removeTableModelListener(lsnr);
  }

  public void addRow(Map tmap)
  {
    actualModel.addRow(tmap);
  }

  public void addRow(ColumnObject[] newData)
  {
    actualModel.addRow(newData);
  }

  public void setModelRow(ColumnObject[] newData, int row)
  {
    actualModel.setModelRow(newData, sortIndex[row]);
  }

  public void setModelRow(Map newData, int row)
  {
    actualModel.setModelRow(newData, sortIndex[row]);
  }

  public void removeRow(int row)
  {
    actualModel.removeRow(sortIndex[row]);
  }

  public Map<String,Object> getRowData(int row)
  {
    return(actualModel.getRowData(sortIndex[row]));
  }

 /**
  * Returns all data in this model.  This is useful in case the model has been 
  * updated but the database has not, and we need the model data for local changes.
  * 
  * @return List&lt;Map&gt; of model data.
  */
  public List<Map> getTableData()
  {
    return(actualModel.getTableData());
  }

  public void resetData(List newData)
  {
    actualModel.resetData(newData);
    if (column != -1)
    {
      int old = column;
      column = -1;
      sort(old, sortOrder);
    }
  }
  
  public void refreshData(int inBeginRow, int inEndRow)
  {
    actualModel.refreshData(inBeginRow, inEndRow);
  }

  public int[] getColumnWidths()
  {
    return(actualModel.getColumnWidths());
  }

  public void clearTable()
  {
    actualModel.clearTable();
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
 public void sort(int iiColumn, int iiSortOrder)
  {
    sortOrder = iiSortOrder;
    if (column != iiColumn)
    {
      //
      // This column needs to be sorted.
      //
      column = iiColumn;
      //
      Map vpUniqueObjectMap = getUniqueObjectMap();
      if (vpUniqueObjectMap.size() < 200)
      {
        //
        // We have a limited number of unique Objects.  Use our map to fill
        // the sort index.
        //
        fillSortIndex(vpUniqueObjectMap, iiSortOrder);
      }
      else
      {
        shuffleSortIndex();
        quickSort(0, sortIndex.length-1);
      }
      vpUniqueObjectMap.clear();
    }
    else
    {
      //
      // The column to sort has not changed, so we are toggling the sort order.
      // Just reverse the sort index order.
      //
      int viSize = sortIndex.length;
      int viSwapCount = viSize / 2;
      for (int i = 0, j = viSize - 1; i < viSwapCount; i++, j--)
      {
        int viTemp = sortIndex[i];
        sortIndex[i] = sortIndex[j];
        sortIndex[j] = viTemp;
      }
    }
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Fill the sortIndex array using the ordered TreeMap ipUniqueObjectMap.
   * @param ipUniqueObjectMap
   * @param iiSortOrder
   */
  private void fillSortIndex(Map ipUniqueObjectMap, int iiSortOrder)
  {
    if (iiSortOrder == DacTableDecorator.ASCENDING)
    {
      int viIndex = 0;
      List[] vpIndexListArray = new List[ipUniqueObjectMap.size()];
      ipUniqueObjectMap.values().toArray(vpIndexListArray);
      int viMapSize = ipUniqueObjectMap.size();
      for (int i = 0; i < viMapSize; i++)
      {
        List vpIndexList = vpIndexListArray[i];
        int viListSize = vpIndexList.size();
        for (int j = 0; j < viListSize; j++)
        {
          Integer vnIndex = (Integer)vpIndexList.get(j);
          int viObjectIndex = vnIndex.intValue();
          sortIndex[viIndex] = viObjectIndex;
          viIndex++;
        }
        
      }
    } else
    {
      //int viIndex = sortIndex.length-1;
      int viIndex = 0;
      List[] vpIndexListArray = new List[ipUniqueObjectMap.size()];
      ipUniqueObjectMap.values().toArray(vpIndexListArray);
      int viMapSize = ipUniqueObjectMap.size();
      for (int i = viMapSize - 1; i >= 0; i--)
      {
        List vpIndexList = vpIndexListArray[i];
        int viListSize = vpIndexList.size();
        for (int j = viListSize - 1; j >= 0; j--)
        {
          Integer vnIndex = (Integer)vpIndexList.get(j);
          int viObjectIndex = vnIndex.intValue();
          sortIndex[viIndex] = viObjectIndex;
          //viIndex--;
          viIndex++;
        }
      }
    }
  }

  /*--------------------------------------------------------------------------*/
   private Map getUniqueObjectMap()
   {
    Map vpObjectMap = new TreeMap();
    int viUniqueObjectCount = 0;
    int viSize = sortIndex.length;
    for (int i = 0; ((i < viSize) && (viUniqueObjectCount <= 200)); i++)
    {
      Object vpKey = actualModel.getValueAt(i, column);
      if (vpKey != null)
      {
        if (vpObjectMap.containsKey(vpKey))
        {
          List vpIndexList =  (List)vpObjectMap.get(vpKey);
          Integer vnIndex = Integer.valueOf(i);
          vpIndexList.add(vnIndex);
        }
        else
        {
          List vpIndexList = new ArrayList();
          Integer vnIndex = Integer.valueOf(i);
          vpIndexList.add(vnIndex);
          vpObjectMap.put(vpKey, vpIndexList);
          viUniqueObjectCount++;
        }
      }
      else
      {
        viSize = i;
        break;
      }
    }
     return vpObjectMap;
   }

  /*--------------------------------------------------------------------------*/
  /**
   * @return  a negative integer, zero, or a positive integer as this object
   *    is less than, equal to, or greater than the specified object.
   */
  private int compare(int i, int j)
  {
    Object firstCellObj = actualModel.getValueAt(sortIndex[i], column);
    Object secondCellObj = actualModel.getValueAt(sortIndex[j], column);
    if ((firstCellObj == null) || (secondCellObj == null))
    {
      if ((firstCellObj == null) && (secondCellObj == null))
        return(0);
      else if (firstCellObj == null)
        return(-1);
      else if (secondCellObj == null)
        return(1);
    }
    int cmp;
    if ((firstCellObj instanceof String) && (secondCellObj instanceof String))
      cmp = ((String)firstCellObj).compareTo((String)secondCellObj);
    else if (firstCellObj instanceof Comparable)
      cmp = ((Comparable)firstCellObj).compareTo(secondCellObj);
    else
      cmp = firstCellObj.toString().compareTo(secondCellObj.toString());
    return cmp;
  }

  /*--------------------------------------------------------------------------*/
  private void swap(int i, int j)
  {
    int tempVar = sortIndex[i];
    sortIndex[i] = sortIndex[j];
    sortIndex[j] = tempVar;
  }

  /*--------------------------------------------------------------------------*/
  /**
   * Quicksort is at iits worst sorting pre-Sorted data, so to ensure "random"
   * data, scramble the data before we do the sort.
   */
  void shuffleSortIndex()
  {
    resetSortIndex();
    int viSize = sortIndex.length;
    Random vpRandom = new Random();
    for (int i = viSize - 1; i >= 0; i--)
    {
      int j = vpRandom.nextInt(viSize);
      int temp = sortIndex[i];
      sortIndex[i] = sortIndex[j];
      sortIndex[j] = temp;
    }
  }
  
  /*--------------------------------------------------------------------------*/
  void resetSortIndex()
  {
    sortIndex = createSortIndex();
  }
  
  private int[] createSortIndex()
  {
    int viArraySize = getRowCount();
    int[] vpArray = null;
    if (viArraySize == 0)
    {
      vpArray = new int[1];
      vpArray[0] = 0;
    }
    else
    {
      vpArray = new int[viArraySize];
      for(int i = 0; i < viArraySize; i++)
      {
        vpArray[i] = i;
      } 
    }
    return vpArray;
  }

  /*--------------------------------------------------------------------------*/
  /*--------------------------------------------------------------------------*/
  /** This is a generic version of C.A.R Hoare's Quick Sort 
   * algorithm.  This will handle arrays that are already
   * sorted, and arrays with duplicate keys.
   *
   * If you think of a one dimensional array as going from
   * the lowest index on the left to the highest index on the right
   * then the parameters to this function are lowest index or
   * left and highest index or right.  The first time you call
   * this function it will be with the parameters 0, a.length - 1.
   *
   * @param lo0     left boundary of array partition
   * @param hi0     right boundary of array partition
   */
  private void quickSort(int lo0, int hi0)
  {
    //
    // Use two specialized quicksort routines to save the cost of an
    // ascending/descending test for every compare.
    //
    if (sortOrder == DacTableDecorator.ASCENDING)
    {
      ascendingQuickSort(lo0, hi0);
    }
    else
    {
      decendingQuickSort(lo0, hi0);
    }
  }
  
  /*--------------------------------------------------------------------------*/
  private void ascendingQuickSort(int lo0, int hi0)
  {
    if (lo0 >= hi0)
    {
        return;
    }
    else if (lo0 == (hi0 - 1))
    {
      if (compare(lo0, hi0) > 0)
      {
        swap(lo0, hi0);
      }
      return;
    }
    int lo = lo0;
    int hi = hi0;
    int mid;
    mid = lo;
    while(true)
    {
      /* find the first element that is greater than
       * the partition element starting from the left Index.
       */
      while( ( ++lo < hi0 ) && (compare(lo, mid) <= 0) )
        ;
      /* find an element that is smaller than or equal to
       * the partition element starting from the right Index.
       */
      while( (hi > lo0 ) && (compare(hi, mid) > 0) )
        hi--;
      // if the indexes have not crossed, swap
      if( lo <= hi ) 
      {
        if (lo < hi)
        {
          swap(lo, hi);
        }
      }
      else
      {
        if (mid != hi)
        {
          swap(mid, hi);
          mid = hi;
        }
        break;
      }
    }
    if (lo0 < mid - 1)
    {
      ascendingQuickSort(lo0, mid - 1);
    }
    if (mid + 1 < hi0)
    {
      ascendingQuickSort(mid + 1, hi0);
    }
  }

  /*--------------------------------------------------------------------------*/
  private void decendingQuickSort(int lo0, int hi0)
  {
    if (lo0 >= hi0)
    {
        return;
    }
    else if (lo0 == (hi0 - 1))
    {
      if (compare(lo0, hi0) < 0)
      {
        swap(lo0, hi0);
      }
      return;
    }
    int lo = lo0;
    int hi = hi0;
    int mid;
    mid = lo;
    while(true)
    {
      /* find the first element that is smaller than or equal to
       * the partition element starting from the left Index.
       */
      while( ( ++lo < hi0 ) && (compare(lo, mid) > 0) )
        ;
      /* find an element that is greater than
       * the partition element starting from the right Index.
       */
      while( (hi > lo0 ) && (compare(hi, mid) <= 0) )
        hi--;
      // if the indexes have not crossed, swap
      if( lo <= hi ) 
      {
        if (lo < hi)
        {
          swap(lo, hi);
        }
      }
      else
      {
        if (mid != hi)
        {
          swap(mid, hi);
          mid = hi;
        }
        break;
      }
    }
    if (lo0 < mid - 1)
    {
      decendingQuickSort(lo0, mid - 1);
    }
    if (mid + 1 < hi0)
    {
      decendingQuickSort(mid + 1, hi0);
    }
  }
}
