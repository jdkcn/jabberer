package com.jdkcn.jabber.domain;

import java.io.Serializable;
import java.util.List;

/**
 * User: rory
 * Date: 4/06/13
 * Time: 21:08
 */
public class User implements Serializable{

    private static final long serialVersionUID = 4372848711554418821L;

    private String username;

    private String password;

    private List<String> manageRobots;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getManageRobots() {
        return manageRobots;
    }

    public void setManageRobots(List<String> manageRobots) {
        this.manageRobots = manageRobots;
    }
}
