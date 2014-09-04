import json


class Config:

    def __init__(self):
        self.__host = 'localhost'
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

        newConfig.host = jsonConfig['hostname']
        newConfig.port = jsonConfig['port']
        newConfig.ssl = jsonConfig['SSL']

        return newConfig
