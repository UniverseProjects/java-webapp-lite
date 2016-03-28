package com.universeprojects.web;

import com.universeprojects.common.shared.util.Strings;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * This is a base class, for a page controller
 *
 * Brief description of the system:
 *  - A page is implemented via a combination of an underlying "controller" class, and a matching JSP file
 *  - The controllers contain all the Java code, and the JSPs contain all of the markup-related stuff (HTML, CSS, etc)
 *
 * Example of a simple scenario, for a page that lists the registered members on a website:
 *  - The page is identified with a simple string identifier, "members"
 *  - The JSP has a name that matches the page identifier, "members.jsp"
 *  - The controller extends this class, and is named MembersController
 *
 */
public abstract class PageController {

    private final String pageName;
    private final String jspResourcePath;

    /**
     * Creates an instance of a controller for a page in the web-application.<br/>
     * The controller is in charge of pre-processing requests, before the actual page is displayed.
     *
     * @param pageName The name of the page (unique identifier), for example - "news"
     * @param jspResourcePath The path to the JSP that contains the page content, for example - "/WEB-INF/pages/news.jsp" <br/>
     *        It's recommended to store your JSPs in the WEB-INF directory in order to prevent direct access from the browser
     *
     */
    protected PageController(String pageName, String jspResourcePath) {
        if (Strings.isEmpty(pageName)) {
            throw new IllegalArgumentException("Page name can't be empty");
        }
        if (Strings.isEmpty(jspResourcePath) || !jspResourcePath.endsWith(".jsp")) {
            throw new IllegalArgumentException("Invalid JSP resource path: " + jspResourcePath);
        }

        this.pageName = pageName;
        this.jspResourcePath = jspResourcePath;
    }

    /**
     * Returns the page name registered with this controller
     */
    String getPageName() {
        return pageName;
    }

    /**
     * Returns the path to the JSP file to be used with this controller
     */
    String getJspPath() {
        return jspResourcePath;
    }

    /**
     * (To be implemented by child class)
     * Handles a GET request, to load the page
     */
    protected abstract void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

}
