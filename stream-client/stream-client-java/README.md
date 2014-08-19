stream-client-java
==================

Stream Client Java API for managing streams via external custom java applications.

## supported actions

 - create Stream with specified <stream-id>;
 - update TTL for Stream by specified <stream-id>;
 - get current Stream TTL by specified <stream-id>;
 - truncate Stream by specified <stream-id> (this means the deletion of all events that were written to the Stream);
 - write event to the Stream by specified <stream-id>;
 - send File to the Stream by specified <stream-id>.



## usage

 For the start to use Stream Client Java API, you need to include maven dependency to your project pom file:
 
 <dependency>
  <groupId>co.cask.cdap</groupId>
  <artifactId>stream-client-java</artifactId>
  <version>1.0-SNAPSHOT</version>
 </dependency>
 
## example
   
 Create StreamClient instance with mandatory fields 'host' and 'port' of Getaway server. 
 Optional configurations could be set as default:
  
  - ssl: false (use HTTP protocol) 
  - writer pool size: '10' (max thread pool size for write events to the Stream)
  - version : 'v2' (Getaway server version, used as a part of base uri [http://localhost:10000/v2/...])  
  - authToken: null (Need to be specify for authenticate client requests). 
  - apiKey: null (Need to be specify for authenticate client requests using SSL)
 
 ```
   StreamClient streamClient = new RestStreamClient.Builder("localhost", 10000).build();
 ```
      
 or also could be specified as the builder parameters:
 
 ```
   StreamClient streamClient = new RestStreamClient.Builder("localhost", 10000)
         .apiKey("apiKey")
         .authToken("token")
         .ssl(false)
         .version("v2")
         .writerPoolSize(10)
         .build();
 ```
 
 Create new Stream with new *stream-id*
 
 ```
   streamClient.create("newStreamName");
 ```
      
 Notes:
 
  - The <stream-id> should only contain ASCII letters, digits and hyphens.
  - If the Stream already exists, no error is returned, and the existing Stream remains in place.
     
 
 Update TTL for Stream by *stream-id*, TTL is long value
 
 ```
   streamClient.setTTL("streamName", newTTL);
 ```
 
 Get current TTL value by *stream-id*
 
 ```  
   long ttl = streamClient.getTTL("streamName");  
 ```
 
 Create StreamWriter instance for writing events to the Stream
 
 ```
   StreamWriter streamWriter = streamClient.createWriter(streamName);
 ```
     
 For write new event to the stream, you could use on of existing 5 methods from the StreamWriter interface:
 
 ``` 
   ListenableFuture<Void> write(String str, Charset charset);
   ListenableFuture<Void> write(String str, Charset charset, Map<String, String> headers);
   ListenableFuture<Void> write(ByteBuffer buffer);
   ListenableFuture<Void> write(ByteBuffer buffer, Map<String, String> headers);
   ListenableFuture<Void> send(File file, MediaType type);
 ```
 
 for example:
 
 ```
   streamWriter.write("New log event", Charsets.UTF_8).get();
 ```
   
 For truncate Stream by *stream-id*, use
 
 ```
   streamClient.truncate("streamName");
 ```
   
 When you already does not need to use created clients, release all unused resources by calling methods
 
 ```  
   streamWriter.close();
   streamClient.close();  
 ```

## additional notes
 
 All methods from the StreamClient and StreamWriter throw Exceptions using response code analysis from 
 the Getaway server. This exceptions help to recognizes is the request processed successfully or not.
  
 So, in the case of **200 OK** no exception will be thrown;
 In other cases will be thrown following exceptions:
  
  - **400 Bad Request** 
    trigger *javax.ws.rs.BadRequestException;*   
  - **401 Unauthorized** 
    trigger *javax.ws.rs.NotAuthorizedException;*
  - **403 Forbidden** 
    trigger *javax.ws.rs.ForbiddenException;*
  - **404 Not Found** 
    trigger *co.cask.cdap.client.exception.NotFoundException/javax.ws.rs.NotFoundException;*
  - **405 Method Not Allowed** 
    trigger *javax.ws.rs.NotAcceptableException;*
  - **409 Conflict** 
    trigger *javax.ws.rs.NotAcceptableException;*
  - **500 Internal Server Error** 
    trigger *javax.ws.rs.ServerErrorException;*
  - **501 Not Implemented** 
    trigger *javax.ws.rs.NotSupportedException*.
