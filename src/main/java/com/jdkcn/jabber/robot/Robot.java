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
package com.jdkcn.jabber.robot;

import org.apache.commons.lang3.StringUtils;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * the jabber robot.
 * @author Rory
 * @version $Id$
 * @since Feb 8, 2012
 */
public class Robot implements Serializable {

    private static final long serialVersionUID = -8057823951538493009L;

    private String name;

    private String username;

    private String password;

    private Date startTime;

    private Boolean sendOfflineMessage;

    private List<RosterEntry> rosters;

    private List<RosterEntry> onlineRosters;

    private List<RosterEntry> administrators;

    private List<String> administratorIds;

    private Status status;

    private XMPPConnection connection;

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

    public XMPPConnection getConnection() {
        return connection;
    }

    public void setConnection(XMPPConnection connection) {
        this.connection = connection;
    }

    public List<String> getAdministratorIds() {
        if (administratorIds == null) {
            administratorIds = new ArrayList<String>();
        }
        return administratorIds;
    }

    public void setAdministratorIds(List<String> administratorIds) {
        this.administratorIds = administratorIds;
    }

    public String getOnlineRosterNames() {
        return parseRosterNames(getOnlineRosters(), false);
    }

    public String getRosterNames() {
        return parseRosterNames(getRosters(), false);
    }

    public String getAdministratorNames() {
        List<String> nameList = parseRosterNameList(getAdministrators(), true);
        String names = StringUtils.join(nameList, ",");
        for (String administratorId : getAdministratorIds()) {
            if (names.indexOf(administratorId) == -1) {
                nameList.add(administratorId);
            }
        }
        return StringUtils.join(nameList, ",");
    }

    /**
     * parse the roster names from RosterEntry list.
     * @param rosters
     * @param showUser
     * @return
     */
    private String parseRosterNames(List<RosterEntry> rosters, boolean showUser) {
        List<String> names = parseRosterNameList(rosters, showUser);
        return StringUtils.join(names, "，");
    }

    /**
     * parse the roster name list from RosterEntry list.
     * @param rosters
     * @param showUser
     * @return
     */
    private List<String> parseRosterNameList(List<RosterEntry> rosters, boolean showUser) {
        List<String> names = new ArrayList<String>();
        for (RosterEntry entry : rosters) {
            String entryName = entry.getName();
            if (StringUtils.isNotBlank(entryName)) {
                if (showUser) {
                    entryName += "[" + entry.getUser() + "]";
                }
                names.add(entryName);
            } else {
                names.add(entry.getUser());
            }
        }
        return names;
    }

    public Boolean getSendOfflineMessage() {
        return sendOfflineMessage;
    }

    public void setSendOfflineMessage(Boolean sendOfflineMessage) {
        this.sendOfflineMessage = sendOfflineMessage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public List<RosterEntry> getRosters() {
        if (rosters == null) {
            rosters = new ArrayList<RosterEntry>();
        }
        return rosters;
    }

    public void setRosters(List<RosterEntry> rosters) {
        this.rosters = rosters;
    }

    public List<RosterEntry> getOnlineRosters() {
        if (onlineRosters == null) {
            onlineRosters = new ArrayList<RosterEntry>();
        }
        return onlineRosters;
    }

    public void setOnlineRosters(List<RosterEntry> onlineRosters) {
        this.onlineRosters = onlineRosters;
    }

    public List<RosterEntry> getAdministrators() {
        if (administrators == null) {
            administrators = new ArrayList<RosterEntry>();
        }
        return administrators;
    }

    public void setAdministrators(List<RosterEntry> administrators) {
        this.administrators = administrators;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * robot status enum.
     */
    public static enum Status {
        Online, Offline, LoginFailed
    }
}
