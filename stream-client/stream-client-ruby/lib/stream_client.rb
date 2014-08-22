require './lib/stream_client/rest'
require './lib/stream_client/stream_writer'

###
  # The client interface to interact with services provided by stream endpoint.
module StreamClient

  ###
    # Creates a stream with the given name.
  def self.create(stream)

  end

  ###
    # Set the Time-To-Live (TTL) property of the given stream.
    #
    # @param stream Name of the stream
    # @param ttl TTL in seconds
    # @throws NotFoundException If the stream does not exists
  def self.set_ttl(stream, ttl)

  end

  ###
    # Retrieves the Time-To-Live (TTL) property of the given stream.
    #
    # @param stream Name of the stream
    # @return Current TTL of the stream in seconds
    # @throws NotFoundException If the stream does not exists
  def self.get_ttl(stream)

  end

  ###
    # Truncates all existing events in the give stream.
    #
    # @param stream Name of the stream
    # @throws NotFoundException If the stream does not exists
  def self.truncate(stream)

  end

  ###
    # Creates a {@link StreamWriter} instance for writing events to the given stream.
    #
    # @param stream Name of the stream
    # @return An instance of {@link StreamWriter} that is ready for writing events to the stream
    # @throws NotFoundException If the stream does not exists
  def self.create_writer(stream)

  end

end
