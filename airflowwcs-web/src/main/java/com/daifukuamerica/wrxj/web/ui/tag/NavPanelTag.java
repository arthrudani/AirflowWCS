package com.daifukuamerica.wrxj.web.ui.tag;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import com.daifukuamerica.wrxj.web.core.security.NavigationOptionsService;
import com.daifukuamerica.wrxj.web.model.hibernate.NavigationGroup;
import com.daifukuamerica.wrxj.web.model.hibernate.NavigationOption;

/**
 * Author: mandrus
 *
 * Convenience Tag to build the navigation side panel
 */
public class NavPanelTag extends NavTag
{
	private static final long serialVersionUID = -2745730559179693499L;

	/**
	 * Constructor
	 */
	public NavPanelTag()
	{
	}

	/**
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	@Override
	public int doStartTag() throws JspException
	{
		initialize();
		JspWriter out = pageContext.getOut();
		try
		{
			out.print(generateNavigationPanel(new NavigationOptionsService().getOptions(getRoles())));
		}
		catch (Exception e)
		{
			throw new JspException(e);
		}
		return SKIP_BODY;
	}

	/**
	 * Generate the navigation panel
	 *
	 * @return
	 */
	public String generateNavigationPanel(List<Object[]> options)
	{
		String lineSeparator = System.lineSeparator();

		StringBuilder sb = new StringBuilder();
		// Navigation Header
		sb.append(lineSeparator).append("<ul class='nav panel-list'>").append(lineSeparator);
		sb.append("	<li class='nav-level'>Navigation</li>").append(lineSeparator);

		if (options != null)
		{
			String currentGroup = "";
			for (Object[] opt : options)
			{
				NavigationGroup og = (NavigationGroup)opt[0];
				NavigationOption o = (NavigationOption)opt[1];

				// Group Menu Items
				if (!og.getName().equals(currentGroup))
				{
					if (currentGroup.length() > 0)
					{
						sb.append("		</ul>").append(lineSeparator);
						sb.append("	</li>").append(lineSeparator);
					}
					sb.append("	<li class=\"hoe-has-menu\">").append(lineSeparator);
					sb.append("		<a href=\"javascript:void(0)\">").append(lineSeparator);
					if (og.getStyle() == null)
						sb.append("			<i class='fa ").append(og.getIcon()).append("'></i>").append(lineSeparator);
					else
						sb.append("			<i class='fa ").append(og.getIcon()).append("' style='").append(og.getStyle()).append("'></i>").append(lineSeparator);

					sb.append("			<span class=\"menu-text\">").append(og.getName()).append("</span>").append(lineSeparator);
					sb.append("			<span class=\"selected\"></span>").append(lineSeparator);
					sb.append("		</a>").append(lineSeparator);
					sb.append("		<ul class=\"hoe-sub-menu\">").append(lineSeparator);
					currentGroup = og.getName();
				}

				// Menu Items
				sb.append("			<li>").append(lineSeparator);
				sb.append("				<a href=").append(toLink(o)).append(">").append(lineSeparator);
				sb.append("					<span class='menu-text'>").append(o.getName()).append("</span>").append(lineSeparator);
				sb.append("					<span class='selected'></span>").append(lineSeparator);
				sb.append("				</a>").append(lineSeparator);
				sb.append("			</li>").append(lineSeparator);
			}
			if (currentGroup.length() > 0)
			{
				sb.append("		</ul>").append(lineSeparator);
				sb.append("	</li>").append(lineSeparator);
			}
		}
		else
		{
			sb.append("	<!-- No navigation options found! -->").append(lineSeparator);
		}

		// Removed for now.  Use menu items
//		// Help
//		sb.append("	<li>").append(lineSeparator);
//		// TODO: It might be nice to have screen-related help.
//		sb.append("		<a href='/wrxj-web/resources/doc/WRxWebClientHelp.pdf' target='WRxHelp'>").append(lineSeparator);
//		sb.append("			<i class='fa fa-question-circle'></i>").append(lineSeparator);
//		sb.append("			<span class='menu-text'>Help</span>").append(lineSeparator);
//		sb.append("			<span class='selected'></span>").append(lineSeparator);
//		sb.append("		</a>").append(lineSeparator);
//		sb.append("	</li>").append(lineSeparator);

		sb.append("</ul>");

		return format(sb);
	}

	/**
	 * Quick Test
	 *
	 * @param args
	 */
	public static void main(String[] args)
	{
		NavPanelTag npt = new NavPanelTag();
		npt.setPretty(true);

		System.out.println("<!-- No Roles -->");
		System.out.println(npt.generateNavigationPanel(null));

		System.out.println("<!-- With Roles -->");
		List<Object[]> testList = new ArrayList<>();
		testList.add(new Object[] {new NavigationGroup(1, "Group 1", "fa-user", null, 0), new NavigationOption(0, "", "Group 1", "Link 1", "/test/view", "Description 1", "fa-dice-one")});
		testList.add(new Object[] {new NavigationGroup(1, "Group 1", "fa-user", null, 0), new NavigationOption(0, "", "Group 1", "Link 2", "/test/view", "Description 2", "fa-dice-two")});
		testList.add(new Object[] {new NavigationGroup(1, "Group 2", "fa-wrench", null, 0), new NavigationOption(0, "", "Group 2", "Link 3", "/test/view", "Description 3", "fa-dice-three")});
		testList.add(new Object[] {new NavigationGroup(1, "Group 2", "fa-wrench", null, 0), new NavigationOption(0, "", "Group 2", "Link 4", "/test/view", "Description 4", "fa-dice-four")});
		System.out.println(npt.generateNavigationPanel(testList));
	}
}
