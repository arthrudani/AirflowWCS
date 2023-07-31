package com.daifukuamerica.wrxj.web.ui.tag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.tagext.TagSupport;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;

import com.daifukuamerica.wrxj.web.model.hibernate.NavigationOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for navigation tags
 *
 * @author mandrus
 */
public abstract class NavTag extends TagSupport
{
	private static final long serialVersionUID = -4062186507020525195L;

	private static final Logger logger = LoggerFactory.getLogger(NavTag.class);

	private boolean pretty = false;
	private String linkContext;
	private List<String> roles;

	/**
	 * Constructor
	 */
	protected NavTag()
	{
	}

	/**
	 * Initialize
	 */
	protected void initialize()
	{
		// TODO: Is there a better way to get assigned roles?
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		HttpSession session = request.getSession();
		SecurityContextImpl sci = (SecurityContextImpl) session.getAttribute("SPRING_SECURITY_CONTEXT");
		Authentication a = sci.getAuthentication();
		roles = new ArrayList<>();
		for (GrantedAuthority ga : a.getAuthorities())
		{
			roles.add(ga.getAuthority());
		}
		roles = expandRoles(roles);

		// TODO: Is there a better way to get the path prefix?
		if (linkContext == null)
		{
			String requestPath = request.getRequestURI();
			requestPath = requestPath.substring(0, requestPath.indexOf("/", 1));
			linkContext = requestPath;
		}

		logger.debug("Generating Navigation Panel -> Path = {}", linkContext);
		logger.debug("Generating Navigation Panel -> Roles = {}", roles);
	}

	/**
	 * Make roles inclusive of lesser roles
	 * @param roles
	 * @return
	 */
	private List<String> expandRoles(List<String> dbRoles)
	{
        // Total hack to make roles inclusive of lesser roles
        Set<String> roles = new HashSet<String>(dbRoles);
        if (roles.contains("ROLE_MASTER"))
        {
            roles.add("ROLE_ADMIN");
            roles.add("ROLE_ELEVATED");
            roles.add("ROLE_USER");
            roles.add("ROLE_READONLY");
        }
        else if (roles.contains("ROLE_ADMIN"))
        {
            roles.add("ROLE_ELEVATED");
            roles.add("ROLE_USER");
            roles.add("ROLE_READONLY");
        }
        else if (roles.contains("ROLE_ELEVATED"))
        {
            roles.add("ROLE_USER");
            roles.add("ROLE_READONLY");
        }
        else if (roles.contains("ROLE_USER"))
        {
            roles.add("ROLE_READONLY");
        }
        return new ArrayList<String>(roles);
	}
	
	/*======================================================================*/
	/* Protected Accessors													*/
	/*======================================================================*/

	/**
	 * Get the roles that will be used to build the navigation panel
	 *
	 * @return
	 */
	protected List<String> getRoles()
	{
		return roles;
	}

	/**
	 * Get the output string
	 *
	 * @param sb
	 * @return
	 */
	protected String format(StringBuilder sb)
	{
		String output = sb.toString();
		if (!isPretty())
		{
			output = output.replaceAll("\t|\n|\r", "");
		}

		return output;
	}

	/**
	 * Convert the DB link definition to the UI definition
	 *
	 * @param o
	 * @return
	 */
	protected String toLink(NavigationOption o)
	{
		String link = o.getLink();
		String target = "";
		if (link.startsWith("/") && !link.endsWith("pdf"))
		{
			// For internal jsp links, add the root path
			link = linkContext + link;
		}
		else if (link.startsWith("http") || link.endsWith("pdf"))
		{
			// Let external URLs open in a new tab
			target = " target='" + o.getName() + "'";
		}
		else
		{
		  logger.error("NavTag: Link [{}] appears invalid.", o.getLink());
		}
		return "'" + link + "'" + target;
	}

	/*======================================================================*/
	/* Public Accessors														*/
	/*======================================================================*/

	/**
	 * Pretty or Ugly (minified)
	 *
	 * @return true if pretty, false is ugly (minified)
	 */
	public boolean isPretty()
	{
		return pretty;
	}

	/**
	 * Pretty or Ugly (minified)
	 *
	 * @param pretty - true if pretty, false is ugly (minified)
	 */
	public void setPretty(boolean pretty)
	{
		this.pretty = pretty;
	}
}
