/**
 * Project:jabberer
 * File:IndexServlet.java
 * Copyright 2004-2012 Homolo Co., Ltd. All rights reserved.
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
