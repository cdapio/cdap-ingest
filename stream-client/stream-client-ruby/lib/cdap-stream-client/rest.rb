require 'httparty'

module CDAP

  class Rest
    include HTTParty

    DEFAULT_CONFIG = {
      gateway: '0.0.0.0',
      port: 10000,
      api_version: 'v2',
      ssl: false
    }

    attr_reader :config

    def initialize _config = {}
      @config = DEFAULT_CONFIG.merge _config
      protocol = ssl? ? 'https' : 'http'
      gateway = config[:gateway]
      port = config[:port]
      api_version = config[:api_version]
      self.class.base_uri "#{protocol}://#{gateway}:#{port}/#{api_version}/streams"
    end

    def ssl?
      !!config[:ssl]
    end

    def request method, url, options = {}, &block
      # define variables
      method.downcase!
      url = "/#{url}"

      # setup options
      headers = options[:headers] || {}
      #headers['Content-type'] = 'application/octet-stream' unless headers['Content-type']
      headers['X-Continuuity-ApiKey'] = config['X-Continuuity-ApiKey'] || '' if ssl?
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
