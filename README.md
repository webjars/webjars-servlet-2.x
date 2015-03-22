webjars-servlet-2.x
========
Implements a Servlet to enable Webjars resources for Servlet 2.x API compliant Java EE containers that do not support the Servlet 3.x API

# Usage

1. Register Maven dependency:

    ```
    <dependency>
        <groupId>org.webjars</groupId>
        <artifactId>webjars-servlet-2.x</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
    ```

2. Register servlet in your web.xml

    ```
    <!--Webjars Servlet-->
    <servlet>
        <servlet-name>WebjarsServlet</servlet-name>
        <servlet-class>org.webjars.servlet.WebjarsServlet</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>WebjarsServlet</servlet-name>
        <url-pattern>/webjars/*</url-pattern>
    </servlet-mapping>
    ```
