Mt Tabor Google Health Java Client Library - README.TXT
Mar 13, 2008

Overview
========
Interacting with Google Health requires using the Google data APIs ("GData"
for short), which provide a simple standard protocol for reading and writing
data on the web. GData combines common XML-based syndication formats (Atom
and RSS) with a feed-publishing system based on the Atom publishing protocol,
plus some extensions for handling queries.

Clients using Java 5 can use the Google-supplied Java client libraries for
GData (http://code.google.com/apis/gdata/client-java.html) to build
applications that interface with Google Health. This package implements
GData support for clients using Java 1.4.2.


Package information
===================
This package contains:
 o  the Java client library, its sources, and build files
 o  documentation in Javadoc format for the client library

The documentation can be found in the 'javadocs' folder. The Java client
library is in 'target', called 'mtos-health-core-2.1.2.jar'. Sources are located
in 'src'. Refer to INSTALL.txt for details on package dependencies and install
instructions. 


Known Issues
============

o This Java client library is beta and has only been tested with Java 1.4.2.

o The Google data APIs are still in beta. Please use one of the API support
Google Groups to give Google any feedback, issues, or bugs:
http://groups.google.com/group/google-help-dataapi

