# CDAP ingest Python library

Stream Client Python API for managing streams via external custom Python applications.

## Supported Actions

- create a Stream with a specified <stream-id>;
- update TTL for an existing Stream with a specified <stream-id>;
- retrieve the current Stream TTL for a specified <stream-id>;
- truncate an existing Stream (the deletion of all events that were written to the Stream);
- write an event to an existing Stream specified by <stream-id>;
- send a File to an existing Stream specififed by <stream-id>.

## Usage

 To use the Stream Client Python API, include this into your script:

import config
import streamclient
or
from config import Config
from streamclient import StreamClient

## Example

Create a StreamClient instance, specifying the fields 'host' and 'port' of the gateway server. 
Optional configurations that can be set (and their default values):

  - SSL: False (use HTTP protocol)
  - apiKey:  '' (Need to specify to authenticate client requests using SSL)

 ```
   config = Config()
   config.setHost('localhost')
   config.setPort(10000)
   config.setSSL(False)
   config.setAPIKey('<api-key-hash>')

   streamClient = StreamClient(config)
 ```

 or using the readFromFile method of Config object:

 ```
   config = Config.readFromFile('/path/to/config.ini')

   streamClient = StreamClient(config)
 ```

Config file structure:
```
[ServerConnection]
hostname = kappac             - gateway hostname
port = 10001                  - gateway port
SSL = true                    - should be SSL used
APIKey = something            - API key hash
```

 Create a new Stream with the *stream-id* "newStreamName":

 ```
   streamClient.create("newStreamName");
 ```

 Notes:

  - The <stream-id> should only contain ASCII letters, digits and hyphens.
  - If the Stream already exists, no error is returned, and the existing Stream remains in place.


 Update TTL for the Stream *stream-id*; TTL is a long value:

 ```
   streamClient.setTTL("streamName", newTTL);
 ```

 Get the current TTL value for the Stream *stream-id*:

 ```
   ttl = streamClient.getTTL("streamName");
 ```

 Create a ```StreamWriter``` instance for writing events to the Stream "streamName":

 ```
   streamWriter = streamClient.createWriter("streamName");
 ```

 To write new events to the Stream, you can use any of these two methods of the ```StreamWriter``` class:

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
 StreamPromise's goal is to implement deffered code execution.

 To handle successful an error handling you should create a handler for each case and set this handlers using ```onResponse``` method.

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

It's possible to not define an error handler. In that case successful handler would used in both cases.

## Additional Notes

 All methods from the ```StreamClient``` and ```StreamWriter``` throw exceptions using response code analysis from the 
 gateway server. These exceptions help determine if the request was processed successfully or not.

 In the case of a **200 OK** response, no exception will be thrown; other cases will throw the NoFoundException exception.

```code``` method of the exception class returns HTTP error code.
```message``` method of the exception class returns text representation of an error.