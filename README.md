# simple-java-web (A very lightweight Java web-framework)

Follow the instructions below to set up a sample page.

#### 1. Set up the framework in your web.xml (you only need to do this once)

The setup below assumes that all your controllers will reside in package "com.mywebsite.controllers".

```xml
<!-- Page controller servlet (locates controllers for various pages) -->
<filter>
  <filter-name>PageControllerFilter</filter-name>
  <filter-class>com.universeprojects.web.PageControllerFilter</filter-class>
  <init-param>
    <param-name>uriPrefix</param-name>
    <param-value>/</param-value>
  </init-param>
  <init-param>
    <param-name>baseScanPackage</param-name>
    <param-value>com.mywebsite.controllers</param-value>
  </init-param>
</filter>
<filter-mapping>
  <filter-name>PageControllerFilter</filter-name>
  <url-pattern>/*</url-pattern>
</filter-mapping>
```

#### 2. Create a page controller

```java
package com.mywebsite.controllers;

import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Controller
public class HelloController extends PageController {
  super("hello");
}

@Override
protected final String processRequest(HttpServletRequest request, HttpServletResponse response) 
  throws ServletException, IOException {
  
  // set request attributes, to be accessible in the JSP page
  request.setAttribute("message", "Hello World!");
  
  return "/WEB-INF/pages/hello.jsp";
}

```

#### 3. Create JSP file /WEB-INF/pages/hello.jsp

```html
<!DOCTYPE html>
<html lang="en">
  <head>
    <title>My Website</title>
  </head>
  <body>
    <h1>My Website</h1>
    <h3 style="color: red">Message: ${message}</h3>
  </body>
</html>
```

#### 4. Access your page

After the application has been deployed, the sample page will be accessible at URL "<deploymnet root>/hello"

