authentication-client-java
==========================

The Authentication Client Java API is for fetching the access token from the authentication server.

## Supported Actions

 - fetch a access token from the authentication server with credentials, which are supported by the active 
 authentication mechanism;
 - check is the authentication enabled in the gateway server;
 - invalidate cached access token. 
 
 Current implementation supports three authentication mechanisms:
  - Basic Authentication;
  - LDAP;
  - JAASPI.
 
 Also, is possible to extend existing logic and implement a custom client for any other authentication 
 mechanisms. To create a new authentication client, implement the ```AuthenticationClient``` interface.   

## Build
 
 To build the Authentication Client Java API jar, use:

 ```mvn package``` or ``` mvn package -DskipTests```

## Usage

 To use the Authentication Client Java API, include this Maven dependency in your project's ```pom.xml``` file:
 
 <dependency>
  <groupId>co.cask.cdap</groupId>
  <artifactId>authentication-client-java</artifactId>
  <version>1.0-SNAPSHOT</version>
 </dependency>
 
## Example
   
 Create a ```BasicAuthenticationClient``` instance, specifying the fields 'host' and 'port' of the gateway server
 and supported credentials (username and password for the basic authentication mechanism). 
 Optional property that can be set (and its default value):
  
  - ssl: false (use HTTP protocol) 
 
 ```
   AuthenticationClient authenticationClient = 
                        new BasicAuthenticationClient("localhost", 10009, "admin", "realtime");
 ```
      
 or specified SSL using the constructor parameters:
 
 ```
   AuthenticationClient authenticationClient = 
                           new BasicAuthenticationClient("localhost", 10009, false, "admin", "realtime");
 ```
 
 Check is authentication enabled in the gateway server:
 
 ```
  boolean isEnabled = authenticationClient.isAuthEnabled();
 ```                      
 
 Get the access token for the user with *username:"admin"* and *password:"realtime"* from the authentication server:
 
 ```  
   String token = authenticationClient.getAccessToken();  
 ```
 
 **Note:** If authentication is disabled in the gateway server, ```getAccessToken();``` method returns empty string, 
 but no need to call this method, if before ```isAuthEnabled();``` method was called and it returned **false** results.
   
## Additional Notes
 
 ```getAccessToken();``` methods from the ```BasicRestAuthenticationClient``` throw exceptions using response code 
 analysis from the authentication server. These exceptions help determine if the request was processed unsuccessfully 
 and what was a reason of.
 
 All cases, except a **200 OK** response, will throw these exceptions:
 
  - **400 Bad Request**: *javax.ws.rs.BadRequestException;*   
  - **401 Unauthorized**: *javax.ws.rs.NotAuthorizedException;*
  - **403 Forbidden**: *javax.ws.rs.ForbiddenException;*
  - **404 Not Found**: *co.cask.cdap.client.exception.NotFoundException/javax.ws.rs.NotFoundException;*
  - **405 Method Not Allowed**: *javax.ws.rs.NotAcceptableException;*
  - **409 Conflict**: *javax.ws.rs.NotAcceptableException;*
  - **500 Internal Server Error**: *javax.ws.rs.ServerErrorException;*
  - **501 Not Implemented**: *javax.ws.rs.NotSupportedException*.