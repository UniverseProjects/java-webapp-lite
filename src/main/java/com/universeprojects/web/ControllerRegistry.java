package com.universeprojects.web;

import com.universeprojects.common.shared.log.Logger;
import com.universeprojects.common.shared.util.DevException;
import com.universeprojects.common.shared.util.Strings;

import javax.servlet.ServletContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Singleton class;
 * Stores a registration of all page-controllers in a system ({@link PageController}).
 */
public class ControllerRegistry {

    private final static Logger log = Logger.getLogger(ControllerRegistry.class);

    /** Holds the singleton instance of this class */
    public static final ControllerRegistry INSTANCE = new ControllerRegistry();

    // hidden constructor because this is a singleton
    private ControllerRegistry() {}

    /** Holds a reference to the servlet context */
    private ServletContext servletContext = null;

    /**
     * This needs to be called to set the servlet context, before any controllers are registered.
     * Controller verification will fail otherwise.
     */
    public void setServletContext(ServletContext servletContext) {
        if (this.servletContext != null) {
            throw new DevException("ServletContext reference already set");
        }
        if (servletContext == null) {
            throw new IllegalArgumentException("ServletContext reference can't be null");
        }
        this.servletContext = servletContext;
    }

    /**
     * A set of the registered controllers. Used to make sure that the same controller isn't added twice.
     */
    private final Set<PageController> controllers = new HashSet<>();
    /**
     * A map of the registered controllers, with the page name as the key.
     */
    private final Map<String, PageController> controllersByPageName = new HashMap<>();


    /**
     * Registers a controller with the page-dispatch system
     */
    public void registerController(PageController controller) {
        if (controller == null) {
            throw new IllegalArgumentException("Controller can't be null");
        }
        final String controllerClassFullName = controller.getClass().getName();
        if (controllers.contains(controller)) {
            throw new IllegalArgumentException("Controller this type is already registered: " + controllerClassFullName);
        }
        final String pageName = controller.getPageName();
        if (controllersByPageName.containsKey(pageName)) {
            throw new IllegalArgumentException("Controller already registered under page name " + Strings.inQuotes(pageName));
        }

        verifyController(controller);

        controllers.add(controller);
        controllersByPageName.put(pageName, controller);

        log.info("Registered controller for page " + Strings.inQuotes(pageName) + ": " + controllerClassFullName);
    }

    /**
     * (Helper method)
     * Performs verification checks on a given controller.
     */
    private void verifyController(PageController controller) {
        if (servletContext == null) {
            throw new DevException("Servlet context reference not set. It is required for controller verification.");
        }

        String jspResourcePath = controller.getJspPath();
        URL jspUrl;
        try {
            jspUrl = servletContext.getResource(jspResourcePath);
        }
        catch (MalformedURLException e) {
            // This is indicative of a code problem
            throw new DevException("Malformed URL encountered when verifying controller: " + jspResourcePath, e);
        }

        if (jspUrl == null) {
            throw new RuntimeException("JSP not found for page " + Strings.inQuotes(controller.getPageName()) +
                    ", at path: " + jspResourcePath);
        }
    }

    /**
     * Retrieves a controleller, registered under the specified page name
     *
     * @return The controller instance, or NULL if nothing was found
     */
    public PageController getController(String pageName) {
        if (Strings.isEmpty(pageName)) {
            throw new IllegalArgumentException("Page name can't be empty");
        }
        return controllersByPageName.get(pageName);
    }

}
