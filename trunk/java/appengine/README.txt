This sample demonstrates using OAuth and Google App Engine to connect to
Google Health and perform simple actions such as profile retrieval
and notice posting.

The following instructions assume some knowledge of building, running, and
deploying Google App Engine apps.

In order to build and run this sample, the following library will need to be
downloaded and the necessary JAR copied into /war/WEB-INF/lib:

- GData Java client libraries (e.g. gdata.java-1.41.5.zip):
  http://code.google.com/p/gdata-java-client/downloads/list
  JAR: java/lib/gdata-core-1.0.jar

Connecting to Health using OAuth requires a domain name and corresponding X.509
certificate to be registered with Google.

- Health and OAuth:
  http://code.google.com/apis/health/docs/2.0/developers_guide_protocol.html#OAuth

- Domain registration overview:
  http://code.google.com/apis/health/getting_started.html#DomainRegistration

For signing requests, the application expects a Java keystore, to be available
in the /src directory.  In order for the application to be able to access this
keystore, you will need to update the filter configuration in
/war/WEB-INF/web.xml.

- Generating a Java keystore:
  http://code.google.com/apis/gdata/docs/auth/authsub.html#keytool
