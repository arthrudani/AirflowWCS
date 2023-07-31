package com.daifukuamerica.wrxj.controller;

/**
 * Persistent controller.
 * 
 * <p><b>Details:</b> A daemon controller is a controller that must be running
 * at all times to support essential application functions.  Daemon controllers
 * are started immediately, unconditionally, and permanently.  All controller
 * implementations with these requirements should extend this marker class.</p>
 * 
 * @author Sharky
 */
public abstract class DaemonController extends Controller
{

  /**
   * Default constructor.
   * 
   * <p><b>Details:</b> This default constructor calls the superclass' default 
   * constructor.</p>
   */
  protected DaemonController()
  {
  }
  
}

