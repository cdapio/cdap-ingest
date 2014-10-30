#  Copyright 2014 Cask Data, Inc.
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

require 'httparty'
require 'thread'
require 'promise.rb'

module CDAPIngest
  
end

require "stream-client-ruby/stream_client"
require "stream-client-ruby/stream_writer"
require "stream-client-ruby/rest"
require "stream-client-ruby/version"
