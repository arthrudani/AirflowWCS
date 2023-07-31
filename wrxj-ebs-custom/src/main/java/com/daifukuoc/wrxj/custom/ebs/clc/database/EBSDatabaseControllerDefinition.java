package com.daifukuoc.wrxj.custom.ebs.clc.database;

import java.util.Map;

import com.daifukuamerica.wrxj.clc.database.DatabaseControllerDefinition;

public class EBSDatabaseControllerDefinition extends DatabaseControllerDefinition
{
	public EBSDatabaseControllerDefinition(String isName, String isType)
	  {
	    super(isName, isType);
	  }
	  
	  public EBSDatabaseControllerDefinition(String isName, String isType, Map<String,String> ipInitialProps)
	  {
	    super( isName,  isType, ipInitialProps);
	  }
}
