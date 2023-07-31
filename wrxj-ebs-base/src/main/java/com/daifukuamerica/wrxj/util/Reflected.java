package com.daifukuamerica.wrxj.util;

/**
 * Marks items accessed via reflection.
 * 
 * <p><b>Details:</b> This interface indicates that the annotated class or 
 * method is accessed via reflection.</p>
 * 
 * <p>There is always a temptation to remove unreferenced classes and methods 
 * from a project because -- let's face it -- who wants to maintain code that 
 * will never execute?  However, if any of those items are accessed at runtime 
 * via reflection, then removing them will create problems at runtime and, 
 * effectively, "break the run."  This interface should be used to mark such 
 * items, in order to discourage their removal.</p>
 *     
 * @author Sharky
 */
public @interface Reflected
{
}

