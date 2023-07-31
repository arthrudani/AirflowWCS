package com.daifukuamerica.wrxj.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Reads properties from file.
 * 
 * <p><b>Details:</b> This class implements {@link PropertiesLayer} using 
 * properties read from a standard Java properties file.  The properties file is
 * read when the instance is created, and if the reread option is enabled, the
 * file is reread every time a change is detected.</p>
 * 
 * @author Sharky
 */
public final class PropertiesFileLayer implements PropertiesLayer
{

  /**
   * Reread file after change.
   * 
   * <p><b>Details:</b> This field indicates whether the properties file should 
   * be reread whenever a modification is detected.  If <code>true</code>, the
   * file will be reread.</p>
   */
  private final boolean mzReread;
  
  /**
   * Properties file.
   * 
   * <p><b>Details:</b> This field is the properties file processed by this 
   * properties layer.</p>
   */
  private final File mpFile;

  /**
   * Last known modified date of file.
   * 
   * <p><b>Details:</b> This field is the last known modified date of the 
   * file.  It is compared against the file's current modified date whenever
   * properties are request, if the reread option is enabled.</p>
   */
  private long mlLastModified;
  
  /**
   * Cached properties.
   *  
   * <p><b>Details:</b> This field caches the properties parsed from the 
   * properties file.  Before reading from or writing to this structure, locks 
   * must be acquired.  The locks for reading and writing are, respectively, 
   * {@link #mpReadLock} and {@link #mpWriteLock}.  A thread should not attempt 
   * to acquire a write lock while holding a read lock.  However, the reverse is 
   * acceptable; a thread may acquire a read lock while holding a write lock.  
   * All locks must be explicitly acquired and released.</p>
   * 
   * <p>The purpose of this locking mechanism is to allow thread-<wbr>safe 
   * concurrent read accesses without imposing the overhead of Java monitors. 
   * Using this locking technique, Java monitors will only be required when 
   * there is a conflicts between concurrent read and write operations.</p>
   */
  private final Properties mpProperties = new Properties();
  
  /**
   * Read lock for properties cache.
   * 
   * <p><b>Details:</b> This field is the read lock that must be acquired before 
   * reading from {@link #mpProperties}.</p>
   */
  private final ReentrantReadWriteLock.ReadLock mpReadLock;

  /**
   * Write lock for properties cache.
   * 
   * <p><b>Details:</b> This field is the write lock that must be acquired 
   * before writing to {@link #mpProperties}.</p>
   */
  private final ReentrantReadWriteLock.WriteLock mpWriteLock;
  
  /**
   * Reads and caches properties.
   * 
   * <p><b>Details:</b> This constructor opens, parses properties from, and 
   * closes the given properties file.  An optional "reread" parameter specifies 
   * whether the properties file should be reprocessed when a change is 
   * detected.</p>
   * 
   * @param isFile the properties file path
   * @param izReread true iff file should be reread on change
   * @throws IOException if an I/O error occurs while processing the file
   */
  public PropertiesFileLayer(String isFile, boolean izReread) throws IOException
  {
    mpFile = new File(isFile);
    mzReread = izReread;
    mlLastModified = mpFile.lastModified();
    loadProperties();
    ReentrantReadWriteLock vpRwl = new ReentrantReadWriteLock();
    mpReadLock = vpRwl.readLock();
    mpWriteLock = vpRwl.writeLock();
  }

  /**
   * Reads and caches properties.
   * 
   * <p><b>Details:</b> This constructor delegates to 
   * {@link #PropertiesFileLayer(String, boolean)}, setting the reread parameter 
   * to <code>true</code>.</p>
   * 
   * @param isFile the properties file path
   * @throws IOException if an I/O error occurs while processing the file
   */
  public PropertiesFileLayer(String isFile) throws IOException
  {
    this(isFile, true);
  }

  /**
   * Conditionally rereads properties file.
   * 
   * <p><b>Details:</b> This method rereads the properties file if two 
   * conditions are met:</p>
   * 
   * <ol>
   *   <li>the reread option is enabled</li>
   *   <li>the properties file appears to have been modified since the last time 
   *     it was read</li>
   * </ol>
   * 
   * <p>If an attempt is made to reread the file but the operation fails, the 
   * properties cached after the previous read will be lost, and this layer will 
   * behave as if no properties are defined at all.</p>
   * 
   * <p>Threads calling this method are expected to have already acquired 
   * exactly one read lock (via {@link #mpReadLock}).</p>
   */
  private void rereadIfNeeded()
  {
    if (! mzReread)
      return;
    long vlLastModified = mpFile.lastModified();
    if (vlLastModified == mlLastModified)
      return;
    mpReadLock.unlock();
    mpWriteLock.lock();
    try
    {
      mlLastModified = vlLastModified;
      mpProperties.clear();
      loadProperties();
    }
    catch (IOException ex)
    {
    }
    finally
    {
      mpReadLock.lock();
      mpWriteLock.unlock();
    }
  }

  /**
   * Loads properties.
   * 
   * <p><b>Details:</b> This method reads the properties file and caches the
   * parsed values.</p>
   * 
   * @throws IOException if an I/O error occurs
   */
  private void loadProperties() throws IOException
  {
    FileInputStream vpInputStream = new FileInputStream(mpFile);
    mpProperties.load(vpInputStream);
    vpInputStream.close();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public String getProperty(String isName)
  {
    mpReadLock.lock();
    try
    {
      rereadIfNeeded();
      return mpProperties.getProperty(isName);
    }
    finally
    {
      mpReadLock.unlock();
    }    
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<String> getPropertyNames(String isPrefix)
  {
    mpReadLock.lock();
    try
    {
      rereadIfNeeded();
      Set<String> vpAllNames = getKeySet(mpProperties);
      Set<String> vpMatchingNames = PropertiesStack.selectByPrefix(vpAllNames, isPrefix);
      return vpMatchingNames;
    }
    finally
    {
      mpReadLock.unlock();
    }
  }
  
  /**
   * {@inheritDoc}
   * 
   * <p><i>Currently unsupported by this implementation.</i></p>
   */ 
  @Override
  public void refresh()
  {
  }

  /**
   * Extracts name set from Properties.
   * 
   * <p><b>Details:</b> This method extracts the names of all properties from
   * the given {@link Properties} and returns them in an unordered set.</p>
   * 
   * @param ipProperties the properties
   * @return the names
   */
  static Set<String> getKeySet(Properties ipProperties)
  {
    int vnSize = ipProperties.size();
    Set<String> vpSet = new HashSet<String>(vnSize);
    for (Object vpKey: ipProperties.keySet())
      vpSet.add((String) vpKey);
    return vpSet;
  }
  
}

