package com.daifukuamerica.wrxj.web.model;

public enum CheckWeightEnum
{
		YES("YES",1),
		NO("NO",2); 
	
		private final String key; 
		private final Integer value; 
		
		CheckWeightEnum(String key, Integer value)
		{
			this.key = key; 
			this.value = value; 
		}

		public String getKey()
		{
			return key;
		}

		public Integer getValue()
		{
			return value;
		}
	   
}
