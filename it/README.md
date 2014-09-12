CDAP Ingest Integration Tests
==================

This project contains set of integration tests for various CDAP ingest project components.
Tests are executed against running CDAP instance. Connection information is configured in properties files.
This files are stored in resources directory per each sub-module. You can launch same integration tests with different 
configuration file using maven profiles: it-local, it-local-auth, it-remote and it-remote-auth

## Usage

 To launch integration tests against local CDAP instance execute:
 
 ```
 mvn clean install -P it-local
 ```

 To launch integration tests against local CDAP instance with authentication enabled, execute:
 
 ```
 mvn clean install -P it-local-auth
 ```
 
 To launch integration tests against remote CDAP instance execute:
 
 ```
 mvn clean install -P it-remote
 ```

 To launch integration tests against remote CDAP instance with authentication enabled, execute:
 
 ```
 mvn clean install -P it-remote-auth
 ```
 