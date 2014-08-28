#!/usr/bin/env ruby

require './lib/stream_client'

stream = 'test'
client = StreamClient.new

puts 'create stream'
client.create stream

puts 'set ttl'
client.set_ttl stream, 256

puts 'get ttl'
ttl = client.get_ttl stream
puts "ttl: #{ttl}"

puts 'truncate'
client.truncate stream

writer = client.create_writer stream, 2

_10mb = '1' * (10 * 1024 * 1024)

20.times {
  writer.write(_10mb).then(
    -> (response) {
      puts "success: #{response.code}"
    },
    -> (error) {
      puts "error: #{error.response.code} -> #{error.message}"
    }
  )
}

puts 'after all requests in code'

writer.close
