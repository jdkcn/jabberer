package com.jdkcn.jabber.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;

import com.google.inject.Singleton;
import static com.jdkcn.jabber.util.Constants.*;

/**
 * @author <a href="mailto:rory.cn@gmail.com">Rory</a>
 * @version $Id$
 */
@Singleton
public class LoginServlet extends HttpServlet {

	private static final long serialVersionUID = -1108987102277577612L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (req.getSession().getAttribute(LOGIN_USER) == null) {
			req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
		} else {
			resp.sendRedirect(req.getContextPath() + "/");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String user = req.getParameter("user");
		String pass = req.getParameter("pass");
		JsonNode jsonConfig = (JsonNode) req.getServletContext().getAttribute(JABBERERJSONCONFIG);
		if (StringUtils.equals(user, jsonConfig.get("user").asText()) && StringUtils.equals(pass, jsonConfig.get("pass").asText())) {
			req.getSession().setAttribute(LOGIN_USER, user);
			resp.sendRedirect(req.getContextPath() + "/");
		} else {
			req.setAttribute("loginError", true);
			req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
		}
	}
}
