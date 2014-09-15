
import json
from io import open


class Config(object):

    def __init__(self):
        self.__username = None
        self.__password = None

    @property
    def security_auth_client_username(self):
        return self.__username

    @security_auth_client_username.setter
    def security_auth_client_username(self, username):
        self.__username = username

    @property
    def security_auth_client_password(self):
        return self.__password

    @security_auth_client_password.setter
    def security_auth_client_password(self, password):
        self.__password = password

    def read_from_file(self, file):
        new_config = Config()
        json_config = None
        with open(file) as configFile:
            json_config = json.loads(configFile.read())

        new_config.__username = json_config[u'security_auth_client_username']
        new_config.__password = json_config[u'security_auth_client_password']

        return new_config