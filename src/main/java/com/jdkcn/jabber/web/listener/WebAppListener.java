/**
 * Project:jabberer
 * File:WebAppListener.java
 * Copyright 2004-2012 Homolo Co., Ltd. All rights reserved.
 */
package com.jdkcn.jabber.web.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.jdkcn.jabber.web.servlet.IndexServlet;

/**
 * @author Rory
 * @date Feb 7, 2012
 * @version $Id$
 */
@WebListener
public class WebAppListener extends GuiceServletContextListener {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		super.contextInitialized(servletContextEvent);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		super.contextDestroyed(servletContextEvent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Injector getInjector() {
		return Guice.createInjector(new ServletModule(){
			/**
			 * {@inheritDoc}
			 */
			@Override
			protected void configureServlets() {
				serve("/index.jsp", "/index.html", "/").with(IndexServlet.class);
			}
		});
	}

}
