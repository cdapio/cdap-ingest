file-tailer
==================

File Tailer is a daemon process that can runs on any machine and perform tailing of set of local files. 
As soon as a new record is being appended to the end of a file that the daemon is monitoring, it will send it to a Stream via REST API.

## Features

 - distributed as debian and rpm packages;
 - loads properties from configuration file;
 - supports rotation of log files;
 - persists state and is able to resume from first unsent record;
 - dumps statistics info;

## Usage

 In order to install File Tailer one should execute following command:
 - on debian/ubuntu systems:
    $> sudo apt-get install file-tailer.deb
 - on RHEL/CentOS systems:
    $> sudo rpm -ivh --force file-tailer.rpm

 Once installed, 
 To configure the daemon, edit following file:
    /etc/file-tailer/conf/
    
    
 Once configured, 
 To start the daemon, execute following command:
    $> sudo service file-tailer start
 
 
 File Tailer stores logs in /var/log/file-tailer folder.
 
  
## Example Configuration
   
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
   ListenableFuture<Void> send(File file, MediaType type);
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
