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

    protected PageController(String pageName) {
        if (Strings.isEmpty(pageName)) {
            throw new IllegalArgumentException("Page name can't be empty");
        }
        this.pageName = pageName;
    }

    /**
     * Returns the page name registered with this controller
     */
    public String getPageName() {
        return pageName;
    }

    /**
     * Returns the path to the JSP file to be used with this controller
     * <p>
     * NOTE: The JSPs are inside of the WEB-INF directory in order to not be served upon direct access
     */
    public String getJspPath() {
        return "/WEB-INF/pages/" + pageName + ".jsp";
    }

    /**
     * (To be implemented by child class)
     * Handles a GET request, to load the page
     */
    public abstract void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

}
