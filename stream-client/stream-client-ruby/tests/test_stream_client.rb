require './lib/cdap-stream-client'
require 'test/unit'

class TestStreamClient < Test::Unit::TestCase

  def setup
    @stream = 'test_stream'
    @client = CDAP::StreamClient.new gateway: 'localhost'
  end

  def test_create_stream
    assert_equal 200, @client.create(@stream).code
  end

  def test_set_ttl
    assert_equal 200, @client.set_ttl(@stream, 256).code
  end

  def test_set_ttl_bad
    assert_raise(HTTParty::ResponseError) {
      @client.set_ttl(@stream, -1)
    }
  end

  def test_get_ttl
    ttl = 512
    @client.set_ttl(@stream, ttl)
    assert_equal ttl, @client.get_ttl(@stream)
  end

  def test_truncate
    assert_equal 200, @client.truncate(@stream).code
  end

end
