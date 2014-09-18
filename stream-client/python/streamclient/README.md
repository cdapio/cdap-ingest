# CDAP Stream Client Python library

Stream Client Python API for managing Streams via external Python applications.

## Supported Actions

- create a Stream with a specified *stream-id*;
- update TTL for an existing Stream with a specified *stream-id*;
- retrieve the current Stream TTL for a specified *stream-id*;
- truncate an existing Stream (the deletion of all events that were written to the Stream);
- write an event to an existing Stream specified by *stream-id*;
- send a File to an existing Stream specififed by *stream-id*.

## Usage

 To use the Stream Client Python API, include these imports in your Python script:

```
    from config import Config
    from streamclient import StreamClient
```

## Example

Create a ```StreamClient``` instance, specifying the fields 'host' and 'port' of the gateway server. 
Optional configurations that can be set (and their default values):

  - ssl: False (use HTTP protocol)

 ```
   config = Config()
   config.host = 'localhost'
   config.port = 10000
   config.ssl = False

   streamClient = StreamClient(config)
 ```

 or using the ```read_from_file``` method of the ```Config``` object:

 ```
   config = Config.read_from_file('/path/to/config.json')

   streamClient = StreamClient(config)
 ```

Config file structure in JSON format:
```
{
    hostname: 'localhost',    - gateway hostname
    port: 10000,              - gateway port
    SSL: false                - if SSL is being used
}
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
   streamClient.set_ttl("streamName", newTTL);
 ```

 Get the current TTL value for the Stream "streamName":

 ```
   ttl = streamClient.get_ttl("streamName");
 ```

 Create a ```StreamWriter``` instance for writing events to the Stream "streamName":

 ```
   streamWriter = streamClient.create_writer("streamName");
 ```

 To write new events to the Stream, you can use either of these these methods of the ```StreamWriter``` class:

 ```
   def write(self, message, charset=None, headers=None)
   def send(self, file, mimetype=None)
 ```

 Example:

 ```
   streamPromise = streamWriter.write("New log event");
 ```

 To truncate the Stream *streamName*, use:

 ```
   streamClient.truncate("streamName");
 ```

 ### StreamPromise
 StreamPromise's goal is to implement deferred code execution.

For error handling, create a handler for each case and set it using the ```onResponse``` method.

Example:

```
def onOkHandler(httpResponse):
    ...
    parse response
    ...

def onErrorHandler(httpResponse):
    ...
    parse response
    ...

streamPromise.onResponse(onOkResponse, onErrorResponse)
```

It's not required to define an error handler. If you don't specify an error handler, the success handler is used for both success and error conditions.

## Additional Notes

 All methods from the ```StreamClient``` and ```StreamWriter``` throw exceptions using response code analysis from the 
 gateway server. These exceptions help determine if the request was processed successfully or not.

 In the case of a **200 OK** response, no exception will be thrown; other cases will throw the NoFoundException exception.

```code``` method of the exception class returns HTTP error code.
```message``` method of the exception class returns text representation of an error.