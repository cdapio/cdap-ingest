import json


class Config:

    def __init__(self):
        self.__host = 'localhost'
        self.__port = 10000
        self.__ssl = False
        self.__authClient = None

    def set_auth_client(self, client):
        self.__authClient = client

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
            return u''

    def read_from_file(filename):
        newConfig = Config()
        jsonConfig = None

        with open(file) as configFile:
            jsonConfig = json.loads(configFile.read())

        newConfig.host = jsonConfig['hostname']
        newConfig.port = jsonConfig['port']
        newConfig.ssl = jsonConfig['SSL']

        authClient = BasicAuthenticationClient()
        authClient.set_connection_info(newConfig.host, newConfig.port,
                                              newConfig.ssl)
        authClient.configure(filename)

        newConfig.set_auth_client(authClient)

        return newConfig
