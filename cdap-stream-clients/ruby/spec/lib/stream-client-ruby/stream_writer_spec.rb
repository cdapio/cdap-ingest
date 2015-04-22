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

describe CDAPIngest::StreamWriter do
  
  let(:stream_writer){ CDAPIngest::StreamWriter.new "text" }
  let(:file){ "spec/fixtures/1MB" }

  it {
    VCR.use_cassette('stream_writer') { 
      expect(stream_writer).to be_a CDAPIngest::StreamWriter
    }
  }

  it { expect(CDAPIngest::StreamWriter).to respond_to(:new) }

  it { expect(stream_writer).to respond_to(:write) }

  it {
    VCR.use_cassette('stream_writer_write') {
      result = stream_writer.write "test_body"
      expect(result).to be_a Thread::Future
    }
  }

  it { expect(stream_writer).to respond_to(:close) }

end
