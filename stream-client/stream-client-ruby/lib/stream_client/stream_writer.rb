require './lib/stream_client/rest'

module StreamClient

  ###
    # Provides ability for ingesting events to a stream in different ways.
  class StreamWriter

    def initialize(stream)

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
    def write(body, charset, headers = [])

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
    def send(file, type)

    end

  end

end
