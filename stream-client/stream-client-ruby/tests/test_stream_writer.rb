require './lib/cdap-stream-client'
require 'test/unit'

class TestStreamWriter < Test::Unit::TestCase

  def setup
    client = CDAP::StreamClient.new gateway: 'localhost'
    @writer = client.create_writer 'test_stream', 3
    @_1mb = '1' * (1024 * 1024)
  end

  def teardown
    @writer.close
  end

  def test_write
    7.times {
      @writer.write(@_1mb).then(
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
    file_name = '1MB'
    File.open(file_name, 'w') { |io| io.write @_1mb }
    @writer.send(file_name).then { |response|
      assert_equal 200, response.code
      File.delete file_name
    }
  end

end
