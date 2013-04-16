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
        User checkedUser = null;
        for (User u : users) {
            if (u.getUsername().equals(user) && u.getPassword().equals(pass)) {
                checkedUser = u;
            }
        }
        if (checkedUser != null) {
            req.getSession().setAttribute(LOGIN_USER, checkedUser);
            resp.sendRedirect(req.getContextPath() + "/");
        } else {
            req.setAttribute("loginError", true);
            req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
        }
    }
}
