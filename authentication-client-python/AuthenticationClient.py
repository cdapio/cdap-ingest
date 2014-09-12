import abc
import six


@six.add_metaclass(abc.ABCMeta)
class AuthenticationClient(object):

    @abc.abstractmethod
    def configure(self, host, port, ssl):
        return

    @abc.abstractmethod
    def get_access_token(self, credentials):
        return

    @abc.abstractmethod
    def is_auth_enabled(self):
        return

    @abc.abstractmethod
    def set_connection_info(self, host, port, ssl):
        return

    @abc.abstractmethod
    def get_required_credentials(self):
        return

    @abc.abstractmethod
    def is_auth_enabled(self):
        return

    @abc.abstractmethod
    def fetch_access_token(self):
        return

    @abc.abstractmethod
    def invalidate_token(self):
        return