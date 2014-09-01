# CDAP Ingest Ruby gem

Stream Client Ruby API for managing streams via external Ruby applications.

## Supported Actions

- create a Stream with a specified <stream-id>;
- update TTL for an existing Stream with a specified <stream-id>;
- retrieve the current Stream TTL for a specified <stream-id>;
- truncate an existing Stream (the deletion of all events that were written to the Stream);
- write an event to an existing Stream specified by <stream-id>;
- send a File to an existing Stream specififed by <stream-id>.

## Usage

 To use the Stream Client Ruby API:
- run `sh build_install` or `cmd build_install`
- include this in your Gemfile:

```
    gem 'cdap-stream-client'
```

## Example

Create a StreamClient instance, specifying the fields 'host' and 'port' of the gateway server. 
Optional configurations that can be set (and their default values):

  - SSL: False (use HTTP protocol)
  - apiKey:  '' (Need to specify to authenticate client requests using SSL)

 ```
   config = {
    gateway: 'localhost'
    post: 10000
    api_version: 'v2'
    ssl: false
   }

   streamClient = CDAP::StreamClient.new(config)
 ```


 Create a new Stream with the *stream-id* "newStreamName":

 ```
   streamClient.create("newStreamName");
 ```

 Notes:

  - The <stream-id> should only contain ASCII letters, digits and hyphens.
  - If the Stream already exists, no error is returned, and the existing Stream remains in place.


 Update TTL for the Stream "streamName"; TTL is a long value:

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

 To write new events to the Stream, you can use any of these two methods of the ```StreamWriter``` object:

 ```
   def write(body, charset='utf-8', headers={})
   def send(file, mimetype='text/plain')
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

For error handling, create a handler for each case and set it using the then method.

Example:

```
success = ->(response):
    ...
    puts response
    ...

error = ->(error):
    ...
    puts error.response
    puts error.message
    ...

streamPromise.then(success, error)
```

It's not required to define an error handler. You can use block for success handler.

## Additional Notes

 All methods from the ```StreamClient``` and ```StreamWriter``` throw exceptions using response code analysis from the 
 gateway server. These exceptions help determine if the request was processed successfully or not.

 In the case of a **200 OK** response, no exception will be thrown; other cases will throw the CDAP::Rest::ResponseError exception.

```response``` method of the exception class returns response.
