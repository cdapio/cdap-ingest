=============================
CDAP Stream Client for Python
=============================

The Stream Client Python API is for managing Streams from Python applications.


Supported Actions
=================

- Create a Stream
- Update TTL (time-to-live) for an existing Stream
- Retrieve the current Stream TTL
- Truncate an existing Stream (the deletion of all events that were written to the Stream)
- Write an event to an existing Stream


Installation
============

To install the CDAP Stream Client, run::

  $ pip install cdap-stream-client

To install the development version, clone the repository::

  $ git clone https://github.com/caskdata/cdap-ingest.git
  $ cd cdap-ingest/cdap-stream-clients/python/
  $ python setup.py install

Supported Python versions: 2.6, 2.7

Usage
=====

To use the Stream Client Python API, include these imports in your Python script::

  from cdap_stream_client import Config
  from cdap_stream_client import StreamClient


Examples
========

Creating a StreamClient
-----------------------
Create a StreamClient instance with default parameters::

  stream_client = StreamClient()

Optional configurations that can be set (and their default values):

- ``host``: ``localhost``
- ``port``: ``10000``
- ``namespace``: ``default``
- ``ssl``: ``False`` (set to ``True`` to use HTTPS protocol)
- ``ssl_cert_check``: ``True`` (set to ``False`` to suspend certificate checks; this allows self-signed
  certificates when SSL is ``True``)
- ``authClient``: ``null`` (`CDAP Authentication Client
  <https://github.com/caskdata/cdap-clients/tree/develop/cdap-authentication-clients/python>`__
  to interact with a secure CDAP instance)

Example::

  config = Config()
  config.host = 'localhost'
  config.port = 10000
  config.namespace = example
  config.ssl = True
  config.set_auth_client(authentication_client)

  stream_client = StreamClient(config)


Creating a Stream
-----------------
Create a new Stream with the ``stream-id`` *newStreamName*::

    stream_client.create("newStreamName")

**Notes:**

- The ``stream-id`` should only contain ASCII letters, digits and hyphens.
- If the Stream already exists, no error is returned, and the existing Stream remains in place.

Creating a StreamWriter
-----------------------
Create a ``StreamWriter`` instance for writing events to the Stream *streamName*:

    stream_writer = stream_client.create_writer("streamName")

Writing Stream Events
---------------------
To write new events to the Stream, use the ``write`` method of the ``StreamWriter`` class::

  def write(self, message, charset=None, headers=None)

Example::

  stream_promise = stream_writer.write("New stream event")

Truncating a Stream
-------------------
To delete all events that were written to the Stream *streamName*, use::

    stream_client.truncate("streamName")

Updating Stream Time-to-Live (TTL)
----------------------------------
Update TTL for the Stream *streamName*::

    stream_client.set_ttl("streamName", newTTL)

Getting Stream Time-to-Live (TTL)
---------------------------------
Get the current TTL value for the Stream *streamName*::

    ttl = stream_client.get_ttl("streamName")

StreamPromise
-------------
``StreamPromise``'s goal is to implement deferred code execution.

For error handling, create a handler for each case and set it using the ``onResponse``
method. The error handling callback function is optional.

Example::

  def on_ok_response(response):
      ...
      parse response
      ...

  def on_error_response(response):
      ...
      parse response
      ...

  stream_promise = stream_writer.write("New stream event")
  stream_promise.on_response(on_ok_response, on_error_response)


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
