/**
 * Project:jabberer
 * File:Robot.java
 * Copyright 2004-2012 Homolo Co., Ltd. All rights reserved.
 */
package com.jdkcn.jabber.robot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jivesoftware.smack.RosterEntry;

/**
 * @author Rory
 * @date Feb 8, 2012
 * @version $Id$
 */
public class Robot implements Serializable {

	private static final long serialVersionUID = -8057823951538493009L;

	private String name;
	
	private Date startTime;
	
	private Boolean sendOfflineMessage;
	
	private List<RosterEntry> rosters;
	
	private List<RosterEntry> onlineRosters;
	
	private List<RosterEntry> administrators;
	
	private Status status;

	public String getOnlineRosterNames() {
		return parseRosterNames(getOnlineRosters(), false);
	}
	
	public String getRosterNames() {
		return parseRosterNames(getRosters(), false);
	}
	
	public String getAdministratorNames() {
		return parseRosterNames(getAdministrators(), true);
	}

	/**
	 * @param rosters
	 * @param showUser
	 * @return
	 */
	private String parseRosterNames(List<RosterEntry> rosters, boolean showUser) {
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
		return StringUtils.join(names, "，");
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

	public static enum Status {
		Online, Offline
	}
}
