#  Copyright 2014 Cask, Inc.
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

describe CDAPIngest::Rest do
  
  let(:rest){ VCR.use_cassette('rest') { CDAPIngest::Rest.new } }

  it { expect(rest).to be_a CDAPIngest::Rest }

  it { expect(CDAPIngest::Rest).to respond_to(:new) }

  it { expect(rest).to respond_to(:ssl?) }

  it {
    result = rest.ssl?
    expect(result).to eq false
  }

  it { expect(rest).to respond_to(:request) }

  it {
    VCR.use_cassette('rest_request_get') {
      result = rest.request 'get', "text/info"
      expect(result.class).to eq HTTParty::Response
    }
  }

  it { expect(rest).to respond_to(:to_s) }

  it {
    result = rest.to_s "test_str"
    expect(result).to eq "test_str"
  }
  
end
