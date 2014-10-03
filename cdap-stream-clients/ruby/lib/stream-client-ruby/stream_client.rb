#  Copyright 2014 Cask Data, Inc.
#
#  Licensed under the Apache License, Version 2.0 (the "License"); you may not
#  use this file except in compliance with the License. You may obtain a copy of
#  the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
#  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
#  License for the specific language governing permissions and limitations under
#  the License.

module CDAPIngest
###
  # The client interface to interact with services provided by stream endpoint.
  class StreamClient

    attr_reader :rest
    attr_reader :auth_client

    def initialize
      @rest = Rest.new
    end

    # Set authentication client
    def set_auth_client auth_client
     Rest.set_auth_client auth_client
    end

    ###
      # Creates a stream with the given name.
    def create(stream)
      rest.request 'put', stream
    end

    ###
      # Set the Time-To-Live (TTL) property of the given stream.
      #
      # @param stream Name of the stream
      # @param ttl TTL in seconds
      # @throws NotFoundException If the stream does not exists
    def set_ttl(stream, ttl)
      rest.request 'put', "#{stream}/config", body: { ttl: ttl }
    end

    ###
      # Retrieves the Time-To-Live (TTL) property of the given stream.
      #
      # @param stream Name of the stream
      # @return Current TTL of the stream in seconds
      # @throws NotFoundException If the stream does not exists
    def get_ttl(stream)
      response = rest.request 'get', "#{stream}/info"
      humanize response['ttl']
    end

    ###
      # Truncates all existing events in the give stream.
      #
      # @param stream Name of the stream
      # @throws NotFoundException If the stream does not exists
    def truncate(stream)
      rest.request 'post', "#{stream}/truncate"
    end

    ###
      # Creates a {@link StreamWriter} instance for writing events to the given stream.
      #
      # @param stream Name of the stream
      # @return An instance of {@link StreamWriter} that is ready for writing events to the stream
      # @throws NotFoundException If the stream does not exists
    def create_writer(stream, pool_size = nil)
      StreamWriter.new stream, rest, pool_size
    end

    private

      def humanize ttl
        ttl / 1000 if ttl
      end
  end
end
