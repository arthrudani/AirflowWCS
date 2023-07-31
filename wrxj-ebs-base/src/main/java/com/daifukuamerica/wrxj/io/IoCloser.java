/* 
 * Portions of this source code were developed independently by Charlton Rose 
 * and distributed on the web at http://sharkysoft.com/ under the product name 
 * "Lava."  Since then, this source code has been voluntarily adapted, by the 
 * author, for non-exclusive, unrestricted, license-free use in SK Daifuku and 
 * Daifuku products.
 */
package com.daifukuamerica.wrxj.io;

//import com.sharkysoft.html.HtmlParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Clean stream/file closing.
 *
 * <p><b>Details:</b> <code>IoCloser</code> contains static methods that allow
 * your to close your streams without catching or throwing exceptions.  To see
 * why this is useful, consider the following code:</p>
 *
  *<blockquote><pre>
    *FileInputStream vpIn = new FileInputStream(vsSomeFile);
    *<i>perform operations on stream;</i>
    *vpIn.close();
  *</pre></blockquote>
 *
 * <p>This code seems correct, unless the containing method is not allowed to
 * throw <code>IOException</code>s.  So we may have to enclose it in a try
 * block:</p>
 *
  *<blockquote><pre>
    *try
    *{
    *  FileInputStream vpIn = new FileInputStream(vsSomeFile);
    *  <i>perform operations on stream;</i>
    *  vpIn.close();
    *}
    *catch (IOException ve)
    *{
    *  <i>do something about it;</i>
    *}
  *</pre></blockquote>
 *
 * <p>That's a little better.  But now, what happens if an exception occurs
 * while the stream is in use?  <var>in</var> might never get closed!  Can we
 * just leave it open and wait for the garbage collector to deal with it?
 * Probably, but that's not a great idea.  Instead, we prefer that our code
 * cleans up after itself, so we make the following modifications:</p>
 *
  *<blockquote><pre>
    *FileInputStream vpIn = null;
    *try
    *{
    *  vpIn = new FileInputStream(vsSomeFile);
    *  perform operations on stream;
    *}
    *catch (IOException e)
    *{
    *  do something about it;
    *}
    *finally
    *{
    *  <font color=red>try
    *  {
    *    if (vpIn != null)</font>
    *      vpIn.close();
    *  <font color=red>}
    *  catch (IOException veIgnored)
    *  {
    *    // We don't really care that we couldn't close it.
    *    // so we won't do anything about it.
    *    // This exception is not very likely anyway.
    *  }</font>
    *}
  *</pre></blockquote>
 *
 * <p>Whoa!  What a mess!  Fortunately, the <code>close</code> methods in
 * <code>IoCloser</code> can spare us from some of this complexity (the
 * <font color=red>red</font> code):</p>
 *
  *<blockquote><pre>
    *FileInputStream vpIn = null;
    *try
    *{
    *  vpIn = new FileInputStream(vsSomeFile);
    *  <i>perform operations on stream;</i>
    *}
    *catch (IOException ve)
    *{
    *  <i>do something about it;</i>
    *}
    *finally
    *{
    *  IoCloser.close(vpIn);
    *}
    *</pre></blockquote>
 *
 * <p>Much cleaner!  In this case, <code>IoCloser</code> allowed us to replace 8
 * lines of code with 1!  (Assuming, of course, that you format your code the
 * way we do -- the <em>correct</em> way... :-)</p>
 *
 * @since 1998.10.11
 * @author Sharky
 */
public final class IoCloser
{

	private IoCloser()
	{
		throw new InstantiationError();
	}

	/**
	 * Closes Writer without throwing exception.
	 *
	 * <p><b>Details:</b>  This method allows you to close a <code>Writer</code>
	 * without worrying about trapping the possible exception.  (This usually
	 * results in cleaner code.)  If an exception occurs, it is returned instead
	 * of thrown.  If no exception occurs, <code>null</code> is returned.</p>
	 *
	 * <p>No action is taken and <code>null</code> is returned if
	 * <var>writer</var> is <code>null</code>.</p>
	 *
	 * @param writer the Writer to close
	 * @return the exception that occurred, if any
	 *
	 * @since 1998.11.05
	 */
	public static IOException close(Writer writer)
	{
		if (writer == null)
			return null;
		try
		{
			writer.close();
		}
		catch (IOException e)
		{
			return e;
		}
		return null;
	}

	/**
	 * Closes Reader without throwing exception.
	 *
	 * <p><b>Details:</b>  This method allows you to close a <code>Reader</code>
	 * without worrying about trapping the possible exception.  (This usually
	 * results in cleaner code.)  If an exception occurs, it is returned instead
	 * of thrown.  If no exception occurs, <code>null</code> is returned.</p>
	 *
	 * <p>No action is taken and <code>null</code> is returned if
	 * <var>reader</var> is <code>null</code>.</p>
	 *
	 * @param reader the Reader to close
	 * @return the exception that occurred, if any
	 *
	 * @since 1998.10.11
	 */
	public static IOException close(Reader reader)
	{
		if (reader == null)
			return null;
		try
		{
			reader.close();
		}
		catch (IOException e)
		{
			return e;
		}
		return null;
	}

	/**
	 * Closes InputStream without throwing exception.
	 *
	 * <p><b>Details:</b>  This method allows you to close an
	 * <code>InputStream</code> without worrying about trapping the possible
	 * exception.  (This usually results in cleaner code.)  If an exception
	 * occurs, it is returned instead of thrown.  If no exception occurs,
	 * <code>null</code> is returned.</p>
	 *
	 * <p>No action is taken and <code>null</code> is returned if <var>in</var> is
	 * <code>null</code>.</p>
	 *
	 * @param in the InputStream to close
	 * @return the exception that occurred, if any
	 *
	 * @since 1999.03.09
	 */
	public static IOException close(InputStream in)
	{
		if (in == null)
			return null;
		try
		{
			in.close();
		}
		catch (IOException e)
		{
			return e;
		}
		return null;
	}

	/**
	 * Closes OutputStream without throwing exception.
	 *
	 * <p><b>Details:</b>  This method allows you to close an
	 * <code>OutputStream</code> without worrying about trapping the possible
	 * exception.  (This usually results in cleaner code.)  If an exception
	 * occurs, it is returned instead of thrown.  If no exception occurs,
	 * <code>null</code> is returned.</p>
	 *
	 * <p>No action is taken and <code>null</code> is returned if <var>out</var>
	 * is <code>null</code>.</p>
	 *
	 * @param out the OutputStream to close
	 * @return the exception that occurred, if any
	 *
	 * @since 1999.12.09
	 */
	public static IOException close(OutputStream out)
	{
		if (out == null)
			return null;
		try
		{
			out.close();
		}
		catch (IOException e)
		{
			return e;
		}
		return null;
	}

	/**
	 * Closes RandomAccessFile without throwing exception.
	 *
	 * <p><b>Details:</b>  This method allows you to close a
	 * <code>RandomAccessFile</code> without worrying about trapping the possible
	 * exception.  (This usually results in cleaner code.)  If an exception
	 * occurs, it is returned instead of thrown.  If no exception occurs,
	 * <code>null</code> is returned.</p>
	 *
	 * <p>No action is taken and <code>null</code> is returned if <var>raf</var>
	 * is <code>null</code>.</p>
	 *
	 * @param raf the RandomAccessFile to close
	 * @return the exception that occurred, if any
	 *
	 * @since 2000.01.29
	 */
	public static IOException close(RandomAccessFile raf)
	{
		if (raf == null)
			return null;
		try
		{
			raf.close();
		}
		catch (IOException e)
		{
			return e;
		}
		return null;
	}

	//  /**
	//   * Closes HtmlParser without throwing exception.
	//   *
	//   * <p><b>Details:</b>  This method allows you to close an
	//   * <code>HtmlParser</code> without worrying about trapping the possible
	//   * exception.  (This usually results in cleaner code.)  If an exception
	//   * occurs, it is returned instead of thrown.  If no exception occurs,
	//   * <code>null</code> is returned.</p>
	//   *
	//   * <p>No action is taken and <code>null</code> is returned if
	//   * <var>parser</var> is <code>null</code>.</p>
	//   *
	//   * @param parser the HtmlParser to close
	//   * @return the exception that occurred, if any
	//   *
	//   * @since 2000.07.08
	//   */
	//  public static IOException close(HtmlParser parser)
	//  {
	//    if (parser == null)
	//      return null;
	//    try
	//    {
	//      parser.close();
	//    }
	//    catch (IOException e)
	//    {
	//      return e;
	//    }
	//    return null;
	//  }

	/**
	 * Closes Socket without throwing exception.
	 *
	 * <p><b>Details:</b>  This method allows you to close a <code>Socket</code>
	 * without worrying about trapping the possible exception.  (This usually
	 * results in cleaner code.)  If an exception occurs, it is returned instead
	 * of thrown.  If no exception occurs, <code>null</code> is returned.</p>
	 *
	 * <p>No action is taken and <code>null</code> is returned if
	 * <var>socket</var> is <code>null</code>.</p>
	 *
	 * @param socket the Socket to close
	 * @return the exception that occurred, if any
	 *
	 * @since 2000.11.05
	 */
	public static IOException close(Socket socket)
	{
		if (socket == null)
			return null;
		try
		{
			socket.close();
		}
		catch (IOException e)
		{
			return e;
		}
		return null;
	}

	/**
	 * Closes ServerSocket without throwing exception.
	 *
	 * <p><b>Details:</b>  This method allows you to close a
	 * <code>ServerSocket</code> without worrying about trapping the possible
	 * exception.  (This usually results in cleaner code.)  If an exception
	 * occurs, it is returned instead of thrown.  If no exception occurs,
	 * <code>null</code> is returned.</p>
	 *
	 * <p>No action is taken and <code>null</code> is returned if
	 * <var>socket</var> is <code>null</code>.</p>
	 *
	 * @param socket the ServerSocket to close
	 * @return the exception that occurred, if any
	 *
	 * @since 2000.11.05
	 */
	public static IOException close(ServerSocket socket)
	{
		if (socket == null)
			return null;
		try
		{
			socket.close();
		}
		catch (IOException e)
		{
			return e;
		}
		return null;
	}

}

