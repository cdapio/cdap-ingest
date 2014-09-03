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

#encoding: utf-8
$:.push File.expand_path("../lib", __FILE__)
require "stream-client-ruby/version"

Gem::Specification.new do |s|
  s.name        = "stream-client-ruby"
  s.version     = CDAPIngest::VERSION
  s.authors     = ["Dmytro Skavulyak"]
  s.email       = ["dskavulyak@continuuity.com"]
  #s.homepage    = "http://github.com/username/gem-name"
  s.summary     = "A Ruby client for Continuuity Reactor"
  s.description = "Provide a more Ruby experience when using Continuuity Reactor."

  s.rubyforge_project = "stream-client-ruby"

  s.add_dependency 'httparty'
  s.add_dependency 'thread'
  s.add_dependency 'promise.rb'
  s.add_dependency 'em-http-request'
  
  s.add_development_dependency 'pry'

  # s.files         = `git ls-files`.split("\n")
  # s.test_files    = `git ls-files -- {test,spec,features}/*`.split("\n")
  # s.executables   = `git ls-files -- bin/*`.split("\n").map{ |f| File.basename(f) }
  s.require_paths = ["lib"]
end
