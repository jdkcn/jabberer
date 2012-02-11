/**
 * Project:jabberer
 * File:WebAppListener.java
 * Copyright 2004-2012 Homolo Co., Ltd. All rights reserved.
 */
package com.jdkcn.jabber.web.listener;

import static com.jdkcn.jabber.util.Constants.JABBERERJSONCONFIG;
import static com.jdkcn.jabber.util.Constants.ROBOTS;
import static com.jdkcn.jabber.util.Constants.XMPPCONNECTION_MAP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

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

import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.jdkcn.jabber.robot.Robot;
import com.jdkcn.jabber.robot.RobotMessageListener;
import com.jdkcn.jabber.util.JsonUtil;
import com.jdkcn.jabber.web.filter.UserSigninFilter;
import com.jdkcn.jabber.web.servlet.DisconnectServlet;
import com.jdkcn.jabber.web.servlet.IndexServlet;
import com.jdkcn.jabber.web.servlet.ReconnectServlet;
import com.jdkcn.jabber.web.servlet.SigninServlet;
import com.jdkcn.jabber.web.servlet.SignoutServlet;

/**
 * @author Rory
 * @date Feb 7, 2012
 * @version $Id$
 */
@WebListener
public class WebAppListener extends GuiceServletContextListener {
	
	private final Logger logger = LoggerFactory.getLogger(WebAppListener.class);
	
	private Map<String, XMPPConnection> connectionMap = Maps.newConcurrentMap();
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		super.contextInitialized(servletContextEvent);
		try {
			JsonNode jsonConfig = JsonUtil.fromJson(WebAppListener.class.getResourceAsStream("/config.json"), JsonNode.class);
			servletContextEvent.getServletContext().setAttribute(JABBERERJSONCONFIG, jsonConfig);
			List<Robot> robots = new ArrayList<Robot>();
			for (Iterator<JsonNode> iterator = jsonConfig.get("robots").iterator(); iterator.hasNext();) {
				Robot robot = new Robot();
				JsonNode robotNode = iterator.next();
				String username = robotNode.get("username").asText();
				String password = robotNode.get("password").asText();
				String robotStatusMessage = robotNode.get("robot.status.message").asText();
				Boolean sendOfflineMessage = robotNode.get("send.offline.message").getBooleanValue();
				ConnectionConfiguration connConfig = new ConnectionConfiguration("talk.google.com", 5222, "gmail.com");
				connConfig.setCompressionEnabled(true);
				connConfig.setSASLAuthenticationEnabled(true);
				XMPPConnection connection  = new XMPPConnection(connConfig);
				connection.connect();
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

				final MessageListener messageListener = new RobotMessageListener(connection, roster, entries, sendOfflineMessage, robot);

				chatManager.addChatListener(new ChatManagerListener() {
					@Override
					public void chatCreated(Chat chat, boolean createdLocally) {
						chat.addMessageListener(messageListener);
					}
				});
				logger.info(" robot {} online now.", username);
				robot.setName(username);
				robot.setSendOfflineMessage(sendOfflineMessage);
				robot.setStartTime(new Date());
				robot.getRosters().addAll(entries);
				findAdministrators(robot, robotNode);
				robot.setStatus(Robot.Status.Online);
				connectionMap.put(username, connection);
				robots.add(robot);
			}
			servletContextEvent.getServletContext().setAttribute(ROBOTS, robots);
			servletContextEvent.getServletContext().setAttribute(XMPPCONNECTION_MAP, connectionMap);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XMPPException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param robot
	 * @param robotNode
	 */
	private void findAdministrators(Robot robot, JsonNode robotNode) {
		List<String> administrators = new ArrayList<String>();
		robot.getAdministrators().clear();
		for(Iterator<JsonNode> iterator = robotNode.get("administrators").iterator(); iterator.hasNext();) {
			JsonNode node = iterator.next();
			administrators.add(node.asText());
		}
		for (RosterEntry entry : robot.getRosters()) {
			if (administrators.contains(entry.getUser())) {
				robot.getAdministrators().add(entry);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		super.contextDestroyed(servletContextEvent);
		for (Map.Entry<String, XMPPConnection> entry : connectionMap.entrySet()) {
			if (entry != null && entry.getValue() != null) {
				logger.info("bot {} disconnect now.", entry.getKey());
				entry.getValue().disconnect();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Injector getInjector() {
		return Guice.createInjector(new ServletModule(){
			/**
			 * {@inheritDoc}
			 */
			@Override
			protected void configureServlets() {
				filter("/index.jsp", "/", "/robot/*").through(UserSigninFilter.class);
				serve("/index.jsp", "/index.html", "/").with(IndexServlet.class);
				serve("/robot/reconnect").with(ReconnectServlet.class);
				serve("/robot/disconnect").with(DisconnectServlet.class);
				serve("/login", "/signin").with(SigninServlet.class);
				serve("/logout", "/signout").with(SignoutServlet.class);
			}
		});
	}

}
