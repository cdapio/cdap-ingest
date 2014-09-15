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

require 'thread/pool'
require 'promise'

module CDAPIngest
  ###
    # Provides ability for ingesting events to a stream in different ways.
  class StreamWriter

    MAX_POOL_SIZE = 5

    attr_reader :stream, :rest, :pool, :parent_thread

    def initialize stream, rest = nil, pool_size = nil
      @stream = stream
      @rest = rest || Rest.new
      pool_size ||= MAX_POOL_SIZE
      @pool = Thread.pool pool_size
      @parent_thread = Thread.current
    end

    ###
      #Ingest a stream event with a set of headers and a string as body.
      #
      # @param body The string body or
      #               Contains the content for the stream event body. All remaining bytes of the {@link ByteBuffer}
      #               should be used as the body. After this method returns and on the completion of the resulting
      #               {@link ListenableFuture}, the buffer content as well as properties should be unchanged.
      # @param charset Charset to encode the string as stream event payload
      # @param headers Set of headers for the stream event
      # @return A future that will be completed when the ingestion is completed. The future will fail if the ingestion
      #         failed. Cancelling the returning future has no effect.
    def write(body, charset = 'utf-8', headers = {})
      promise_request {
        body = rest.to_s(body).encode(charset).force_encoding(Encoding::BINARY)
        rest.request 'post', stream, body: body, headers: headers
      }
    end

    ###
      # Sends the content of a {@link File} as multiple stream events.
      #
      # @param file The file to send
      # @param type Contains information about the file type.
      # @return A future that will be completed when the ingestion is completed. The future will fail if the ingestion
      #         failed. Cancelling the returning future has no effect.
      #
      # NOTE: There will be a new HTTP API in 2.5 to support extracting events from the file based on the content type.
      #       Until that is available, breaking down the file content into multiple events need to happen in the client
      #       side.
    def send(file, type = 'text/plain')
      # rest.send_file file, type
      promise_request {
        file = File.open(file, 'rb') { |io| io.read }
        rest.request 'post', stream, body: file, headers: { 'Content-type' => type }
      }
    end

    def close
      pool.shutdown
    end

  private

    def promise_request
      promise = Promise.new
      pool.process {
        begin
          promise.fulfill yield
        rescue Rest::ResponseError => e
          promise.reject e
        rescue Exception => e
          parent_thread.raise e
        end
      }
      promise
    end

  end

end
