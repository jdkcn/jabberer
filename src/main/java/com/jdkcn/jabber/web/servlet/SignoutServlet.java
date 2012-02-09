package com.jdkcn.jabber.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Singleton;
import com.jdkcn.jabber.util.Constants;

/**
 * @author <a href="mailto:rory.cn@gmail.com">Rory</a>
 * @version $Id$
 */
@Singleton
public class SignoutServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7157895059055890646L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.getSession().removeAttribute(Constants.LOGIN_USER);
		req.getSession().invalidate();
		resp.sendRedirect(req.getContextPath() + "/signin");
	}
}
