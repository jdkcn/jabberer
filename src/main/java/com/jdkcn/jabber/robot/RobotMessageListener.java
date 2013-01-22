/**
 * Project:jabberer
 * Copyright 2004-2010 Homolo Co., Ltd. All rights reserved.
 */
package com.jdkcn.jabber.robot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.Presence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

/**
 * @author <a href="mailto:rory.cn@gmail.com">Rory</a>
 * @since Mar 17, 2010 2:40:32 PM
 * @version $Id$
 */
public class RobotMessageListener implements MessageListener {
	
	private final Logger logger = LoggerFactory.getLogger(RobotMessageListener.class);

	private Robot robot;
	
	private boolean sendOfflineMessage = true;
	
	private static final List<String> commandList;
	
	private Cache<String, Message> sentMessageCache;
	
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	
	private final BlockingQueue<RetryTask> retryQueue = new ArrayBlockingQueue<RetryTask>(1000);
	
	private CacheLoader<String, Message> cacheLoader = new MessageCacheLoader();
	
	class MessageCacheLoader extends CacheLoader<String, Message> {
		private Chat chat;
		
		private Message message;
		
		public void setChat(Chat chat) {
			this.chat = chat;
		}
		
		public void setMessage(Message message) {
			this.message = message;
		}
		
		@Override
		public Message load(String key) throws Exception {
			if (chat != null && message != null) {
				
			}
			return null;
		}
		
	}
	
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
	
	public void setSendOfflineMessage(boolean sendOfflineMessage) {
    	this.sendOfflineMessage = sendOfflineMessage;
    }

	public RobotMessageListener(Robot robot, Boolean sendOfflineMessage) {
		this.robot = robot;
		if (sendOfflineMessage != null) {
			this.sendOfflineMessage = sendOfflineMessage; 
		}
		executor.execute(new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						logger.debug("running :");
						RetryTask task = retryQueue.take();
						logger.debug("msg :" + task.getMessage().getBody());
						Message retryMessage = new Message(task.getChat().getParticipant(), Type.chat);
						retryMessage.setBody("retry send on " + DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss") + "\n" + task.getMessage().getBody());
						task.getChat().sendMessage(retryMessage);
						Thread.sleep(30000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (XMPPException e) {
						e.printStackTrace();
					}
				}
			}
		});
		sentMessageCache = CacheBuilder.newBuilder().maximumSize(2000).build();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.jivesoftware.smack.MessageListener#processMessage(org.jivesoftware.smack.Chat, org.jivesoftware.smack.packet.Message)
	 */
	@Override
	public void processMessage(Chat chat, Message message) {
		//if message is failed, need to retry.
		if (message.getType() == Message.Type.error) {
			try {
				RetryTask task = new RetryTask();
				task.setChat(chat);
				task.setMessage(message);
				retryQueue.put(task);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
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
				logger.info(StringUtils.center(" sending start ", 50, "#"));
				for (RosterEntry entry : robot.getConnection().getRoster().getEntries()) {
					if (entry.getUser().equalsIgnoreCase(sender)) {
						continue;
					}
					Presence presence = robot.getConnection().getRoster().getPresence(entry.getUser());
					if (!sendOfflineMessage && !presence.isAvailable()) {
						continue;
					}
					logger.info("sending to :" + entry.getUser() + "[" + entry.getName() + "]" + body);
					Message msg = new Message(entry.getUser(), Message.Type.chat);
					msg.setBody("<" + findPosterName(sender, name) + "> " + body);
					try {
//						final MessageListener messageListener = new RobotMessageListener(connection, roster, rosterEntries, sendOfflineMessage, robot);
						robot.getConnection().getChatManager().createChat(entry.getUser(), this).sendMessage(msg);
					} catch (XMPPException e) {
						logger.error("send message to:" + entry.getUser(), e);
					}
				}
				logger.info(StringUtils.center(" sent done ", 50, "#"));
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
					roster.setSubscriptionMode(SubscriptionMode.reject_all);
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
						}
						roster.setSubscriptionMode(SubscriptionMode.reject_all);
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
		for(RosterEntry entry : robot.getConnection().getRoster().getEntries()) {
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
