from __future__ import with_statement
import json
from io import open


class Config(object):

    def __init__(self):
        self.__host = u'localhost'
        self.__port = 10000
        self.__ssl = False

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

    def read_from_file(filename):
        newConfig = Config()
        jsonConfig = None

        with open(file) as configFile:
            jsonConfig = json.loads(configFile.read())

        newConfig.host = jsonConfig[u'hostname']
        newConfig.port = jsonConfig[u'port']
        newConfig.ssl = jsonConfig[u'SSL']

        return newConfig
