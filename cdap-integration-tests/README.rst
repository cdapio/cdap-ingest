==============================================================
Cask Data Application Platform (CDAP) Ingest Integration Tests
==============================================================

This project contains set of integration tests for various CDAP ingest project components.
Tests are executed against running CDAP instance. Connection information is configured in
properties files. These files are stored in the resource directory in each sub-module. 

You can launch the same integration tests with different configuration files using maven
profiles::

  it-local
  it-local-auth
  it-local-auth-ssl
  it-remote
  it-remote-auth
  it-remote-auth-ssl

Usage
=====

To launch integration tests against Standalone CDAP instance, execute::

  mvn clean install -P it-local


To launch integration tests against Standalone CDAP instance with authentication enabled,
execute::

  mvn clean install -P it-local-auth


To launch integration tests against Standalone CDAP instance with authentication enabled
and ssl turned on, execute::

  mvn clean install -P it-local-auth-ssl


To launch integration tests against Distributed CDAP instance, execute::

  mvn clean install -P it-remote


To launch integration tests against Distributed CDAP instance with authentication enabled,
execute::

  mvn clean install -P it-remote-auth


To launch integration tests against Distributed CDAP instance with authentication enabled
and ssl turned on, execute::

  mvn clean install -P it-remote-auth-ssl


License and Trademarks
----------------------
Copyright © 2014-2015 Cask Data, Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the 
License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
either express or implied. See the License for the specific language governing permissions 
and limitations under the License.

Cask is a trademark of Cask Data, Inc. All rights reserved.

Apache, Apache HBase, and HBase are trademarks of The Apache Software Foundation. Used with
permission. No endorsement by The Apache Software Foundation is implied by the use of these marks.
