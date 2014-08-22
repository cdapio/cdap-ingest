require 'yaml'
require 'net/http'
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
      "#{protocol}://#{config['gateway']}:#{@port}/#{@api_version}"
    end

    def self.request type, url
      type.capitalize!
      uri = URI "#{base_url}/#{url}"
      Net::HTTP.start(uri.hostname, uri.port, use_ssl: ssl?) do |http|
        request = instance_eval("Net::HTTP::#{type}").new uri
        request['X-Continuuity-ApiKey'] = config['X-Continuuity-ApiKey'] if ssl?
        http.request request
      end
    end

  end

end
