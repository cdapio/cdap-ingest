
import json
from io import open


class Config(object):
    def __init__(self):
        self.__username = None
        self.__password = None

    @property
    def username(self):
        return self.__username

    @username.setter
    def username(self, username):
        self.__username = username

    @property
    def password(self):
        return self.__password

    @password.setter
    def password(self, password):
        self.__password = password

    def read_from_file(self, file):
        new_config = Config()
        json_config = None
        with open(file) as configFile:
            json_config = json.loads(configFile.read())

        new_config.__username = json_config[u'username']
        new_config.__password = json_config[u'password']

        return new_config