require 'cdap-stream-client/stream_writer'

module CDAP

  ###
  # The client interface to interact with services provided by stream endpoint.
  class StreamClient

    attr_reader :rest

    def initialize config = {}
      @rest = Rest.new config
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
      response['ttl']
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

  end

end
