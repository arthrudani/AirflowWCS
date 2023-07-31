package com.daifukuamerica.wrxj.web.controllers;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.web.model.Login;
import com.daifukuamerica.wrxj.web.model.User;
import com.daifukuamerica.wrxj.web.ui.UIConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Login request/response controller for Login specific actions.
 *
 * Mapped using relative context mappings. (http://{serverhostname}:{port}/{context}/)
 * This is mapped to root context and is not an endpoint for which user authentication is needed.
 *
 * Author: dystout
 * Created : May 2, 2017
 *
 */
@Controller
@RequestMapping("/")
public class LoginController
{

	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

	@RequestMapping("/")
	public String view(Model model)
	{
		model.addAttribute("login", new Login());
		return UIConstants.VIEW_LOGIN;
	}

	@RequestMapping(value="/logErr", method=RequestMethod.GET)
	public String viewLoginError(Model model, @RequestParam("error") String error)
	{
		Login login  = new Login();
		model.addAttribute("login", login);
		model.addAttribute("loginError", true);
		if(error.equals("timeout")){
			model.addAttribute("loginErrorMessage", "This session has timed out, possibly from an idle session. Please login to continue.");
		}else{
			model.addAttribute("loginErrorMessage", error);
		}

		return UIConstants.VIEW_LOGIN;
	}

	@RequestMapping("/welcome")
	public String viewWelcome(HttpSession session, Model model)
	{
		setSessionAttributes(session);
		model.addAttribute("pageName", "HOME");
		return UIConstants.VIEW_WELCOME;
	}

	@RequestMapping(value="/logout",  method=RequestMethod.GET)
	public String logout(Model model, HttpSession session, @ModelAttribute("user") User user)
	{
		model.addAttribute("login", new Login());
		return "login";
	}

	@RequestMapping(value="/timeout",  method=RequestMethod.GET)
	public String timeout(Model model, HttpSession session, @ModelAttribute("user") User user)
	{
		model.addAttribute("login", new Login());
		return "redirect:timeoutlogin";
	}

	/**
	 * Set session attributes for application configuration
	 * @param session
	 */
	protected void setSessionAttributes(HttpSession session)
	{
		// Load Mover vs Inventory Mover
		session.setAttribute("wrxHasInventory", Application.getBoolean("com.daifukuamerica.wrxj.hasInventory", false));

		// Location vs Shelf (doubledeep2)
		boolean vzhasShelf = false;
		try
		{
			// Assume that if this class is on the classpath that we are using shelf positions
			Class.forName("com.daifukuamerica.wrxj.doubledeep.dbadapter.data.ShelfPositionData");
			vzhasShelf = true;
		}
		catch (ClassNotFoundException cnfe) {}
		session.setAttribute("wrxHasShelf", vzhasShelf);
	}
}
