require '../lib/stream_client'
require 'test/unit'

class TestStreamWriter < Test::Unit::TestCase

  def setup
    client = StreamClient.new gateway: 'localhost'
    @writer = client.create_writer 'test_stream', 3
  end

  def teardown
    @writer.close
  end

  def test_write
    _10mb = '1' * (10 * 1024 * 1024)
    10.times {
      @writer.write(_10mb).then(
          ->(response) {
            assert_equal 200, response.code
          },
          ->(error) {
            assert_not_equal 200, error.response.code
          }
      )
    }
  end

  def test_send
    @writer.send('1MB').then { |response|
      assert_equal 200, response.code
    }
  end

end
