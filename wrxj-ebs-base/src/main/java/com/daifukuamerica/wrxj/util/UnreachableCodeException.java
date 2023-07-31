/* 
 * Portions of this source code were developed independently by Charlton Rose 
 * and distributed on the web at http://sharkysoft.com/ under the product name 
 * "Lava."  Since then, this source code has been voluntarily adapted, by the 
 * author, for non-exclusive, unrestricted, license-free use in SK Daifuku and 
 * Daifuku products.
 */
package com.daifukuamerica.wrxj.util;

/**
 * Indicates unanticipated execution path.
 *
 * <p><b>Details:</b> An <code>UnreachableCodeException</code> may be thrown
 * whenever an execution path that the programmer has assumed to be unreachable
 * actually turns out to be reachable.  In a perfect world, therefore, this
 * exception will never be thrown.  However, since no programmers (other than
 * Sharkysoft programmers) are perfect, we have provided this exception class
 * for you to use wherever you assert that some piece of code is unreachable.
 * Later, when this exception is finally thrown, you will realize how naive you
 * were and quickly fix the problem.  As embarrassing as it is, however, it is
 * better to throw an <code>UnreachableCodeException</code> than to do nothing
 * at all and risk leaving the bug undetected!</p>
 *
 * <blockquote>
 *
 *   <p><b>Example 1:</b></p>
 *
 *   <p>Your code <code>switch</code>es on the variable <code>vnShape</code>.
 *   You know, in this particular <code>switch</code> statement, that
 *   <var>vnShape</var> can be only one of three values.  (Or at least that's
 *   what you <em>think</em> you know!)  However, since you are
 *   safety-<wbr>conscious, your code might look like this:</p>
 *
    *<blockquote><pre>
      *int vnEdges;
      *switch (vnShape)
      *{
      *case TRIANGLE:
      *  vnEdges = 3;
      *  break;
      *case SQUARE:
      *  vnEdges = 4;
      *  break;
      *case PENTAGON:
      *  vnEdges = 5;
      *  break;
      *default:
      *  // <i>Because we are only expecting one of the above three</i>
      *  // <i>shapes, we'll be really surprised if we actually get here!</i>
      *  <b>throw new UnreachableCodeException("Surprise, you moron!");</b>
      *}
      *return vnEdges;
    *</pre></blockquote>
 *
 *   <p>In this example, adding the unreachable <code>default</code> case
 *   accomplishes two good things:</p>
 *
 *   <ol>
 *     <li>It guarantees that you'll be notified if your assumption is wrong,
 *       and</li>
 *     <li>It allows your code to compile, without the compiler complaining that
 *       <code>vnEdges</code> might not have been initialized.  (You see,
 *       compilers just aren't as smart as humans!)</li>
 *   </ol>
 *
 * </blockquote>
 *
 * <p>As the above example demonstrates, <code>UnreachableCodeException</code>
 * not only documents your control flow assumptions, but it also helps you to
 * fool the compiler.  Such deception is often necessary in the presence of
 * unreachable code that the compiler can't guess about.</p>
 *
 * <blockquote>
 *
 *   <p><b>Example 2:</b></p>
 *
 *   <p>The function <code>foo</code> divides a value by 2 if the value is even,
 *   but terminates the application if the value is odd.</p>
 *
    *<blockquote><pre>
      *int foo(int vn)
      *{ if (vn % 2 == 0)
      *    return vn / 2;
      *  System.exit (0);
      *  // <i>exit() will never return, but does the compiler know that?</i>
      *  // <i>No!  It will innocently complain about needing a return value</i>
      *  // <i>right here.</i>
      *}
    *</pre></blockquote>
 *
 *   <p>Although this code looks correct (because <code>exit</code> never
 *   returns), the compiler will not accept it.  This is because the compiler
 *   does not know anything about <code>System.exit</code>'s behavior.  Instead,
 *   it stupidly expects <code>exit</code> to return, and therefore complains
 *   about a missing return value for <code>foo</code>.
 *   <code>UnreachableCodeException</code> offers a good solution here.</p>
 *
    *<blockquote><pre>
      *int foo (int vn)
      *{ if (vn % 2 == 0)
      *    return vn / 2;
      *  System.exit (0);
      *  <b>throw new com.sharkysoft.util.UnreachableCodeException ();</b>
      *}
    *</pre></blockquote>
 *
 *   <p>Now the compiler will now accept our code without complaint.  Although
 *   you could have inserted a dummy <code>return</code> statement into the code
 *   -- like many programmers do -- the better approach shown here renders the
 *   code more "self-documenting," and it also guarantees that you'll be
 *   notified if your assumption about the never-<wbr>returning method turns out
 *   to be <code>incorrect</code> (for some strange reason).</p>
 *
 * </blockquote>
 *
 * @author Sharky
 */
public class UnreachableCodeException extends RuntimeException
{
  private static final long serialVersionUID = 0L;

  /**
   * Initializes without message.
   *
   * <p><b>Details:</b>  This default constructor initializes a new
   * <code>UnreachableCodeException</code> without an exception message.</p>
   */
  public UnreachableCodeException() {}

  /**
   * Initializes with message.
   *
   * <p><b>Details:</b>  This constructor initializes a new
   * <code>UnreachableCodeException</code> with the given exception detail
   * message.</p>
   *
   * @param isDetail the messsage
   */
  public UnreachableCodeException(String isDetail) {super(isDetail);}

  /**
   * Initializes with exception.
   *
   * <p><b>Details:</b> This constructor initializes a new instance with the
   * supplied cause.  This constructor is useful in <code>catch</code> blocks
   * where an exception must be caught but is never anticipated.</p>
   *
   * @param ieCause surprise exception
   */
  public UnreachableCodeException(final Throwable ieCause) {super(ieCause);}

}

