import abc
import six


@six.add_metaclass(abc.ABCMeta)
class Credential(object):

    def __init__(self, username, password, secret):
        self.__username = username
        self.__password = password
        self.__secret = secret
