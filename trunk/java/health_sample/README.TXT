This sample demonstrates using AuthSub to connect to Google Health and perform
simple actions such as profile retrieval, notice retrieval, and notice posting.

The following instructions assume some knowledge of building, deploying 
and running Java web applications.

In order to build and run this sample, there are a number of dependent
libraries required:
* Sun Java 1.5 or higher environment
* GData Java Client Library:
  * URL:
      http://code.google.com/apis/gdata/
  * Definitions in build.properties:
      gdata_java_client_lib_client.jar
* Sun's Java Activation Framework (JAF)
  * URL:
      http://java.sun.com/products/javabeans/jaf/downloads/index.html
  * Definitions in build.properties:
      activation.jar
* Sun's Java Mail API
  * URL:
      http://java.sun.com/products/javamail/download.html
  * Definitions in build.properties:
      mail.jar
* Sun's Servlet API library
  * URL:
      http://java.sun.com/products/servlet/download.html
  * Definitions in build.properties:
      servlet.jar
* Jakarta Commons libraries 
  * URL:
      http://jakarta.apache.org/commons/
  * Libraries:
      collections
      configuration
      lang
  * Definitions in build.properties:
      commons-collections.jar
      commons-configuration.jar
      commons-lang.jar
* Jakarta Standard 1.1 Taglib
  * URL:
      http://jakarta.apache.org/site/downloads/downloads_taglibs-standard.cgi
  * Definitions in build.properties:
      jstl.jar
      standard.jar

There are also several tool/application dependencies:
* Apache Ant
  * URL: 
      http://ant.apache.org/
* Servlet container such as Apache Tomcat or full app server such as JBoss
  * URLs:
      http://tomcat.apache.org/
      http://www.jboss.org/downloads/index

Before you begin, please do the following: 
1) Update build.properties to point to the correct location of the 
   dependencies.  By default, the appropriate jar files should be placed
   in /scratch/gdata_java.  Please see build.properties for a list of all
   jar files required.

To build, run ant.  A war file should be produced in the deploy directory.  
Deploy this file to your servlet container.  Tomcat's default configuration, 
for example, will auto-deploy the war file if it is copied to <root>/webapps.

You should then be able to access this application at:
http://hostname:port/HealthSample

NOTE: This web application curently doesn't have much error feedback to the
end-user visiting the application.  Most exceptions are caught and logged
to stderr, so please look at the server error logs if you are experiencing
any problems with this application.  In Tomcat, for instance, these errors
would be logged to <catalina_root>/logs/catalina.out.
