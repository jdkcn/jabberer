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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;

import com.jdkcn.jabber.util.JsonUtil;

/**
 * @author <a href="mailto:rory.cn@gmail.com">Rory</a>
 * @since Mar 18, 2010 10:00:57 AM
 * @version $Id$
 */
public class RosterEntryCleaner {

	public static void main(String[] args) throws Exception {
		JsonNode jsonConfig = JsonUtil.fromJson(RosterEntryCleaner.class.getResourceAsStream("/config.json"), JsonNode.class);
		List<String> friends = new ArrayList<String>();
		friends.add("friends1@gmail.com");
		friends.add("friends2@gmail.com");
		friends.add("friends3@gmail.com");
		String username = jsonConfig.get("username").asText();;
		String password = jsonConfig.get("password").asText();
		ConnectionConfiguration connConfig = new ConnectionConfiguration("talk.google.com", 5222, "gmail.com");
		final XMPPConnection connection = new XMPPConnection(connConfig);
		connection.connect();
		connection.login(username, password);
		Presence presence = new Presence(Presence.Type.available);
		connection.sendPacket(presence);

		Roster roster = connection.getRoster();
		Collection<RosterEntry> rosterEntries = roster.getEntries();
		for (RosterEntry rosterEntry : rosterEntries) {
			if (!friends.contains(rosterEntry.getUser())) {
				System.out.println(rosterEntry.getUser() + "----- not friend.");
				roster.removeEntry(rosterEntry);
			}
		}
		connection.disconnect();
	}
}
