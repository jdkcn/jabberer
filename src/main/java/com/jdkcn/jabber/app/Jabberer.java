/**
 * Copyright (c) 2002-2010, Jdkcn.com
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
 *     * Neither the name of the <ORGANIZATION> nor the names of its contributors may 
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
 *
 * Project:jabberer
 */
package com.jdkcn.jabber.app;

import java.io.Console;
import java.util.Collection;
import java.util.Properties;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.packet.Presence;

import com.jdkcn.jabber.listener.BotMessageListener;

/**
 * @author <a href="mailto:rory.cn@gmail.com">Rory, Ye</a>
 * @since Mar 16, 2010 11:12:26 PM
 * @version $Id$
 */
public final class Jabberer {

	public static void main(String[] args) throws Exception {
		Properties properties = new Properties();
		properties.load(Jabberer.class.getResourceAsStream("/bot.properties"));
		String username = properties.getProperty("username");
		String password = properties.getProperty("password");
		String botStatusMessage = properties.getProperty("bot.status.message");
		Boolean sendOfflineMessage = Boolean.valueOf(properties.getProperty("send.offline.message"));
		ConnectionConfiguration connConfig = new ConnectionConfiguration("talk.google.com", 5222, "gmail.com");
		final XMPPConnection connection = new XMPPConnection(connConfig);
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
		Thread.sleep(50000000);
		System.out.println("Use 'quit' or 'exit' to disconnect");
		Console console = System.console();
		String command = console.readLine();
		while (!"exit".equalsIgnoreCase(command) && !"quit".equalsIgnoreCase(command)) {
			System.out.println("please use 'quit' or 'exit' to disconnect.");
			command = console.readLine();
		}
		System.out.println("quit this bot, disconnect from server.");
		connection.disconnect();
	}
}
