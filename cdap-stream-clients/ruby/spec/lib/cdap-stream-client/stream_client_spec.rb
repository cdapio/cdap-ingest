#  Copyright 2014-2015 Cask Data, Inc.
#
#  Licensed under the Apache License, Version 2.0 (the "License"); you may not
#  use this file except in compliance with the License. You may obtain a copy of
#  the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
#  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
#  License for the specific language governing permissions and limitations under
#  the License.

require 'spec_helper'

describe CDAP::StreamClient do
  let(:stream_client) { CDAP::StreamClient.new }
  let(:stream) { 'rspec_text' }
  let(:non_existing_stream) { 'non_existing_stream' }

  it do
    VCR.use_cassette('stream_client') do
      expect(stream_client).to be_a CDAP::StreamClient
    end
  end

  it { expect(CDAP::StreamClient).to respond_to(:new) }

  it { expect(stream_client).to respond_to(:create) }

  it do
    VCR.use_cassette('stream_client_create') do
      result = stream_client.create stream
      expect(result.class).to eq HTTParty::Response
    end
  end

  it { expect(stream_client).to respond_to(:set_ttl) }

  it do
    VCR.use_cassette('stream_client_set_ttl') do
      result = stream_client.set_ttl stream, 22_500
      expect(result.class).to eq HTTParty::Response
    end
  end

  it { expect(stream_client).to respond_to(:get_ttl) }

  it do
    VCR.use_cassette('stream_client_get_ttl') do
      result = stream_client.get_ttl stream
      expect(result).to eq 22_500
    end
  end

  it do
    VCR.use_cassette('no_stream_client_get_ttl') do
      expect do
        result = stream_client.get_ttl non_existing_stream
      end.to raise_error 'The request did not address any of the known URIs'
    end
  end

  it { expect(stream_client).to respond_to(:truncate) }

  it do
    VCR.use_cassette('stream_client_truncate') do
      result = stream_client.truncate stream
      expect(result.class).to eq HTTParty::Response
    end
  end

  it do
    VCR.use_cassette('no_stream_client_truncate') do
      expect do
        result = stream_client.truncate non_existing_stream
      end.to raise_error 'The request did not address any of the known URIs'
    end
  end

  it { expect(stream_client).to respond_to(:create_writer) }

  it do
    VCR.use_cassette('stream_client_create_writer') do
      result = stream_client.create_writer stream
      expect(result.class).to eq CDAP::StreamWriter
    end
  end

  it do
    VCR.use_cassette('stream_client_create_writer') do
      result = stream_client.create_writer non_existing_stream
      expect(result.class).to eq CDAP::StreamWriter
    end
  end
end
