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

  let(:rest) {CDAPIngest::Rest.new}
  let(:stream_client){ CDAPIngest::StreamClient.new }
  let(:stream) {"LogAnalyticsFlow"}

  def test_create_stream stream_id
    stream_client.create stream_id
    data = rest.request('get', '').parsed_response
    found = data.find {|e| e['name'] == stream_id}
    expect(found).not_to be_nil
  end

  def test_truncate_stream stream_id
    stream_client.truncate stream_id
    response = rest.request('get', "#{stream_id}/events")
    expect(response.code).to eq 204
  end

  def test_ttl stream_id
    ttl_test_value = 256
    stream_client.set_ttl stream_id, ttl_test_value
    ttl = stream_client.get_ttl stream_id
    expect(ttl).to eq ttl_test_value
  end

  def test_writer stream_id
    writer_test_value = 'test_body'
    writer = stream_client.create_writer stream_id
    writer.write writer_test_value
    data = rest.request('get', "#{stream_id}/events").parsed_response
    found = data.find {|e| e['body'] == writer_test_value}
    expect(found).not_to be_nil
  end

  it 'test client without ssl and authentication' do

    load_config 'spec/reactor.yml'
    # test stream creating
    test_create_stream stream
    # test ttl
    test_ttl stream
    #test writer
    test_writer stream
    #test stream truncating
    test_truncate_stream stream

    true.should == true
  end

  it 'test client with authentication without ssl' do

    auth_client = AuthenticationClient::AuthenticationClient.new
    config = YAML.load_file('spec/auth_config.yml')
    auth_client.configure(config)
    load_config 'spec/reactor.yml'
    stream_client.set_auth_client(auth_client)
    result = stream_client.create stream
    res = stream_client.create_writer(stream)

    true.should == true
  end


  it 'test client with authentication and ssl' do

    auth_client = AuthenticationClient::AuthenticationClient.new
    config = YAML.load_file('spec/auth_config.yml')
    auth_client.configure(config)
    load_config 'spec/reactor_ssh.yml'
    stream_client.set_auth_client(auth_client)
    result = stream_client.create stream
    res = stream_client.create_writer(stream)

    true.should == true
  end

end