/**
 * Project:jabberer
 * File:DisconnectServlet.java
 * Copyright 2004-2012 Homolo Co., Ltd. All rights reserved.
 */
package com.jdkcn.jabber.web.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.jivesoftware.smack.XMPPConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.jdkcn.jabber.robot.Robot;
import com.jdkcn.jabber.web.listener.WebAppListener;

/**
 * @author Rory
 * @date Feb 8, 2012
 * @version $Id$
 */
@Singleton
public class DisconnectServlet extends HttpServlet {
	
	private final Logger logger = LoggerFactory.getLogger(DisconnectServlet.class);

	private static final long serialVersionUID = 62363420741659852L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String robotName = req.getParameter("robot");
		@SuppressWarnings("unchecked")
		Map<String, XMPPConnection> connectionMap = (Map<String, XMPPConnection>) req.getServletContext().getAttribute(WebAppListener.XMPPCONNECTION_MAP);
		@SuppressWarnings("unchecked")
		List<Robot> robots = (List<Robot>) req.getServletContext().getAttribute(WebAppListener.ROBOTS);
		XMPPConnection connection = connectionMap.get(robotName);
		if (connection != null) {
			connection.disconnect();
			Robot robot = findRobot(robotName, robots);
			if (robot != null) {
				robot.setStatus(Robot.Status.Offline);
			}
		} else {
			logger.error("no connection found with robot {}", robotName);
		}
		resp.sendRedirect(req.getContextPath());
	}

	/**
	 * @param robotName
	 * @param robots
	 * @return
	 */
	private Robot findRobot(String robotName, List<Robot> robots) {
		for (Robot robot : robots) {
			if (StringUtils.equalsIgnoreCase(robotName, robot.getName())) {
				return robot;
			}
		}
		return null;
	}
}
