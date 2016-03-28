package com.universeprojects.web;

import com.universeprojects.common.shared.log.Logger;
import com.universeprojects.common.shared.util.Dev;
import com.universeprojects.common.shared.util.Strings;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This servlet takes care of routing incoming requests to appropriate page controllers.
 * The system only needs this single servlet to function, regardless of how many controllers are defined.
 */
public class PageControllerServlet extends HttpServlet {

    private final static Logger log = Logger.getLogger(PageControllerServlet.class);

    private String uriPrefix;

    @Override
    public void init() throws ServletException {
        ServletConfig servletConfig = getServletConfig();
        String servletName = servletConfig.getServletName();

        // Verify the servlet config parameters, that were supposed to be set in web.xml
        String uriPrefix = servletConfig.getInitParameter("uriPrefix");
        if (Strings.isEmpty(uriPrefix)) {
            throw new RuntimeException("Servlet init parameter \"uriPrefix\" for servlet " + servletName + " must be set in web.xml");
        }
        if (!uriPrefix.startsWith("/") || !uriPrefix.endsWith("/")) {
            throw new RuntimeException("Servlet init parameter \"uriPrefix\" for servlet " + servletName + " must begin and end with '/'");
        }
        this.uriPrefix = uriPrefix;

        String baseScanPackage = servletConfig.getInitParameter("baseScanPackage");
        if (Strings.isEmpty(baseScanPackage)) {
            throw new RuntimeException("Servlet init parameter \"baseScanPackage\" for servlet " + servletName + " must be set in web.xml");
        }

        // This will detect & register all controllers that are on the classpath
        ControllerRegistry.INSTANCE.initialize(servletConfig);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug("Processing GET request");
        // TODO: When the page is loaded for the first time, static resources are requested separately (CSS, JS, images)
        // TODO: I think that these N requests are routed to this method. If confirmed, the extra calls should be ignored!

        PageController controller = getController(request, response);
        if (controller == null) {
            // controller not found for this URL - abort
            return;
        }

        // TODO: For now, pass the request/response along to the controller
        // TODO: In the future, create a suitable abstraction to limit the controller's power
        String targetJspResourcePath = controller.doGet(request, response);
        if (Strings.isEmpty(targetJspResourcePath)) {
            // NULL or "empty" return value indicates that we do not want to display the target page,
            // due to something that was established during the processing of this request. It is assumed
            // that the request object now knows what to do next (URL redirect, HTTP error, etc.)
            return;
        }

        // The request has been processed by the controller without issues, and now we're ready to display the target JSP
        request.getRequestDispatcher(targetJspResourcePath).forward(request, response);
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
        if (!requestURI.matches(uriPrefix + "\\w+")) {
            // If the URL doesn't match the expected format, redirect to root
            response.sendRedirect("/");
            return null;
        }

        String pageName = requestURI.substring(uriPrefix.length());
        Dev.check(!Strings.isEmpty(pageName), "Looks like someone messed with page-controller URL routing"); // regression-check

        PageController controller = ControllerRegistry.INSTANCE.getController(pageName);
        if (controller == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Page " + Strings.inQuotes(pageName) + " not found");
            return null;
        }

        return controller;
    }

}
