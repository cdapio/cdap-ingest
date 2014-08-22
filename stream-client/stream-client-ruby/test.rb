#!/usr/bin/env ruby

require './lib/stream_client'

puts 'create stream'
StreamClient.create 'dima'

puts 'set ttl'
StreamClient.set_ttl 'dima', 256

puts 'get ttl'
ttl = StreamClient.get_ttl 'dima'
puts "ttl: #{ttl}"

puts 'truncate'
StreamClient.truncate 'dima'
