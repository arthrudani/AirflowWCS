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
 * Convenience Tag to build the favorites navigation panel
 */
public class FavPanelTag extends NavTag
{
	private static final long serialVersionUID = -2745730559179693499L;

	/**
	 * Constructor
	 */
	public FavPanelTag()
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
			// TODO: User-configurable favorites
			out.print(generateNavigationPanel(new NavigationOptionsService().getDefaultFavoriteOptions(getRoles())));
		}
		catch (Exception e)
		{
			throw new JspException(e);
		}
		return SKIP_BODY;
	}

	/**
	 * Generate the favorites navigation panel
	 *
	 * @return
	 */
	public String generateNavigationPanel(List<Object[]> options)
	{
		String lineSeparator = System.lineSeparator();

		StringBuilder sb = new StringBuilder();

		sb.append(lineSeparator);
		sb.append("<div class='panel-heading'><span class='panel-title'>Navigation Favorites</span></div>").append(lineSeparator);
		sb.append("<div id='favorites' class='panel-body'>").append(lineSeparator);

		sb.append("	<div class='row clearfix'>").append(lineSeparator);
		if (options != null)
		{
			int rowControl = 0;
			for (Object[] opt : options)
			{
				// 4 tiles per row
				if (rowControl > 0 && rowControl % 4 == 0)
				{
					sb.append("	</div>").append(lineSeparator);
					sb.append("	<div class='row clearfix'>").append(lineSeparator);
				}
				rowControl++;

				// Tiles
				NavigationOption o = (NavigationOption)opt[1];

				sb.append("		<div class='col-md-3 column'>").append(lineSeparator);
				sb.append("			<a href=").append(toLink(o)).append(">").append(lineSeparator);
				sb.append("				<div class='feature-box'>").append(lineSeparator);
				sb.append("					<span class='feature-icon'><i class='fa ").append(o.getIcon()).append("'></i></span>").append(lineSeparator);
				sb.append("						<h3>").append(o.getName().toUpperCase()).append("</h3>").append(lineSeparator);
				sb.append("					<span>").append(o.getDescription()).append("</span>").append(lineSeparator);
				sb.append("				</div>").append(lineSeparator);
				sb.append("			</a>").append(lineSeparator);
				sb.append("		</div>").append(lineSeparator);
			}
		}
		else
		{
			sb.append("	<!-- No navigation favorites found! -->").append(lineSeparator);
		}
		sb.append("	</div>").append(lineSeparator);

		sb.append("</div>");

		return format(sb);
	}

	/**
	 * Quick Test
	 *
	 * @param args
	 */
	public static void main(String[] args)
	{
		FavPanelTag npt = new FavPanelTag();
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
