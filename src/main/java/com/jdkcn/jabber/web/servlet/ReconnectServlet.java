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
package com.jdkcn.jabber.web.servlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Singleton;
import com.jdkcn.jabber.robot.Robot;
import com.jdkcn.jabber.robot.RobotConnector;
import com.jdkcn.jabber.util.Constants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * The reconnect servlet.
 *
 * @author Rory
 * @version $Id$
 * Time Feb 8, 2012
 */
@Singleton
public class ReconnectServlet extends HttpServlet {

    /**
     * The serial version uid.
     */
    private static final long serialVersionUID = 9193217606790233091L;

    /**
     * The slf4j logger.
     */
    private final Logger logger = LoggerFactory.getLogger(ReconnectServlet.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String robotName = req.getParameter("robot");
        @SuppressWarnings("unchecked")
        List<Robot> robots = (List<Robot>) req.getServletContext().getAttribute(Constants.ROBOTS);
        Robot reconnectRobot = null;

        for (Robot robot : robots) {
            if (StringUtils.equals(robot.getName(), robotName)) {
                reconnectRobot = robot;
            }
        }
        if (reconnectRobot != null) {
            robots.remove(reconnectRobot);
        }
        if (reconnectRobot != null) {
            try {
                if (reconnectRobot.getConnection() != null && reconnectRobot.getConnection().isConnected()) {
                    reconnectRobot.getConnection().disconnect();
                }
                JsonNode jsonConfig = (JsonNode) req.getServletContext().getAttribute(Constants.JABBERERJSONCONFIG);
                for (JsonNode node : jsonConfig.get("robots")) {
                    if (node.get("name").asText().equalsIgnoreCase(robotName)) {
                        reconnectRobot = RobotConnector.connect(node);
                    }
                }
            } catch (Exception e) {
                logger.error("reconnect robot failed:", e);
            }
            robots.add(reconnectRobot);
        }
        req.getServletContext().setAttribute(Constants.ROBOTS, robots);
        resp.sendRedirect(req.getContextPath() + "/");
    }

}
