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
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
		Map<String, XMPPConnection> connectionMap = (Map<String, XMPPConnection>) req.getServletContext().getAttribute(Constants.XMPPCONNECTION_MAP);
		XMPPConnection connection = connectionMap.remove(robotName);
		@SuppressWarnings("unchecked")
		List<Robot> robots = (List<Robot>) req.getServletContext().getAttribute(Constants.ROBOTS);
		if (connection != null) {
			try {
				if (connection.isConnected()) {
					connection.disconnect();
				}
				ConnectionConfiguration connConfig = new ConnectionConfiguration("talk.google.com", 5222, "gmail.com");
				connConfig.setCompressionEnabled(true);
				connConfig.setSASLAuthenticationEnabled(true);
				connection = new XMPPConnection(connConfig);
				connection.connect();
				JsonNode jsonConfig = (JsonNode) req.getServletContext().getAttribute(Constants.JABBERERJSONCONFIG);
				String robotStatusMessage = null;
				Boolean sendOfflineMessage = null;
				String username = robotName;
				String password = null;
				for (Iterator<JsonNode> iterator = jsonConfig.get("robots").iterator(); iterator.hasNext();) {
					JsonNode node = iterator.next();
					if (node.get("username").asText().equalsIgnoreCase(robotName)) {
						password = node.get("password").asText();
						robotStatusMessage = node.get("robot.status.message").asText();
						sendOfflineMessage = node.get("send.offline.message").asBoolean();
					}
				}
				connection.login(username, password);
				Presence presence = new Presence(Presence.Type.available, robotStatusMessage, 0, Presence.Mode.available);
				connection.sendPacket(presence);
				
				Roster roster = connection.getRoster();
				roster.setSubscriptionMode(SubscriptionMode.reject_all);
				roster.addRosterListener(new RosterListener() {
					@Override
					public void presenceChanged(Presence presence) {
						// System.out.println("Presence changed: " + presence.getFrom() + " " + presence);
					}

					@Override
					public void entriesUpdated(Collection<String> addresses) {
						System.out.println("entries want updated:" + addresses);
					}

					@Override
					public void entriesDeleted(Collection<String> addresses) {
						System.out.println("entries want deleted:" + addresses);
					}

					@Override
					public void entriesAdded(Collection<String> addresses) {
						System.out.println("entries want added:" + addresses);
					}
				});
				final Collection<RosterEntry> entries = roster.getEntries();
				ChatManager chatManager = connection.getChatManager();

				Robot robot = findRobot(robotName, robots);
				final MessageListener messageListener = new RobotMessageListener(connection, roster, entries, sendOfflineMessage, robot);

				chatManager.addChatListener(new ChatManagerListener() {
					@Override
					public void chatCreated(Chat chat, boolean createdLocally) {
						chat.addMessageListener(messageListener);
					}
				});
				if (robot != null) {
					robot.setStartTime(new Date());
					robot.setStatus(Robot.Status.Online);
				}
				connectionMap.put(robotName, connection);
			} catch (XMPPException e) {
				logger.error("reconnect robot failed:", e);
			}
		} else {
			logger.error("no connection found with robot {}", robotName);
		}
		resp.sendRedirect(req.getContextPath() + "/");
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
