package com.daifukuamerica.wrxj.web.pages;

public class DecantTRPage
{

	
	public boolean isPalindrome(int x) {
	    if(x<9 && x>-9) return true; 
        String forward = String.valueOf(x); 
        StringBuilder sb = new StringBuilder(String.valueOf(x)); 
        String backwards = sb.reverse().toString(); 
        return forward.equals(backwards); 
        
    }
}
