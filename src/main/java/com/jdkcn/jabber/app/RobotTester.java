package com.jdkcn.jabber.app;

import java.util.Arrays;
import java.util.Iterator;

import org.codehaus.jackson.JsonNode;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;

import com.jdkcn.jabber.robot.Robot;
import com.jdkcn.jabber.robot.RobotXmppListener;
import com.jdkcn.jabber.util.JsonUtil;

/**
 * @author <a href="mailto:rory.cn@gmail.com">Rory</a>
 * @version $Id$
 */
public class RobotTester {

	public static void main(String... args) throws Exception {
		JsonNode jsonConfig = JsonUtil.fromJson(RosterEntryCleaner.class.getResourceAsStream("/config.json"), JsonNode.class);
		for (Iterator<JsonNode> iterator = jsonConfig.get("robots").iterator(); iterator.hasNext();) {
			JsonNode robotNode = iterator.next();
			String username = robotNode.get("username").asText();
			String password = robotNode.get("password").asText();
			ConnectionConfiguration connConfig = new ConnectionConfiguration("talk.google.com", 5222, "gmail.com");
			final XMPPConnection connection = new XMPPConnection(connConfig);
			connection.connect();
			connection.login(username, password);
			Robot robot = new Robot();
			robot.setAdministratorIds(Arrays.asList("rory.cn@gmail.com"));
			connection.getChatManager().addChatListener(new RobotXmppListener(connection, robot));
			System.out.println("a");
		}
	}
}
