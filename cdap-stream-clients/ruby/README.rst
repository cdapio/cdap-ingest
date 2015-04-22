===========================
CDAP Stream Client for Ruby
===========================

The Stream Client Ruby API is a tool for managing Streams via external custom Ruby applications.

Supported Actions
=================

- Create a Stream with a specified <stream-id>
- Update TTL for an exiting Stream with a specified <stream-id>
- Retrieve the current Stream TTL for a specified <stream-id>
- Truncate an existing Stream (the deletion of all events that were written to the Stream)
- Write an event to an existing Stream

Build
=====

To build a gem, run::

  gem build stream-client-ruby.gemspec


Usage
=====

To use the Stream Client Ruby API, just add to your application Gemfile::

  gem 'stream-client-ruby'


If you use gem outside Rails, you should require gem files in your application files::

  require 'stream-client-ruby'


Example
=======

You can configure ``StreamClient`` settings in your config files. For example::

  # config/stream.yml
  gateway: 'localhost'
  port: 10000
  api_version: 'v3'
  namespace: 'example'
  api_key:
  ssl: false

  # initializers/stream.rb
  require "yaml"

  config = YAML.load_file("config/stream.yml")

  CDAPIngest::Rest.gateway     = config['gateway']
  CDAPIngest::Rest.port        = config['port']
  CDAPIngest::Rest.api_version = config['api_version']
  CDAPIngest::Rest.namespace   = config['namespace']
  CDAPIngest::Rest.ssl         = config['ssl']


Create a StreamClient instance and use it as any Ruby object::

  client = CDAPIngest::StreamClient.new


If authentication is required, set authentication client::

  client.set_auth_client auth_client


Create a new Stream with the *stream-id* "new_stream_name"::

  client.create "new_stream_name"


Notes:

- The <stream-id> must only contain ASCII letters, digits and hyphens.
- If the Stream already exists, no error is returned, and the existing Stream remains in place.


Update TTL for the Stream *stream_name*; TTL is a integer value such as 256::

  client.set_ttl stream_name, 256


Get the current TTL value for the Stream *stream_name*::

  ttl = client.get_ttl "stream_name"


Create a ``StreamWriter`` instance for writing events to the Stream *stream_name* in 3
threads asynchronously::

  writer = client.create_writer "stream_name", 3


To write new events to the Stream, you can use code::

  test_data = "string to send in stream 10 times"

  10.times {
    writer.write(test_data).then(
      ->(response) {
        puts "success: #{response.code}"
      },
      ->(error) {
        puts "error: #{error.response.code} -> #{error.message}"
      }
    )
  }



To truncate the Stream *stream_name*, use::

  client.truncate "stream_name"


When you are finished, release all resources by calling this method::

  writer.close


Additional Notes
================

All methods from the ``StreamClient`` and ``StreamWriter`` throw exceptions using response
code analysis from the gateway server. These exceptions help determine if the request was
processed successfully or not.

In the case of a ``200 OK`` response, no exception will be thrown; other cases will throw
these exceptions:

- ``400``: The request had a combination of parameters that is not recognized
- ``401``: The request did not contain an authentication token
- ``403``: The request was authenticated but the client does not have permission
- ``404``: The request did not address any of the known URIs
- ``405``: A request was received with a method not supported for the URI
- ``409``: A request could not be completed due to a conflict with the current resource state
- ``500``: An internal error occurred while processing the request
- ``501``: A request contained a query that is not supported by this API


Testing
=======

To launch unit tests only, execute::

  rspec --tag ~it


To launch integration tests against a Standalone CDAP instance, execute::

  rspec --tag type:it-local


To launch integration tests against a Standalone CDAP instance with authentication enabled,
execute::

  rspec --tag type:it-local-auth


To launch integration tests against a Standalone CDAP instance with authentication enabled
and SSL turned on, execute::

  rspec --tag type:it-local-auth-ssl


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
