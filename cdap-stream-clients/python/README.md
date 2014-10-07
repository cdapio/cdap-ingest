# CDAP Stream Client
The Stream Client Python API is for managing Streams from Python applications.

## Supported Actions
 - Create a Stream
 - Update TTL (time-to-live) for an existing Stream
 - Retrieve the current Stream TTL
 - Truncate an existing Stream (the deletion of all events that were written to the Stream)
 - Write an event to an existing Stream


## Installation
 To install CDAP Stream Client, run:
```
    $ python setup.py install
```

## Usage

 To use the Stream Client Python API, include these imports in your Python script:

```
    from cdap_stream_client import Config
    from cdap_stream_client import StreamClient
```

## Example

Create a ```StreamClient``` instance, specifying the fields 'host' and 'port' of the gateway server.
```
   streamClient = StreamClient()
```

Optional configurations that can be set (and their default values):
- ssl: False (use HTTP protocol)
- ssl_cert_check: true (set false to suspend certificate checks to allow self-signed certificates when SSL is true)
- authClient: null ([Authenticaton Client](https://github.com/caskdata/cdap-clients/tree/develop/cdap-authentication-clients/java)
 to interact with a secure CDAP instance)
 ```
   config = Config()
   config.host = 'localhost'
   config.port = 10000
   config.ssl = True
   config.set_auth_client(authentication_client)

   streamClient = StreamClient(config)
 ```

 Create a new Stream with the *stream-id* "newStreamName":

 ```
   streamClient.create("newStreamName");
 ```

**Notes:**
 - The *stream-id* should only contain ASCII letters, digits and hyphens.
 - If the Stream already exists, no error is returned, and the existing Stream remains in place.


 Update TTL for the Stream "streamName"; ```newTTL``` is a long value:

 ```
   stream_client.set_ttl("streamName", newTTL);
 ```

 Get the current TTL value for the Stream "streamName":

 ```
   ttl = stream_client.get_ttl("streamName");
 ```

 Create a ```StreamWriter``` instance for writing events to the Stream "streamName":

 ```
   stream_writer = stream_client.create_writer("streamName");
 ```

 To write new events to the Stream, you can use either of these these methods of the ```StreamWriter``` class:

 ```
   def write(self, message, charset=None, headers=None)
 ```

 Example:

 ```
   stream_promise = stream_writer.write("New stream event");
 ```

 To truncate the Stream *streamName*, use:

 ```
   stream_client.truncate("streamName");
 ```

 ### StreamPromise
 StreamPromise's goal is to implement deferred code execution.

For error handling, create a handler for each case and set it using the ```onResponse``` method. The error handling callback function is optional.

Example:

```
def on_ok_response(response):
    ...
    parse response
    ...

def on_error_response(response):
    ...
    parse response
    ...

stream_promise.on_response(on_ok_response, on_error_response)
```
