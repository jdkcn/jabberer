/**
 * Project:jabberer
 * File:MyGuiceFilter.java
 * Copyright 2004-2012 Homolo Co., Ltd. All rights reserved.
 */
package com.jdkcn.jabber.web.filter;

import javax.servlet.annotation.WebFilter;

import com.google.inject.servlet.GuiceFilter;

/**
 * @author Rory
 * @date Feb 7, 2012
 * @version $Id$
 */
@WebFilter(filterName = "Guice Filter", urlPatterns = {"/*"})
public class MyGuiceFilter extends GuiceFilter {

}
