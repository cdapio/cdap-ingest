# CDAP Stream Client Python library

Stream Client JavaScript API for managing Streams via external JavaScript applications.

## Supported Actions

- create a Stream with a specified *stream-id*;
- update TTL for an existing Stream with a specified *stream-id*;
- retrieve the current Stream TTL for a specified *stream-id*;
- truncate an existing Stream (the deletion of all events that were written to the Stream);
- write an event to an existing Stream specified by *stream-id*;


## Usage

 To use the Stream Client JavaScript API, include these imports in your JavaScript script:

### Browser
```
    <script src="cdap-stream-client.min.js"></script>
```
### NodeJS
```
    var StreamClient = require('cdap-stream-client');
```

## Example

Create a ```StreamClient``` instance, specifying the fields 'host' and 'port' of the gateway server. 
Optional configurations that can be set (and their default values):

  - ssl: false (use HTTP protocol)
  - authManager: null (reference to AutheticationManager)

 ```
   config = {
       host: 'localhost',
       port: 10000
   }

   var streamClient = new StreamClient(config)
 ```

 Create a new Stream with the *stream-id* "newStreamName":

 ```
   streamClient.create("newStreamName");
 ```

 Notes:

  - The *stream-id* can only contain ASCII letters, digits and hyphens.
  - If the Stream already exists, no error is returned, and the existing Stream remains in place.


 Update TTL for the Stream "streamName"; ```newTTL``` is a long value:

 ```
   streamClient.setTTL("streamName", newTTL);
 ```

 Get the current TTL value for the Stream "streamName":

 ```
   var ttl = streamClient.getTTL("streamName");
 ```

 Create a ```StreamWriter``` instance for writing events to the Stream "streamName":

 ```
   var streamWriter = streamClient.createWriter("streamName");
 ```

 To write new events to the Stream, you can use either of these these methods of the ```StreamWriter``` class:

 ```
   write({
         message,
         [headers = {}]
         })
 ```

 Example:

 ```
   var streamPromise = streamWriter.write("New log event");
 ```

 To truncate the Stream *streamName*, use:

 ```
   streamClient.truncate("streamName");
 ```

 ### StreamPromise
 StreamPromise's goal is to implement deferred code execution.

For error handling, create a handler for each case and set it using the ```then``` method.

Example:

```
var onOkHandlerr = function onOkHandler(httpResponse) {
    ...
    parse response
    ...
}

var onErrorHandler = function onErrorHandler(httpResponse) {
    ...
    parse response
    ...
}

streamPromise.then(onOkResponse, onErrorResponse)
```

## Additional Notes

 All methods from the ```StreamClient``` and ```StreamWriter``` throw exceptions using response code analysis from the 
 gateway server. These exceptions help determine if the request was processed successfully or not.

 In the case of a **200 OK** response, no exception will be thrown; other cases will throw the exception.

```status``` method of the exception class returns HTTP error code.
```message``` method of the exception class returns text representation of an error.