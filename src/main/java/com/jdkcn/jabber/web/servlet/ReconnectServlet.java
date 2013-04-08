/**
 * Project:jabberer
 * File:ReconnectServlet.java
 * Copyright 2004-2012 Homolo Co., Ltd. All rights reserved.
 */
package com.jdkcn.jabber.web.servlet;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jdkcn.jabber.web.listener.WebAppListener;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.jdkcn.jabber.robot.Robot;
import com.jdkcn.jabber.robot.RobotMessageListener;
import com.jdkcn.jabber.util.Constants;

/**
 * @author Rory
 * @date Feb 8, 2012
 * @version $Id$
 */
@Singleton
public class ReconnectServlet extends HttpServlet {

	private final Logger logger = LoggerFactory.getLogger(ReconnectServlet.class);
	
	private static final long serialVersionUID = 9193217606790233091L;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String robotName = req.getParameter("robot");
		@SuppressWarnings("unchecked")
		List<Robot> robots = (List<Robot>) req.getServletContext().getAttribute(Constants.ROBOTS);
        Robot reconnectRobot = null;

		for (Robot robot : robots) {
			if (StringUtils.equals(robot.getName(), robotName)) {
                reconnectRobot = robot;
            }
        }
        if (reconnectRobot != null) {
            robots.remove(reconnectRobot);
        }
        if (reconnectRobot != null) {
            try {
                if (reconnectRobot.getConnection().isConnected()) {
                    reconnectRobot.getConnection().disconnect();
                }
                JsonNode jsonConfig = (JsonNode) req.getServletContext().getAttribute(Constants.JABBERERJSONCONFIG);
                for (JsonNode node : jsonConfig.get("robots")) {
                    if (node.get("name").asText().equalsIgnoreCase(robotName)) {
                        reconnectRobot = WebAppListener.connect(node);
                    }
                }
            } catch (Exception e) {
                logger.error("reconnect robot failed:", e);
            }
            robots.add(reconnectRobot);
        }
        req.getServletContext().setAttribute(Constants.ROBOTS, robots);
        resp.sendRedirect(req.getContextPath() + "/");
	}
	
}
