package com.universeprojects.web;

import com.universeprojects.common.shared.log.Logger;
import com.universeprojects.common.shared.util.Dev;
import com.universeprojects.common.shared.util.Strings;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * TODO: add javadoc
 */
public class PageControllerServlet extends HttpServlet {

    private final static Logger log = Logger.getLogger(PageControllerServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.info("GET request");
        // TODO: When the page is loaded for the first time, static resources are requested separately (CSS, JS, images)
        // TODO: I think that these N requests are routed to this method. If confirmed, the extra calls should be ignored!

        PageController controller = getController(request, response);
        if (controller == null) {
            return;
        }

        // TODO: For now, pass the request/response along to the controller
        // TODO: In the future, create a suitable abstraction to limit the controller's power
        controller.doGet(request, response);

        // The request has been pre-processed by the controller. Forward to the matching JSP.
        request.getRequestDispatcher(controller.getJspPath()).forward(request, response);
    }

    /**
     * (Helper method)
     * Looks for a controller, that matches the context path in the request URL
     *  - If a controller is found, it is returned to the caller
     *  - If an issue arises, this method takes care of appropriate error messaging in the servlet response,
     *  and NULL is returned to the caller. The caller then should do no further processing
     */
    private PageController getController(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestURI = request.getRequestURI();
        if (!requestURI.matches("/pages/\\w+")) {
            // If the URL doesn't match the expected format, redirect to root
            response.sendRedirect("/");
            return null;
        }

        String pageName = requestURI.substring(7);
        Dev.check(!Strings.isEmpty(pageName), "Looks like someone messed with the routing of page-URLs"); // regression-check

        PageController controller = ControllerRegistry.INSTANCE.getController(pageName);
        if (controller == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Page " + Strings.inQuotes(pageName) + " not found");
            return null;
        }

        return controller;
    }

}
