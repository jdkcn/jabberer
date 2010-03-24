#!/bin/sh
mvn compile && mvn exec:java -Dexec.mainClass=com.jdkcn.jabber.app.Jabberer