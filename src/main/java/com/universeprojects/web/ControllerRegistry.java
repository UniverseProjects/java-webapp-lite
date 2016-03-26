package com.universeprojects.web;

import com.universeprojects.common.shared.log.Logger;
import com.universeprojects.common.shared.util.Dev;
import com.universeprojects.common.shared.util.DevException;
import com.universeprojects.common.shared.util.Strings;
import org.reflections.Reflections;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Singleton class;
 * Stores a registration of all page-controllers in a system.
 *
 * This is a singleton, because it's accessed from {@link PageControllerServlet}, and there can be
 * multiple instances of the same type of servlet, in order to scale system performance.
 */
class ControllerRegistry {

    private final static Logger log = Logger.getLogger(ControllerRegistry.class);

    /** Holds the singleton instance of this class */
    public static final ControllerRegistry INSTANCE = new ControllerRegistry();

    // hidden constructor because this is a singleton
    private ControllerRegistry() {}

    /** Holds a reference to the servlet context */
    private boolean initialized = false;

    /**
     * This is called at servlet-initialization time, to detect all the page-controllers
     * that are present on the classpath, and to register them for use with the dispatcher servlet.
     *
     * @param servletConfig Reference to the ServletConfig object of PageControllerServlet
     */
    synchronized void initialize(ServletConfig servletConfig) {
        if (initialized) {
            // ignore this call, if the registry is already initialized
            return;
        }

        Dev.checkNotNull(servletConfig);
        ServletContext servletContext = servletConfig.getServletContext();

        String baseScanPackage = servletConfig.getInitParameter("baseScanPackage");
        if (Strings.isEmpty(baseScanPackage)) {
            throw new java.lang.RuntimeException("Init parameter \"baseScanPackage\" must be set in web.xml, " +
                    "on servlet " + PageControllerServlet.class.getSimpleName());
        }

        log.info("Scanning for page-controller classes in package " + Strings.inQuotes(baseScanPackage));
        Reflections reflections = new Reflections(baseScanPackage);
        Set<Class<? extends PageController>> controllerClasses = reflections.getSubTypesOf(PageController.class);

        for (Class<? extends PageController> controllerClass : controllerClasses) {
            if (!controllerClass.isAnnotationPresent(Controller.class)) {
                log.warn("Ignoring controller class " + Strings.inQuotes(controllerClass.getName()) +
                        " because it's not annotated with @" + Controller.class.getSimpleName());
                continue;
            }

            log.info("Registering controller: " + controllerClass.getSimpleName());
            final PageController controller;
            try {
                controller = controllerClass.newInstance();
            }
            catch (InstantiationException | IllegalAccessException e) {
                throw new DevException("Problem creating an instance of class " + Strings.inQuotes(controllerClass.getName()) +
                        ". The controller class must declare a public no-arg constructor " + controllerClass.getSimpleName() + "()", e);
            }

            verifyController(controller, servletContext);
            registerController(controller);
        }


        if (controllers.isEmpty()) {
            log.warn("No page-controller classes registered with the dispatcher servlet");
        }
        else {
            log.info("Registered " + controllers.size() + " page-controllers");
        }

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
    private void registerController(PageController controller) {
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

        controllers.add(controller);
        controllersByPageName.put(pageName, controller);

        log.info("Registered controller for page " + Strings.inQuotes(pageName) + ": " + controllerClassFullName);
    }

    /**
     * (Helper method)
     * Performs verification checks on a given controller. To be called before registration takes place.
     */
    private void verifyController(PageController controller, ServletContext servletContext) {
        Dev.checkNotNull(servletContext);
        Dev.checkNotNull(controller);

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
    PageController getController(String pageName) {
        if (Strings.isEmpty(pageName)) {
            throw new IllegalArgumentException("Page name can't be empty");
        }
        return controllersByPageName.get(pageName);
    }

}
