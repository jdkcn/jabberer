/**
 * Project:jabberer
 * File:WebAppListener.java
 * Copyright 2004-2012 Homolo Co., Ltd. All rights reserved.
 */
package com.jdkcn.jabber.web.listener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.jdkcn.jabber.bot.BotMessageListener;
import com.jdkcn.jabber.util.JsonUtil;
import com.jdkcn.jabber.web.servlet.IndexServlet;

/**
 * @author Rory
 * @date Feb 7, 2012
 * @version $Id$
 */
@WebListener
public class WebAppListener extends GuiceServletContextListener {
	
	private final Logger logger = LoggerFactory.getLogger(WebAppListener.class);
	
	private List<XMPPConnection> connections = new ArrayList<XMPPConnection>();
	
	private JsonNode jsonConfig;
	
	public List<XMPPConnection> getConnections() {
		return connections;
	}
	
	public JsonNode getJsonConfig() {
		return jsonConfig;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		super.contextInitialized(servletContextEvent);
		try {
			jsonConfig = JsonUtil.fromJson(WebAppListener.class.getResourceAsStream("/config.json"), JsonNode.class);
			for (Iterator<JsonNode> iterator = jsonConfig.get("bots").iterator(); iterator.hasNext();) {
				JsonNode bot = iterator.next();
				String username = bot.get("username").asText();
				String password = bot.get("password").asText();
				String botStatusMessage = bot.get("bot.status.message").asText();
				Boolean sendOfflineMessage = bot.get("send.offline.message").getBooleanValue();
				ConnectionConfiguration connConfig = new ConnectionConfiguration("talk.google.com", 5222, "gmail.com");
				XMPPConnection connection  = new XMPPConnection(connConfig);
				connection.connect();
				connection.login(username, password);
				Presence presence = new Presence(Presence.Type.available, botStatusMessage, 0, Presence.Mode.available);
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

				final MessageListener messageListener = new BotMessageListener(connection, roster, entries, sendOfflineMessage);

				chatManager.addChatListener(new ChatManagerListener() {
					@Override
					public void chatCreated(Chat chat, boolean createdLocally) {
						chat.addMessageListener(messageListener);
					}
				});
				logger.info(" bot {} online now.", username);
				connections.add(connection);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XMPPException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		super.contextDestroyed(servletContextEvent);
		for (XMPPConnection connection : connections) {
			if (connection != null) {
				logger.info("bot {} disconnect now.", connection.getUser());
				connection.disconnect();
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
				serve("/index.jsp", "/index.html", "/").with(IndexServlet.class);
			}
		});
	}

}
