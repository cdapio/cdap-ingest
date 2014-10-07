# CDAP Stream Client
The Stream Client Java API is for managing Streams from Java applications.

## Supported Actions
 - Create a Stream
 - Update TTL (time-to-live) for an exiting Stream
 - Retrieve the current Stream TTL
 - Truncate an existing Stream (the deletion of all events that were written to the Stream)
 - Write an event to an existing Stream

## Build
To build the Stream Client Java API jar, use:
```mvn clean package```

## Usage
To use the Stream Client Java API, include this Maven dependency in your project's ```pom.xml``` file:
```
 <dependency>
  <groupId>co.cask.cdap</groupId>
  <artifactId>stream-client-java</artifactId>
  <version>1.0.0</version>
 </dependency>
```
 
## Example
#### Create StreamClient
Create a StreamClient instance, specifying the fields 'host' and 'port' of the CDAP instance.
```
   StreamClient streamClient = new RestStreamClient.Builder("localhost", 10000).build();
 ```

Optional configuration that can be set (and their default values):
 - ssl: false (set true to use HTTPS protocol)
 - verifySSLCert: true (set false to suspend certificate checks to allow self-signed certificates when SSL is true)
 - authClient: null (Needed to interact with secure CDAP instances)
```
   StreamClient streamClient = new RestStreamClient.Builder("localhost", 10000)
         .ssl(true)
         .authClient(authenticationClient)
         .build();
 ```
 
#### Create Stream
Create a new Stream with the *stream id* "streamName":
```
   streamClient.create("streamName");
 ```
**Notes:**
 - The *stream-id* should only contain ASCII letters, digits and hyphens.
 - If the Stream already exists, no error is returned, and the existing Stream remains in place.
     
#### Create StreamWriter
Create a ```StreamWriter``` instance for writing events to the Stream *streamName*:
```
   StreamWriter streamWriter = streamClient.createWriter("streamName");
 ```

#### Write Stream Events
To write new events to the Stream, you can use any of these five methods in the ```StreamWriter``` interface:
```
   ListenableFuture<Void> write(String str, Charset charset);
   ListenableFuture<Void> write(String str, Charset charset, Map<String, String> headers);
   ListenableFuture<Void> write(ByteBuffer buffer);
   ListenableFuture<Void> write(ByteBuffer buffer, Map<String, String> headers);
```
Example:
```
   streamWriter.write("New log event", Charsets.UTF_8).get();
```

#### Truncate Stream
To truncate the Stream *streamName*, use:
```
   streamClient.truncate("streamName");
```

#### Update Stream TTL
Update TTL for the Stream *streamName*:
```
   streamClient.setTTL("streamName", newTTL);
 ```

#### Get Strem TTL
Get the current TTL value for the Stream *streamName*:
```
   long ttl = streamClient.getTTL("streamName");
 ```

#### Close Clients
When you are finished, release all resources by calling these two methods:
```
   streamWriter.close();
   streamClient.close();  
```

## Additional Notes
All methods from the ```StreamClient``` and ```StreamWriter``` throw exceptions using response code analysis from the
gateway server. These exceptions help determine if the request was processed successfully or not.
 
In the case of a **200 OK** response, no exception will be thrown; other cases will throw these exceptions:
 - **400 Bad Request**: *javax.ws.rs.BadRequestException;*
 - **401 Unauthorized**: *javax.ws.rs.NotAuthorizedException;*
 - **403 Forbidden**: *javax.ws.rs.ForbiddenException;*
 - **404 Not Found**: *co.cask.cdap.client.exception.NotFoundException/javax.ws.rs.NotFoundException;*
 - **405 Method Not Allowed**: *javax.ws.rs.NotAcceptableException;*
 - **409 Conflict**: *javax.ws.rs.NotAcceptableException;*
 - **500 Internal Server Error**: *javax.ws.rs.ServerErrorException;*
 - **501 Not Implemented**: *javax.ws.rs.NotSupportedException*.
