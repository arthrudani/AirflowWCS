package com.daifukuamerica.wrxj.web.controllers;

import com.daifukuamerica.wrxj.web.ui.UIConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for Help operations. This is the main mapping declaration and will map URL patterns
 * to the controller methods. For example the top level controller in this instance would be {servletContext}/help
 */
@Controller
@RequestMapping("/help")
public class HelpController {

	/**
	 * Log4j logger: Help
	 */
//	private static final Logger logger = Logger.getLogger("Help");

	/**
	 * Return the logical view name for the /wrxj/load/load.jsp page
	 * @see {@link UIConstants} for mappings to logical view names
	 *
	 * @param model
	 * @return
	 */
	@RequestMapping("/support")
	public String view(Model model)
	{
		model.addAttribute("pageName", "SUPPORT");
		return UIConstants.VIEW_SUPPORT;
	}
}
