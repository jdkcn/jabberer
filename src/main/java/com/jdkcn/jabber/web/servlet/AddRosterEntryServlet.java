package com.jdkcn.jabber.web.servlet;

import com.google.inject.Singleton;
import com.jdkcn.jabber.robot.Robot;
import com.jdkcn.jabber.util.Constants;
import org.apache.commons.lang3.StringUtils;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * User: rory
 * Date: 4/06/13
 * Time: 23:08
 */
@Singleton
public class AddRosterEntryServlet extends HttpServlet {

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        String robotName = req.getParameter("robotName");
        String entry = req.getParameter("entry");
        String nickname = req.getParameter("nickname");
        if (StringUtils.isNotBlank(robotName) && StringUtils.isNotBlank(entry)) {
            List<Robot> allRobots = (List<Robot>) req.getServletContext().getAttribute(Constants.ROBOTS);
            Robot robot = null;
            for (Robot r : allRobots) {
                if (StringUtils.equals(r.getName(), robotName)) {
                    robot = r;
                }
            }

            if (robot != null) {
                try {
                    Roster roster = robot.getConnection().getRoster();
                    roster.createEntry(entry, nickname, null);
                    robot.getRosters().clear();
                    robot.getRosters().addAll(roster.getEntries());
                } catch (XMPPException e) {
                    e.printStackTrace();
                }
            }
        }
        ((HttpServletResponse) res).sendRedirect(((HttpServletRequest) req).getContextPath() + "/");
    }
}
