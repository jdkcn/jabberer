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

import org.apache.commons.lang3.StringUtils;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Robot message listener for xmpp message deal.
 *
 * @author <a href="mailto:rory.cn@gmail.com">Rory</a>
 */
public class RobotMessageListener implements MessageListener {

    /**
     * THe constructor with robot and sendOfflineMessage.
     * @param robot
     * @param sendOfflineMessage
     */
    public RobotMessageListener(Robot robot, Boolean sendOfflineMessage) {
        this.robot = robot;
        if (sendOfflineMessage != null) {
            this.sendOfflineMessage = sendOfflineMessage;
        }
    }

    /**
     *
     */
    private final Logger logger = LoggerFactory.getLogger(RobotMessageListener.class);

    /**
     * The robot for this message listener.
     */
    private Robot robot;

    /**
     * The send offline message or not.
     */
    private boolean sendOfflineMessage = true;

    /**
     * The robot's command list.
     */
    private static final List<String> commandList;

    static {
        commandList = new ArrayList<String>();
        commandList.add("/help");
        commandList.add("/h");
        commandList.add("/?");
        commandList.add("/list");
        commandList.add("/l");
        commandList.add("/online");
        commandList.add("/o");
        commandList.add("/add");
        commandList.add("/name");
        commandList.add("/remove");
    }

    /**
     * {@inheritDoc}
     *
     * @see org.jivesoftware.smack.MessageListener#processMessage(org.jivesoftware.smack.Chat, org.jivesoftware.smack.packet.Message)
     */
    @Override
    public void processMessage(Chat chat, Message message) {
        String body = StringUtils.trim(message.getBody());
        String from = message.getFrom();
        logger.info("process message from:" + from);
        String name = org.jivesoftware.smack.util.StringUtils.parseName(from);
        String server = org.jivesoftware.smack.util.StringUtils.parseServer(from);
        String sender = name + "@" + server;
        if (message.getType() == Message.Type.chat && StringUtils.isNotBlank(body)) {
            boolean isCommand = false;
            for (String command : commandList) {
                if (body.toLowerCase().startsWith(command)) {
                    isCommand = true;
                    break;
                }
            }
            if (isCommand) {
                processCommand(chat, body, sender);
            } else {
                int size = 50;
                logger.info(StringUtils.center(" sending start ", size, "#"));
                for (RosterEntry entry : robot.getConnection().getRoster().getEntries()) {
                    if (entry.getUser().equalsIgnoreCase(sender)) {
                        continue;
                    }
                    Presence presence = robot.getConnection().getRoster().getPresence(entry.getUser());
                    if (!sendOfflineMessage && !presence.isAvailable()) {
                        continue;
                    }
                    logger.info("sending to :" + entry.getUser() + "[" + entry.getName() + "]");
                    Message msg = new Message(entry.getUser(), Message.Type.chat);
                    msg.setBody("<" + findPosterName(sender, name) + "> " + body);
                    try {
//						final MessageListener messageListener = new RobotMessageListener(connection, roster, rosterEntries, sendOfflineMessage, robot);
                        robot.getConnection().getChatManager().createChat(entry.getUser(), this).sendMessage(msg);
                    } catch (XMPPException e) {
                        logger.error("send message to:" + entry.getUser(), e);
                    }
                }
                logger.info(StringUtils.center(" sent done ", size, "#"));
            }
        }
    }

    /**
     * @param sender
     * @param name
     * @return
     */
    private String findPosterName(String sender, String name) {
        for (RosterEntry entry : robot.getConnection().getRoster().getEntries()) {
            if (entry.getUser().equalsIgnoreCase(sender)) {
                if (StringUtils.isNotBlank(entry.getName())) {
                    return entry.getName();
                }
            }
        }
        return name;
    }

    private void processCommand(Chat chat, String command, String sender) {
        if ("/l".equalsIgnoreCase(command) || "/list".equalsIgnoreCase(command)) {
            Message message = new Message(sender, Message.Type.chat);
            message.setBody("\nUsers：\n" + getRosterEntryNames(" \n"));
            try {
                chat.sendMessage(message);
            } catch (XMPPException e) {
                e.printStackTrace();
            }
        } else if ("/?".equalsIgnoreCase(command) || "/h".equalsIgnoreCase(command) || "/help".equalsIgnoreCase(command)) {
            Message message = new Message(sender, Message.Type.chat);
            StringBuffer sb = new StringBuffer();
            sb.append("\n Jabberer Help：\n");
            sb.append("\t /help /h /? for help message \n");
            sb.append("\t /list /l to list all friends. \n");
            sb.append("\t /online /o to list online friends. \n");
            sb.append("\t /add <account> <nickname> [groupname]... to add a user as friends. \n");
            sb.append("\t /remove <account> to remove a user. \n");
            sb.append("\t /name <account> <nickname> set a nickname to a user. \n");
            message.setBody(sb.toString());
            try {
                chat.sendMessage(message);
            } catch (XMPPException e) {
                e.printStackTrace();
            }
        } else if ("/online".equalsIgnoreCase(command) || "/o".equalsIgnoreCase(command)) {
            Message message = new Message(sender, Message.Type.chat);
            message.setBody("\n Online Users:\n" + getRosterEntryNames(true, "\n"));
            try {
                chat.sendMessage(message);
            } catch (XMPPException e) {
                e.printStackTrace();
            }
        } else if (command.startsWith("/add")) {
            if (robot.getAdministratorNames().indexOf(sender) == -1) {
                sendNoPermissionMessage(chat, sender);
            } else {
                String[] args = StringUtils.split(command.substring(4), " ");
                if (args == null || args.length == 0) {
                    Message message = new Message(sender, Message.Type.chat);
                    message.setBody("\n args wrong please use:\n \t /add <account> <nickname> [groupname]...");
                    try {
                        chat.sendMessage(message);
                    } catch (XMPPException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        String nickname = null;
                        if (args.length > 1) {
                            nickname = args[1].trim();
                        }
                        String[] groups = null;
                        if (args.length > 2) {
                            groups = Arrays.copyOfRange(args, 2, args.length);
                        }
                        Roster roster = robot.getConnection().getRoster();
                        roster.createEntry(args[0].trim(), nickname, groups);
                        robot.getRosters().clear();
                        robot.getRosters().addAll(roster.getEntries());
                    } catch (XMPPException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (command.startsWith("/name")) {
            if (robot.getAdministratorNames().indexOf(sender) == -1) {
                sendNoPermissionMessage(chat, sender);
            } else {
                String[] args = StringUtils.split(command.substring(5), " ");
                if (args == null || args.length < 2) {
                    Message message = new Message(sender, Message.Type.chat);
                    message.setBody("\n args wrong please use:\n \t /name <account> <nickname>");
                    try {
                        chat.sendMessage(message);
                    } catch (XMPPException e) {
                        e.printStackTrace();
                    }
                } else {
                    Roster roster = robot.getConnection().getRoster();
                    RosterEntry rosterEntry = roster.getEntry(args[0].trim());
                    if (rosterEntry != null) {
                        rosterEntry.setName(args[1]);
                    }
                    robot.getRosters().clear();
                    robot.getRosters().addAll(roster.getEntries());
                }
            }
        } else if (command.startsWith("/remove")) {
            if (robot.getAdministratorNames().indexOf(sender) == -1) {
                sendNoPermissionMessage(chat, sender);
            } else {
                String[] args = StringUtils.split(command.substring(7), " ");
                if (args == null || args.length < 1) {
                    Message message = new Message(sender, Message.Type.chat);
                    message.setBody("\n args wrong please use:\n \t /remove <account>");
                    try {
                        chat.sendMessage(message);
                    } catch (XMPPException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Roster roster = robot.getConnection().getRoster();
                        RosterEntry rosterEntry = roster.getEntry(args[0]);
                        if (rosterEntry != null) {
                            roster.removeEntry(rosterEntry);
                            Presence presence = new Presence(Presence.Type.unsubscribe);
                            presence.setTo(rosterEntry.getUser());
                            robot.getConnection().sendPacket(presence);
                        }
                        robot.getRosters().clear();
                        robot.getRosters().addAll(roster.getEntries());
                    } catch (XMPPException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * @param chat
     * @param sender
     */
    private void sendNoPermissionMessage(Chat chat, String sender) {
        Message message = new Message(sender, Message.Type.chat);
        message.setBody("\n no permission to add more entry.");
        try {
            chat.sendMessage(message);
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }

    private String getRosterEntryNames(String separator) {
        return getRosterEntryNames(false, separator);
    }

    private String getRosterEntryNames(boolean onlineOnly, String separator) {
        StringBuffer sb = new StringBuffer();
        for (RosterEntry entry : robot.getConnection().getRoster().getEntries()) {
            if (onlineOnly) {
                Presence presence = robot.getConnection().getRoster().getPresence(entry.getUser());
                if (!presence.isAvailable()) {
                    continue;
                }
            }
            if (StringUtils.isNotBlank(entry.getName())) {
                sb.append(entry.getName());
            }
            sb.append(" [").append(entry.getUser()).append("]").append(separator);
        }
        return "\t" + sb.toString();
    }


}