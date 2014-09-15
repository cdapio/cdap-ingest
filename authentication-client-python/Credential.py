
class Credential(object):

    def __init__(self, name, description, secret):
        self.__name = name
        self.__description = description
        self.__secret = secret

    def get_name(self):
        return self.__name

    def get_description(self):
        return self.__description

    def is_secret(self):
        return self.__secret