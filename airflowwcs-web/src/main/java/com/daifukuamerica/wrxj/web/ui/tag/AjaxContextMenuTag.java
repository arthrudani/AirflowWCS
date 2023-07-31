/**
 * 
 */
package com.daifukuamerica.wrxj.web.ui.tag;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.data.AsrsMetaData;
import com.daifukuamerica.wrxj.dbadapter.data.AsrsMetaDataData;
import com.daifukuamerica.wrxj.factory.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: dystout
 * Created : Apr 29, 2017
 * 
 * Tag to create a right click context menu from asrs metadata for view.
 * 
 * @deprecated - will use JS to build context menus
 * 
 * TODO - possibly impl db lookup of screen context menu options to build dropdown? 
 * 
 */
public class AjaxContextMenuTag extends SimpleTagSupport
{
	/**
	* Log4j logger: AjaxContextMenuTag
	*/
	private static final Logger logger = LoggerFactory.getLogger(AjaxContextMenuTag.class);
	
	private AsrsMetaDataData mddata = Factory.create(AsrsMetaDataData.class);
	
	  
	private List<Map> metadata;
	private String metaDataName; 
	private AbstractSKDCData skdcData;
	protected String genJavaScript; 
	protected String genHtml; 
	
	public AjaxContextMenuTag()
	{
		
	}
	
	  /**
	 * @see javax.servlet.jsp.tagext.SimpleTagSupport#doTag()
	 * 
	 * Generate Context Menu
	 *
	 */
	@Override
	public void doTag() throws JspException, IOException
	{
		
	}
	
	

}
