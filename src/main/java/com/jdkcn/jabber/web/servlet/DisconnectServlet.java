/**
 * Project:jabberer
 * File:DisconnectServlet.java
 * Copyright 2004-2012 Homolo Co., Ltd. All rights reserved.
 */
package com.jdkcn.jabber.web.servlet;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Singleton;
import com.jdkcn.jabber.robot.Robot;
import com.jdkcn.jabber.util.Constants;

/**
 * @author Rory
 * @date Feb 8, 2012
 * @version $Id$
 */
@Singleton
public class DisconnectServlet extends HttpServlet {
	
	private static final long serialVersionUID = 62363420741659852L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String robotName = req.getParameter("robot");
		@SuppressWarnings("unchecked")
		List<Robot> robots = (List<Robot>) req.getServletContext().getAttribute(Constants.ROBOTS);
		for (Robot robot : robots) {
			if (StringUtils.equals(robot.getName(), robotName)) {
				if (robot.getConnection() != null && robot.getConnection().isConnected()) {
					robot.getConnection().disconnect();
					robot.setStartTime(new Date());
					robot.setStatus(Robot.Status.Offline);
				}
				break;
			}
		}
		resp.sendRedirect(req.getContextPath() + "/");
	}

}
