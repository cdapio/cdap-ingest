
require 'spec_helper'
require 'authentication-client-ruby'
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
  let(:stream) {"/LogAnalyticsFlow"}

  it 'test client without ssl and authentication' do
    load_config 'spec/reactor.yml'
    result = stream_client.create stream
    res = stream_client.create_writer(stream)

    true.should == true
  end
  it 'test client with authentication without ssl' do

    auth_client = CDAPIngest::AuthenticationClient.new
    auth_client.configure('spec/auth_config.yml')
    load_config 'spec/reactor.yml'
    stream_client.set_auth_client(auth_client)
    result = stream_client.create stream
    res = stream_client.create_writer(stream)

    true.should == true
  end


  it 'test client with authentication and ssl' do

    auth_client = CDAPIngest::AuthenticationClient.new
    auth_client.configure('spec/auth_config.yml')
    load_config 'spec/reactor_ssh.yml'
    stream_client.set_auth_client(auth_client)
    result = stream_client.create stream
    res = stream_client.create_writer(stream)

    true.should == true
  end

end