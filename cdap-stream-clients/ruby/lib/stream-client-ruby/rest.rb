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

require 'httparty'
require 'em-http-request'

module CDAPIngest

  class Rest
    include HTTParty

    attr_reader :config

    class << self
      attr_accessor :gateway
      attr_accessor :port
      attr_accessor :api_version
      attr_accessor :ssl
      attr_accessor :namespace
    end

    def initialize
      protocol = self.class.ssl ? 'https' : 'http'
      self.class.base_uri "#{protocol}://#{self.class.gateway}:#{self.class.port}/#{self.class.api_version}/namespaces/#{self.class.namespace}/streams"
      @auth_client = nil
    end

    def ssl?
      !!self.class.ssl
    end

    def is_auth_enabled
      @auth_client ? @auth_client.auth_enabled? : false
    end

    def get_access_token
      @auth_client.get_access_token
    end

    def set_auth_client auth_client
      @auth_client = auth_client
      @auth_client.set_connection_info(self.class.gateway, self.class.port, self.class.ssl)
    end

    def request (method, url, options = {}, &block)
      # define variables
      method.downcase!
      url = "#{self.class.base_uri}/#{url}"

      # setup options
      headers = options[:headers] || {}
      # The name of this header will change in the future to take out 'continuuity'
      headers['X-Continuuity-ApiKey'] = config['api_key'] || '' if ssl?
      if @auth_client && self.is_auth_enabled
        token = self.get_access_token
        headers['Authorization'] = "'#{token.token_type} #{token.value}'"
      end
      options[:headers] = headers
      options[:body] = to_s options[:body]

      # send request
      case method
        when 'get'
          response = self.class.get url, options, &block
        when 'post'
          response = self.class.post url, options, &block
        when 'put'
          response = self.class.put url, options, &block
        else
          raise 'Unknown http method'
      end

      # process response
      unless response.response.is_a?(Net::HTTPSuccess)
        error = ResponseError.new response
        case response.code
          when 400
            raise error, 'The request had a combination of parameters that is not recognized'
          when 401
            raise error, 'The request did not contain an authentication token'
          when 403
            raise error, 'The request was authenticated but the client does not have permission'
          when 404
            raise error, 'The request did not address any of the known URIs'
          when 405
            raise error, 'A request was received with a method not supported for the URI'
          when 409
            raise error, 'A request could not be completed due to a conflict with the current resource state'
          when 500
            raise error, 'An internal error occurred while processing the request'
          when 501
            raise error, 'A request contained a query that is not supported by this API'
          else
            raise error, 'Unknown http error'
        end
      end

      response
    end

    def to_s body
      body = body.to_json if body.is_a?(Hash)
      body.to_s
    end

  end

end

