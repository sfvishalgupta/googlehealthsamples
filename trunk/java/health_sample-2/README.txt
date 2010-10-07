This sample demonstrates using AuthSub to connect to Google Health and perform
simple actions such as profile retrieval, notice retrieval, and notice posting.

The following instructions assume some knowledge of building, deploying 
and running Java web applications.

In order to build and run this sample, the following libraries will need to be
downloaded and the necessary JARs copied into /WebContent/lib:

- Guava library (e.g. guava-r07.zip):
  http://code.google.com/p/guava-libraries/downloads/list
  JAR: guava-r07.jar

- JSTL API and implementation libraries:
  https://jstl.dev.java.net/download.html
  JARs: jstl-api-1.2.jar and jstl-impl-1.2.jar
  
- GData Java client libraries (e.g. gdata.java-1.41.5.zip):
  http://code.google.com/p/gdata-java-client/downloads/list
  JAR: java/lib/gdata-core-1.0.jar
