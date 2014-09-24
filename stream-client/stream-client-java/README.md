stream-client-java
==================

The Stream Client Java API is for managing Streams via external custom Java applications.

## Supported Actions

 - create a Stream with a specified <stream-id>;
 - update TTL for an exiting Stream with a specified <stream-id>;
 - retrieve the current Stream TTL for a specified <stream-id>;
 - truncate an existing Stream (the deletion of all events that were written to the Stream);
 - write an event to an existing Stream; 

## Build
 
 To build the Stream Client Java API jar, use:
 
 ```mvn package``` or ``` mvn package -DskipTests```

## Usage

 To use the Stream Client Java API, include this Maven dependency in your project's ```pom.xml``` file:
 
 <dependency>
  <groupId>co.cask.cdap</groupId>
  <artifactId>stream-client-java</artifactId>
  <version>1.0-SNAPSHOT</version>
 </dependency>
 
## Example
   
 Create a StreamClient instance, specifying the fields 'host' and 'port' of the gateway server. 
 Optional configurations that can be set (and their default values):
  
  - ssl: false (use HTTP protocol) 
  - writerPoolSize: '10' (max thread pool size for write events to the Stream)
  - version : 'v2' (Gateway server version, used as a part of the base URI [http(s)://localhost:10000/v2/...]) 
  - authToken: null (Need to specify to authenticate client requests) 
  - apiKey:  null (Need to specify to authenticate client requests using SSL)
 
 ```
   StreamClient streamClient = new RestStreamClient.Builder("localhost", 10000).build();
 ```
      
 or specified using the builder parameters:
 
 ```
   StreamClient streamClient = new RestStreamClient.Builder("localhost", 10000)
         .apiKey("apiKey")
         .authToken("token")
         .ssl(false)
         .version("v2")
         .writerPoolSize(10)
         .build();
 ```
 
 Create a new Stream with the *stream id* "newStreamName":
 
 ```
   streamClient.create("newStreamName");
 ```
      
 Notes:
 
  - The <stream-id> should only contain ASCII letters, digits and hyphens.
  - If the Stream already exists, no error is returned, and the existing Stream remains in place.
     
 
 Update TTL for the Stream *streamName*; TTL is a long value:
 
 ```
   streamClient.setTTL("streamName", newTTL);
 ```
 
 Get the current TTL value for the Stream *streamName*:
 
 ```  
   long ttl = streamClient.getTTL("streamName");  
 ```
 
 Create a ```StreamWriter``` instance for writing events to the Stream *streamName*:
 
 ```
   StreamWriter streamWriter = streamClient.createWriter("streamName");
 ```
     
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
   
 To truncate the Stream *streamName*, use:
 
 ```
   streamClient.truncate("streamName");
 ```
   
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
