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

describe CDAP::RestClient do
  let(:rest) { VCR.use_cassette('rest') { CDAP::RestClient.new } }

  it { expect(rest).to be_a CDAP::RestClient }

  it { expect(CDAP::RestClient).to respond_to(:new) }

  it { expect(rest).to respond_to(:ssl?) }

  it do
    result = rest.ssl?
    expect(result).to eq false
  end

  it { expect(rest).to respond_to(:request) }

  it do
    VCR.use_cassette('rest_request_get') do
      result = rest.request 'get', 'text'
      expect(result.class).to eq HTTParty::Response
    end
  end

  it { expect(rest).to respond_to(:to_s) }

  it do
    result = rest.to_s 'test_str'
    expect(result).to eq 'test_str'
  end
end
