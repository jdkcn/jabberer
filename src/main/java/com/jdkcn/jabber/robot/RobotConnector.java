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
package com.jdkcn.jabber.robot;

import org.codehaus.jackson.JsonNode;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Presence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * The robot connector to connect the server.
 * User: rory
 * Date: 5/02/13
 * Time: 22:08
 */
public final class RobotConnector {

    /**
     * The slf4j logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RobotConnector.class);

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
            roster.setSubscriptionMode(Roster.SubscriptionMode.manual);
            PacketListener subscriptionListener = new SubscriptionListener(roster, connection);
            connection.addPacketListener(subscriptionListener, new PacketTypeFilter(Presence.class));

            final Collection<RosterEntry> entries = roster.getEntries();
            LOGGER.info(" robot {} online now.", username);

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
            LOGGER.error(String.format(" robot[%s] connect error.", username), e);
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
}
