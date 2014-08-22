#!/usr/bin/env ruby

require './lib/stream_client'

rest = StreamClient::Rest

response = rest.request 'post', 'streams/dima/consumer-id'

puts response.body if response.is_a?(Net::HTTPSuccess)
