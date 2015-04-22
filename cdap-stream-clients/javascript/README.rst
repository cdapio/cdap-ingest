=================================
CDAP Stream Client for JavaScript
=================================

The Stream Client JavaScript API is for managing Streams via external JavaScript applications.

Supported Actions
=================

- Create a Stream with a specified *stream-id*
- Update TTL for an existing Stream with a specified *stream-id*
- Retrieve the current Stream TTL for a specified *stream-id*
- Truncate an existing Stream (the deletion of all events that were written to the Stream)
- Write an event to an existing Stream specified by *stream-id*


Usage
=====

To use the Stream Client JavaScript API, include these imports in your JavaScript script:

Browser
-------

::

  <script src="cdap-stream-client.min.js"></script>

NodeJS
------

::

  var StreamClient = require('cdap-stream-client');


Examples
--------

**Note:** All methods return ``Promise`` objects. Use the ``then``, ``catch``, and
``notify`` methods of ``Promise`` to interact with the returned data.

Create a ``StreamClient`` instance, specifying the fields 'host' and 'port' of the gateway
server. Optional configurations that can be set (and their default values):

- ``ssl: false`` (use HTTP protocol)
- ``authManager: null`` (a reference to the AutheticationManager)

::

   config = {
       host: 'localhost',
       port: 10000
   }

   var streamClient = new StreamClient(config)


Create a new Stream with the *stream-id* "newStreamName"::

  streamClient.create("newStreamName");

**Notes:**
- The *stream-id* can only contain ASCII letters, digits and hyphens.
- If the Stream already exists, no error is returned, and the existing Stream remains in place.

Update TTL for the Stream "streamName"; ``newTTL`` is a long value::

   var ttlPromise = streamClient.setTTL("streamName", newTTL);
   ttlPromise.then(function onOk(ttlValue) {
     //TTL has been set up. 
   }, function onError(statusCode) {
     //actions on error
   });


Get the current TTL value for the Stream "streamName"::

   var ttlPromise = streamClient.getTTL("streamName");
   ttlPromise.then(function onOk(ttlValue) {
     //use TTL value
   }, function onError(statusCode) {
     //actions on error
   });


Create a ``StreamWriter`` instance for writing events to the Stream "streamName"::

   var streamWriterPromise = streamClient.createWriter("streamName");
   streamWriterPromise.then(function(writer){
       console.log('new writer instance has been created:', writer);
   })

To write new events to the Stream, you can use either of these these methods of the
``StreamWriter`` class::

   write({
         message,
         [headers = {}]
         })

Example::

   var streamWriter = streamClient.createWriter("streamName");
   streamWriter.then(function(writer){
       writer.write("Many messages sent to stream.");
       writer.write("Another message sent to stream.");
       writer.write("Multiple messages sent to stream.");
   })

To truncate the Stream *streamName*, use::

   var truncatePromise = streamClient.truncate("streamName");
   truncatePromise.then(function () {
       console.log('truncated successfully');
   });


StreamPromise
-------------
 
StreamPromise's goal is to implement deferred code execution.

For error handling, create a handler for each case and set it using the ``then`` method.

Example::

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


Additional Notes
================

All methods from the ``StreamClient`` and ``StreamWriter`` return errors using
``Promise``\ s. Please use ``Promise``\ 's ``catch`` method to interact with errors. These
errors help determine if the request was processed successfully or not.

In the case of a **200 OK** response, no error will be thrown; in other cases, HTTP status
code will be returned.

License and Trademarks
----------------------
Copyright © 2014-2015 Cask Data, Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
in compliance with the License. You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the
License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
either express or implied. See the License for the specific language governing permissions
and limitations under the License.

Cask is a trademark of Cask Data, Inc. All rights reserved.

Apache, Apache HBase, and HBase are trademarks of The Apache Software Foundation. Used with
permission. No endorsement by The Apache Software Foundation is implied by the use of these marks.
