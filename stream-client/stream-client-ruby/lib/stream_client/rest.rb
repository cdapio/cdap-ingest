require 'yaml'
require 'net/http'
require 'json'
require 'promise'

module StreamClient

  class Rest

    @port = 10000
    @api_version = 'v2'
    @config = YAML.load_file './config/rest.yml'

    def self.config
      @config
    end

    def self.ssl?
      config['ssl']
    end

    def self.protocol
      if ssl?
        'https'
      else
        'http'
      end
    end

    def self.base_url
      "#{protocol}://#{config['gateway']}:#{@port}/#{@api_version}/streams"
    end

    def self.request type, url, params = nil
      type.capitalize!
      uri = URI "#{base_url}/#{url}"
      response = Net::HTTP.start(uri.hostname, uri.port, use_ssl: ssl?) do |http|
        request = instance_eval("Net::HTTP::#{type}").new uri
        request['X-Continuuity-ApiKey'] = config['X-Continuuity-ApiKey'] if ssl?
        request.body = params.to_json if params
        http.request request
      end
      is_json = response['content-type'] == 'application/json'
      response.body = JSON.parse(response.body) if is_json
      puts "Status code: #{response.code}"
      response
    end

  end

end
