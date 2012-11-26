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
		for (Robot robot : robots) {
			if (StringUtils.equals(robot.getName(), robotName)) {
				try {
					if (robot.getConnection().isConnected()) {
						robot.getConnection().disconnect();
					}
					JsonNode jsonConfig = (JsonNode) req.getServletContext().getAttribute(Constants.JABBERERJSONCONFIG);
					ConnectionConfiguration connConfig = new ConnectionConfiguration("talk.google.com", 5222, "gmail.com");
					connConfig.setCompressionEnabled(true);
					connConfig.setSASLAuthenticationEnabled(true);
					XMPPConnection connection  = new XMPPConnection(connConfig);
					connection.connect();
					connection.login(robot.getName(), robot.getPassword());
					
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
					Presence presence = new Presence(Presence.Type.available, robotStatusMessage, 0, Presence.Mode.available);
					connection.sendPacket(presence);
					robot.setPassword(password);
					robot.setConnection(connection);

					Roster roster = connection.getRoster();
					roster.setSubscriptionMode(SubscriptionMode.reject_all);
					roster.addRosterListener(new RosterListener() {
						@Override
						public void presenceChanged(Presence presence) {
							logger.info("Presence changed: " + presence.getFrom() + " " + presence);
						}

						@Override
						public void entriesUpdated(Collection<String> addresses) {
							logger.info("entries want updated:" + addresses);
						}

						@Override
						public void entriesDeleted(Collection<String> addresses) {
							logger.info("entries want deleted:" + addresses);
						}

						@Override
						public void entriesAdded(Collection<String> addresses) {
							logger.info("entries want added:" + addresses);
						}
					});
					final Collection<RosterEntry> entries = roster.getEntries();
					ChatManager chatManager = connection.getChatManager();
					final MessageListener messageListener = new RobotMessageListener(robot, sendOfflineMessage);

					chatManager.addChatListener(new ChatManagerListener() {
						@Override
						public void chatCreated(Chat chat, boolean createdLocally) {
							chat.addMessageListener(messageListener);
						}
					});
					logger.info(" robot {} online now.", username);
					
					robot.setStartTime(new Date());
					robot.getRosters().addAll(entries);
					robot.setStatus(Robot.Status.Online);
				} catch (XMPPException e) {
					logger.error("reconnect robot failed:", e);
				}
				break;
			}
		}
		resp.sendRedirect(req.getContextPath() + "/");
	}
	
}
