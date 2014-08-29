Gem::Specification.new do |g|
  g.name = 'cdap-stream-client'
  g.version = '0.1'
  g.summary = 'CDAP Rest Stream Client.'
  g.description = 'This is a stream client and stream writer for cdap framework.'
  g.email = 'dskavulyak@continuuity.com'
  g.homepage = 'http://continuuity.com/'
  g.files = [
    'Gemfile',
    'lib/cdap-stream-client/rest.rb',
    'lib/cdap-stream-client/stream_writer.rb',
    'lib/cdap-stream-client/stream_client.rb',
    'lib/cdap-stream-client.rb',
    'tests/test_stream_writer.rb',
    'tests/test_stream_client.rb'
  ]
  g.author = 'Continuuity: dima.rgb'
  g.licenses = ''
end
