/**
 * Copyright (c) 2005-2013, Rory Ye
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of the Jdkcn.com nor the names of its contributors may
 *       be used to endorse or promote products derived from this software without
 *       specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jdkcn.jabber.web.listener;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.jdkcn.jabber.robot.Robot;
import com.jdkcn.jabber.robot.RobotMessageListener;
import com.jdkcn.jabber.util.JsonUtil;
import com.jdkcn.jabber.web.filter.UserSigninFilter;
import com.jdkcn.jabber.web.servlet.AddRosterEntryServlet;
import com.jdkcn.jabber.web.servlet.DisconnectServlet;
import com.jdkcn.jabber.web.servlet.IndexServlet;
import com.jdkcn.jabber.web.servlet.ReconnectServlet;
import com.jdkcn.jabber.web.servlet.RemoveRosterEntryServlet;
import com.jdkcn.jabber.web.servlet.RenameServlet;
import com.jdkcn.jabber.web.servlet.SigninServlet;
import com.jdkcn.jabber.web.servlet.SignoutServlet;
import org.codehaus.jackson.JsonNode;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.jdkcn.jabber.util.Constants.JABBERERJSONCONFIG;
import static com.jdkcn.jabber.util.Constants.ROBOTS;

/**
 * The webapp listener for robot startup.
 *
 * @author Rory
 * @version $Id$
 *          Time Feb 7, 2012
 */
@WebListener
public class WebAppListener extends GuiceServletContextListener {

    /**
     * The slf4j logger.
     */
    private static Logger logger = LoggerFactory.getLogger(WebAppListener.class);
    /**
     * All the robots.
     */
    private List<Robot> robots = new ArrayList<Robot>();

    /**
     * connec the robot.
     *
     * @param robotNode the robot's json config.
     * @return the robot.
     */
    public static Robot connect(JsonNode robotNode) {
        String username = robotNode.get("username").asText();
        Robot robot = new Robot();
        robot.setUsername(username);
        robot.setName(robotNode.get("name").asText());
        Boolean sendOfflineMessage = robotNode.get("send.offline.message").getBooleanValue();
        robot.setSendOfflineMessage(sendOfflineMessage);
        findAdministrators(robot, robotNode);
        try {
            String password = robotNode.get("password").asText();
            String robotStatusMessage = robotNode.get("robot.status.message").asText();
            String host = robotNode.get("host").asText();
            String serviceName = host;
            if (robotNode.has("service.name")) {
                serviceName = robotNode.get("service.name").asText();
            }
            robot.setPassword(password);

            ConnectionConfiguration connConfig = new ConnectionConfiguration(host, robotNode.get("port").asInt(), serviceName);
            connConfig.setCompressionEnabled(true);
            connConfig.setSASLAuthenticationEnabled(true);
            XMPPConnection connection = new XMPPConnection(connConfig);
            connection.connect();
            connection.login(username, password);

            Presence presence = new Presence(Presence.Type.available, robotStatusMessage, 0, Presence.Mode.available);
            connection.sendPacket(presence);
            Roster roster = connection.getRoster();
            roster.setSubscriptionMode(SubscriptionMode.manual);
            PacketListener subscriptionListener = new SubscriptionListener(roster, connection);
            connection.addPacketListener(subscriptionListener, new PacketTypeFilter(Presence.class));

            final Collection<RosterEntry> entries = roster.getEntries();
            logger.info(" robot {} online now.", username);

            robot.setStartTime(new Date());
            robot.getRosters().addAll(entries);
            robot.setStatus(Robot.Status.Online);

            ChatManager chatManager = connection.getChatManager();
            final MessageListener messageListener = new RobotMessageListener(robot, sendOfflineMessage);

            chatManager.addChatListener(new ChatManagerListener() {
                @Override
                public void chatCreated(Chat chat, boolean createdLocally) {
                    chat.addMessageListener(messageListener);
                }
            });
            robot.setConnection(connection);
        } catch (Exception e) {
            logger.error(String.format(" robot[%s] connect error.", username), e);
            e.printStackTrace();
            robot.setStatus(Robot.Status.LoginFailed);
        }
        return robot;
    }

    /**
     * find the administrators and set to robot.
     *
     * @param robot     the robot.
     * @param robotNode the robot's json config.
     */
    private static void findAdministrators(Robot robot, JsonNode robotNode) {
        List<String> administrators = new ArrayList<String>();
        robot.getAdministrators().clear();
        for (JsonNode node : robotNode.get("administrators")) {
            administrators.add(node.asText());
        }
        for (RosterEntry entry : robot.getRosters()) {
            if (administrators.contains(entry.getUser())) {
                robot.getAdministrators().add(entry);
            }
        }
        robot.setAdministratorIds(administrators);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        super.contextInitialized(servletContextEvent);
        try {
            JsonNode jsonConfig = JsonUtil.fromJson(WebAppListener.class.getResourceAsStream("/config.json"), JsonNode.class);
            servletContextEvent.getServletContext().setAttribute(JABBERERJSONCONFIG, jsonConfig);
            for (JsonNode robotNode : jsonConfig.get("robots")) {
                Robot robot = connect(robotNode);
                robots.add(robot);
            }
            servletContextEvent.getServletContext().setAttribute(ROBOTS, robots);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        super.contextDestroyed(servletContextEvent);
        for (Robot robot : robots) {
            if (robot.getConnection() != null) {
                logger.info("bot {} disconnect now.", robot.getName());
                robot.getConnection().disconnect();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Injector getInjector() {
        return Guice.createInjector(new ServletModule() {
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
                serve("/entry/add").with(AddRosterEntryServlet.class);
                serve("/entry/remove").with(RemoveRosterEntryServlet.class);
                serve("/entry/rename").with(RenameServlet.class);
            }
        });
    }

    /**
     * Deal the subscription manual.
     */
    private static class SubscriptionListener implements PacketListener {

        /**
         * The roster.
         */
        private Roster roster;
        /**
         * The xmpp connection.
         */
        private Connection connection;

        /**
         * Constructor with roster and connection.
         *
         * @param roster     the roster.
         * @param connection the xmpp connection.
         */
        public SubscriptionListener(Roster roster, Connection connection) {
            this.roster = roster;
            this.connection = connection;
        }

        @Override
        public void processPacket(Packet packet) {
            if (packet instanceof Presence) {
                Presence presence = (Presence) packet;
                String from = presence.getFrom();
                if (presence.getType() == Presence.Type.subscribe) {
                    logger.info("receive a subscribe presence.");
                    if (roster.contains(from)) {
                        // Accept the subscription request.
                        Presence response = new Presence(Presence.Type.subscribed);
                        response.setTo(from);
                        connection.sendPacket(response);
                        logger.info("Accept the subscription request from:" + from);
                    } else {
                        // Reject the subscription request.
                        Presence response = new Presence(Presence.Type.unsubscribed);
                        response.setTo(from);
                        connection.sendPacket(response);
                        logger.info("Reject the subscription request from:" + from);
                    }
                } else if (presence.getType() == Presence.Type.unsubscribe) {
                    logger.info("Unsubscribe from:" + from);
                    Presence response = new Presence(Presence.Type.unsubscribed);
                    response.setTo(from);
                    connection.sendPacket(response);
                }
            }
        }

    }
}