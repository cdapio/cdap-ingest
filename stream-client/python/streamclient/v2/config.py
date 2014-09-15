from __future__ import with_statement
import json
from io import open
from caskauthclient.BasicAuthenticationClient import BasicAuthenticationClient


class Config(object):

    def __init__(self):
        self.__host = u'localhost'
        self.__port = 10000
        self.__ssl = False
        self.__authClient = BasicAuthenticationClient()

    @property
    def host(self):
        return self.__host

    @host.setter
    def host(self, hostname):
        self.__host = hostname

    @property
    def port(self):
        return self.__port

    @port.setter
    def port(self, port):
        self.__port = port

    @property
    def ssl(self):
        return self.__ssl

    @ssl.setter
    def ssl(self, ssl):
        self.__ssl = ssl

    @property
    def auth_token(self):
        try:
            return self.__authClient.get_access_token()
        except IOError:
            return ''

    def read_from_file(filename):
        newConfig = Config()
        jsonConfig = None

        with open(file) as configFile:
            jsonConfig = json.loads(configFile.read())

        newConfig.host = jsonConfig[u'hostname']
        newConfig.port = jsonConfig[u'port']
        newConfig.ssl = jsonConfig[u'SSL']

        self.__authClient.set_connection_info(newConfig.host, newConfig.port,
                                              newConfig.ssl)
        self.__authClient.configure(filename)

        return newConfig
