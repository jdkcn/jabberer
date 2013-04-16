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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jdkcn.jabber.domain.User;
import org.codehaus.jackson.JsonNode;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;

import com.google.inject.Singleton;
import com.jdkcn.jabber.robot.Robot;
import com.jdkcn.jabber.util.Constants;

/**
 * @author Rory
 * @version $Id$
 * @date Feb 7, 2012
 */
@Singleton
public class IndexServlet extends HttpServlet {

    private static final long serialVersionUID = -4585928956316091202L;

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonNode jsonConfig = (JsonNode) req.getServletContext().getAttribute(Constants.JABBERERJSONCONFIG);
        User loginUser = (User) req.getSession().getAttribute(Constants.LOGIN_USER);
        List<Robot> allRobots = (List<Robot>) req.getServletContext().getAttribute(Constants.ROBOTS);
        List<Robot> robots = new ArrayList<Robot>();
        Boolean allRobotsOnline = true;
        for (Robot robot : allRobots) {
            XMPPConnection connection = robot.getConnection();
            if (connection != null && connection.isConnected()) {
                robot.getOnlineRosters().clear();
                Roster roster = connection.getRoster();
                for (RosterEntry entry : roster.getEntries()) {
                    if (roster.getPresence(entry.getUser()).isAvailable() && !robot.getOnlineRosters().contains(entry)) {
                        robot.getOnlineRosters().add(entry);
                    }
                }
            } else {
                robot.getOnlineRosters().clear();
                allRobotsOnline = false;
            }
            if (!loginUser.getManageRobots().isEmpty() && loginUser.getManageRobots().contains(robot.getName())) {
                robots.add(robot);
            }
        }
        req.setAttribute("allRobotsOnline", allRobotsOnline);
        req.setAttribute("robots", robots);
        req.setAttribute("jsonConfig", jsonConfig);
        req.getRequestDispatcher("/WEB-INF/jsp/index.jsp").forward(req, resp);
    }

}
