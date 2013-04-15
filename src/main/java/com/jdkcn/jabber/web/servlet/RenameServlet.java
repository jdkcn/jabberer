package com.jdkcn.jabber.web.servlet;

import com.google.inject.Singleton;
import com.jdkcn.jabber.robot.Robot;
import com.jdkcn.jabber.util.Constants;
import org.apache.commons.lang3.StringUtils;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * User: rory
 * Date: 4/15/13
 * Time: 09:08
 */
@Singleton
public class RenameServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String robotName = req.getParameter("robotName");
        String entry = req.getParameter("entry");
        String newName = req.getParameter("newname");
        if (StringUtils.isNotBlank(robotName) && StringUtils.isNotBlank(entry) && StringUtils.isNotBlank(newName)) {

            List<Robot> allRobots = (List<Robot>) req.getServletContext().getAttribute(Constants.ROBOTS);
            Robot robot = null;
            for (Robot r : allRobots) {
                if (StringUtils.equals(r.getName(), robotName)) {
                    robot = r;
                }
            }

            if (robot != null) {
                Roster roster = robot.getConnection().getRoster();
                RosterEntry rosterEntry = roster.getEntry(entry);
                if (rosterEntry != null) {
                    rosterEntry.setName(newName);
                }
                robot.getRosters().clear();
                robot.getRosters().addAll(roster.getEntries());
            }
        }
        resp.sendRedirect(req.getContextPath() + "/");
    }
}
