require 'spec_helper'
require 'cdap-authentication-client'
require 'stream-client-ruby'
require "yaml"

describe CDAPIngest::StreamClient do

  def load_config file

    config = YAML.load_file(file)
    CDAPIngest::Rest.gateway     = config['gateway']
    CDAPIngest::Rest.port        = config['port']
    CDAPIngest::Rest.api_version = config['api_version']
    CDAPIngest::Rest.ssl         = config['ssl']
  end

  let(:stream_client){ CDAPIngest::StreamClient.new }
  let(:stream) {"LogAnalyticsFlow"}

  def read_from_stream stream_id, start_time, end_time
    if start_time == 0 && end_time == 0
      url = "#{stream_id}/events"
    else
      url = "#{stream_id}/events?start=#{start_time}&end=#{end_time}"
    end
    stream_client.rest.request('get', url).parsed_response
  end

  def test_create_stream stream_id
    stream_client.create stream_id
    data = stream_client.rest.request('get', '').parsed_response
    found = data.find {|e| e['name'] == stream_id}
    expect(found).not_to be_nil
  end

  def test_truncate_stream stream_id
    # write to the stream
    writer = stream_client.create_writer stream_id
    writer.write 'test_value'
    writer.close
    # check that the stream is not empty
    data = read_from_stream stream_id, 0, 0
    expect(data).not_to be_nil
    # truncate the stream
    stream_client.truncate stream_id
    # check that the stream is empty
    data = read_from_stream stream_id, 0, 0
    expect(data).to be_nil
  end

  def test_ttl stream_id
    ttl_test_value = 256
    stream_client.set_ttl stream_id, ttl_test_value
    ttl = stream_client.get_ttl stream_id
    expect(ttl).to eq ttl_test_value
  end

  def test_writer stream_id
    # first truncate the stream
    stream_client.truncate stream_id
    data = read_from_stream stream_id, 0, 0
    expect(data).to be_nil
    # write to the stream
    writer_test_value = 'test_value'
    writer = stream_client.create_writer stream_id
    writer.write writer_test_value
    writer.close
    # check events from the stream
    data = read_from_stream stream_id, 0, 0
    found = data == nil ? nil : data.find {|e| e['body'] == writer_test_value}
    expect(found).not_to be_nil
  end

  it 'test client without ssl and authentication', :type => 'it-local', :it => true do

    load_config 'spec/reactor.yml'
    # test stream creating
    test_create_stream stream
    # test ttl
    test_ttl stream
    #test writer
    test_writer stream
    #test stream truncating
    test_truncate_stream stream

    expect(true).to eq(true)
  end

  it 'test client with authentication without ssl', :type => 'it-local-auth', :it => true do

    auth_client = AuthenticationClient::AuthenticationClient.new
    config = YAML.load_file('spec/auth_config.yml')
    auth_client.configure config
    load_config 'spec/reactor.yml'
    stream_client.set_auth_client auth_client

    # test stream creating
    test_create_stream stream
    # test ttl
    test_ttl stream
    #test writer
    test_writer stream
    #test stream truncating
    test_truncate_stream stream

    expect(true).to eq(true)
  end


  it 'test client with authentication and ssl', :type => 'it-local-auth-ssl', :it => true do

    auth_client = AuthenticationClient::AuthenticationClient.new
    config = YAML.load_file('spec/auth_config.yml')
    auth_client.configure(config)
    load_config 'spec/reactor_ssh.yml'
    stream_client.set_auth_client(auth_client)

    # test stream creating
    test_create_stream stream
    # test ttl
    test_ttl stream
    #test writer
    test_writer stream
    #test stream truncating
    test_truncate_stream stream

    expect(true).to eq(true)
  end

end
