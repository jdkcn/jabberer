package com.jdkcn.jabber.web.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jdkcn.jabber.domain.User;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;

import com.google.inject.Singleton;

import static com.jdkcn.jabber.util.Constants.*;

/**
 * @author <a href="mailto:rory.cn@gmail.com">Rory</a>
 * @version $Id$
 */
@Singleton
public class SigninServlet extends HttpServlet {

    private static final long serialVersionUID = -1108987102277577612L;

    private List<User> users = new ArrayList<User>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        JsonNode jsonConfig = (JsonNode) config.getServletContext().getAttribute(JABBERERJSONCONFIG);
        for (JsonNode adminNode : jsonConfig.get("administrators")) {
            User user = new User();
            user.setUsername(adminNode.get("username").asText());
            user.setPassword(adminNode.get("password").asText());
            List<String> manageRobots = new ArrayList<String>();
            if (adminNode.has("manage.robots")) {
                for (JsonNode jsonNode : adminNode.get("manage.robots")) {
                    manageRobots.add(jsonNode.asText());
                }
            }
            user.setManageRobots(manageRobots);
            users.add(user);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getSession().getAttribute(LOGIN_USER) == null) {
            req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
        } else {
            resp.sendRedirect(req.getContextPath() + "/");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String user = req.getParameter("user");
        String pass = req.getParameter("pass");
        boolean isOk = false;
        for (User u : users) {
            if (u.getUsername().equals(user) && u.getPassword().equals(pass)) {
                isOk = true;
            }
        }
        if (isOk) {
            req.getSession().setAttribute(LOGIN_USER, user);
            resp.sendRedirect(req.getContextPath() + "/");
        } else {
            req.setAttribute("loginError", true);
            req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
        }
    }
}
