webjars-servlet-2.x
========
Implements a Servlet to enable Webjars resources for Servlet 2.x API compliant Java EE containers that do not support the Servlet 3.x API

# Usage

1. Register Maven dependency:

    ```
    <dependency>
        <groupId>org.webjars</groupId>
        <artifactId>webjars-servlet-2.x</artifactId>
        <version>1.3</version>
    </dependency>
    ```

2. Register servlet in your web.xml

    ```
    <!--Webjars Servlet-->
    <servlet>
        <servlet-name>WebjarsServlet</servlet-name>
        <servlet-class>org.webjars.servlet.WebjarsServlet</servlet-class>
        <load-on-startup>2</load-on-startup>
    </servlet>
    <servlet-mapping>
        <servlet-name>WebjarsServlet</servlet-name>
        <url-pattern>/webjars/*</url-pattern>
    </servlet-mapping>
    ```

3. If you want to disable webjars resources caching you can use the `disableCache` configuration property. Eg:

    ```
    <!--Webjars Servlet-->
    <servlet>
        <servlet-name>WebjarsServlet</servlet-name>
        <servlet-class>org.webjars.servlet.WebjarsServlet</servlet-class>
        <init-param>
            <param-name>disableCache</param-name>
            <param-value>true</param-value>
        </init-param>
        <load-on-startup>2</load-on-startup>
    </servlet>
    ```
