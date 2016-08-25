# Copyright © 2014-2016 Cask Data, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License. You may obtain a copy of
# the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.

# encoding: utf-8

require 'thread/pool'
require 'thread/future'

module CDAP
  ###
  # Provides ability for ingesting events to a stream in different ways.
  class StreamWriter
    MAX_POOL_SIZE = 5

    attr_reader :stream, :rest, :pool, :parent_thread

    def initialize(stream, rest = nil, pool_size = nil)
      @stream = stream
      @rest = rest || RestClient.new
      pool_size ||= MAX_POOL_SIZE
      @pool = Thread.pool pool_size
      @parent_thread = Thread.current
    end

    ###
    # Ingest a stream event with a set of headers and a string as body.
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
      pool.future do
        begin
          body = rest.to_s(body).encode(charset).force_encoding(Encoding::BINARY)
          rest.request 'post', stream, body: body, headers: headers
        rescue RestClient::ResponseError => e
          result.reject e
        rescue Exception => e
          parent_thread.raise e
        end
      end
    end

    def close
      pool.shutdown
    end
  end
end
