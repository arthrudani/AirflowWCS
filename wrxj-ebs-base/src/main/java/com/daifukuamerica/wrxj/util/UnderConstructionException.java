package com.daifukuamerica.wrxj.util;

/**
 * Indicates an unfinished execution path.
 *
 * <p><b>Details:</b> A <code>UnderConstructionException</code> is thrown when a
 * method is called, a case invoked, or a feature is requested that has not yet
 * been implemented.  This class is the extreme programmer's best friend; it
 * allows the developer to compile and execute half-<wbr>baked code while
 * minimizing the risk of forgetting to finish it later.  More importantly, this
 * class allows you to completely design a class without implementing any of it.
 * To do this, simply include the statement</p>
 *
 * <blockquote><code>
 *   throw new UnderConstructionException();
 * </code></blockquote>
 *
 * <p>in any method or execution path that you'd like to finish later.  The
 * compiler will let you compile the code without forcing you to insert dummy
 * return values or "TO<!-- fool IDE -->DO" comments that you might forget about 
 * later.</p>
 *
 * <blockquote>
 *
 *   <p><b>Example 1:</b></p>
 *
 * <!-- The arrangement of the asterisks has special significance in javadoc
 * comments.  Please ask Sharky for details if you are curious. -->
 *
     <pre>
      *if (bReadyToRock)
      *{ do_stuff();
      *  do_more_stuff();
      *}
      *else
      *  throw new UnderConstructionException();
     </pre>
 *
 * </blockquote>
 *
 * <p>In Example 1, a single execution path has been left unimplemented because
 * the programmer is not yet concerned about it.  He'll get to it later, and if
 * he forget's he'll be reminded when it's needed.</p>
 *
 * <blockquote>
 *
 *   <p><b>Example 2:</b></p>
 *
     <pre>
      *public static float[] findPolynomialRoots(Polynomial ipPolynomial)
      *{ // I'm putting this declaration here so that the rest of my code will
      *  // compile, but I'll implement the body later or ask my partner to do
      *  //  it.
      *  throw new UnderConstructionException();
      *}
     </pre>
 *
 * </blockquote>
 *
 * <p>In Example 2, a method is stubbed out because the developer knows he's
 * going to need it, but he doesn't want to focus on its implementation right
 * now.  In situations like this, you may be tempted to stub out the method and
 * return a dummy value, such as <code>null</code> or 0.  Don't!  Doing so is
 * asking for trouble!  You might forget about it and wind up with a
 * difficult-<wbr>to-<wbr>trace bug!  Just use 
 * <code>UnderConstructionException</code> instead, and rest confident that the 
 * JVM will remind you if your forgotten method ever gets executed.  In fact, 
 * you won't just be reminded, but you'll also be pointed to the class, method, 
 * and line number!</p>
 *
 * <blockquote>
 *
 *   <p><b>Example 3:</b></p>
 *
     <pre>
      *public void actionPerformed(ActionEvent ipEvent)
      *{ // We're safe doing nothing for now, but eventually we'll need to
      *  // implement this.
      *  UnderConstructionException.trace("warning: button response not implemented");
      *}
     </pre>
 *
 * </blockquote>
 *
 * <p>In Example 3, a button's action event handler has not yet been 
 * implemented.  At this stage in the application development, however, the
 * programmer has decided that this is a problem.  Rather than stopping the show
 * with an exception, the developer has decided to simply let
 * <code>UnderConstructionException</code> issue a reminder that the handler has
 * not yet been implemented.</p>
 *
 * <p>Before deploying the final build of an application, the developer should
 * search all source files for instances of the identifier
 * "<code>UnderConstructionException</code>" to be sure that application is 
 * indeed complete.</p>
 *
 * @since 1997
 * @version 1999.04.19
 */
public class UnderConstructionException extends RuntimeException
{

  private static final long serialVersionUID = 0;

  /**
   * Initializes without detail.
   *
   * <p><b>Details:</b>  This default constructor initializes a new
   * <code>UnderConstructionException</code> without a detail message.</p>
   */
  public UnderConstructionException()
  {
    super();
  }

  /**
   * Initializes with detail.
   *
   * <p><b>Details:</b>  This constructor initializes a new
   * <code>UnderConstructionException</code> with the given detail message.</p>
   *
   * @param isMessage the messsage
   */
  public UnderConstructionException(String isMessage)
  {
    super(isMessage);
  }

  /**
   * Initializes with detail and cause.
   * 
   * <p><b>Details:</b> This constructor initializes a new instance with the
   * given detail message and wraps the exception that caused the VM to reach
   * the unimplemented execution path.</p>
   * 
   * <p>This constructor may be useful for marking unimplemented execution paths 
   * in catch blocks.</p>
   * 
   * @param iMessage the message
   * @param iCause the causing exception
   */
  public UnderConstructionException(String iMessage, Throwable iCause)
  {
    super(iMessage, iCause);
  }

  /**
   * Initializes with cause.
   * 
   * <p><b>Details:</b> This constructor wraps the exception that caused the VM
   * to reach the unimplemented execution path.</p>
   * 
   * <p>This constructor may be useful for marking unimplemented execution paths 
   * in catch blocks.</p>
   * 
   * @param iCause the causing exception
   */
  public UnderConstructionException(Throwable iCause)
  {
    super(iCause);
  }

}

