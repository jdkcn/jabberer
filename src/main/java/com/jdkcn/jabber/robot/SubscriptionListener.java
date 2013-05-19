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

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deal the subscription manual.
 *
 * User: rory
 * Date: 5/02/13
 * Time: 22:08
 */
public class SubscriptionListener implements PacketListener {

    /**
     * The slf4j logger.
     */
    private final Logger logger = LoggerFactory.getLogger(SubscriptionListener.class);

    /**
     * The roster.
     */
    private Roster roster;
    /**
     * The xmpp connection.
     */
    private Connection connection;

    /**
     * Constructor with roster and connection.
     *
     * @param roster     the roster.
     * @param connection the xmpp connection.
     */
    public SubscriptionListener(Roster roster, Connection connection) {
        this.roster = roster;
        this.connection = connection;
    }

    @Override
    public void processPacket(Packet packet) {
        if (packet instanceof Presence) {
            Presence presence = (Presence) packet;
            String from = presence.getFrom();
            if (presence.getType() == Presence.Type.subscribe) {
                logger.info("receive a subscribe presence.");
                if (roster.contains(from)) {
                    // Accept the subscription request.
                    Presence response = new Presence(Presence.Type.subscribed);
                    response.setTo(from);
                    connection.sendPacket(response);
                    logger.info("Accept the subscription request from:" + from);
                } else {
                    // Reject the subscription request.
                    Presence response = new Presence(Presence.Type.unsubscribed);
                    response.setTo(from);
                    connection.sendPacket(response);
                    logger.info("Reject the subscription request from:" + from);
                }
            } else if (presence.getType() == Presence.Type.unsubscribe) {
                logger.info("Unsubscribe from:" + from);
                Presence response = new Presence(Presence.Type.unsubscribed);
                response.setTo(from);
                connection.sendPacket(response);
            }
        }
    }

}
