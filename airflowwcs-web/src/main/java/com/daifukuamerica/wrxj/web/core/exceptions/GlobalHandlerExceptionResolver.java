package com.daifukuamerica.wrxj.web.core.exceptions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import com.daifukuamerica.wrxj.web.model.Login;
import com.daifukuamerica.wrxj.web.ui.UIConstants;

@Component
public class GlobalHandlerExceptionResolver implements HandlerExceptionResolver
{

	@Override
	public ModelAndView resolveException(HttpServletRequest req, HttpServletResponse resp, Object handler, Exception exception)
	{
		ModelAndView mav = new ModelAndView(); 
		if(exception instanceof NotLoggedInException)
		{

			mav.setViewName(UIConstants.VIEW_LOGIN);
			Login login = new Login(); 
			login.setLoginError(UIConstants.SESSION_INVALID);
			mav.addObject("login", login);

		}
		
		return mav;
	}

}
