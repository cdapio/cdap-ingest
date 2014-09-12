

class AccessToken:

    def __init__(self, value, expires_in, token_type):
        self.__value = value
        self.__expires_in = expires_in
        self.__token_type = token_type
    pass

    @property
    def value(self):
        return self.__value

    @property
    def expires_in(self):
        return self.__expires_in

    @property
    def token_type(self):
        return self.__token_type