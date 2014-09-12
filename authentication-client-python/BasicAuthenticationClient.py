import base64
import logging
import urllib2
from AbstractAuthenticationClient import AbstractAuthenticationClient
from Config import Config
from Credential import Credential

LOG = logging.getLogger(__name__)

class BasicAuthenticationClient(AbstractAuthenticationClient):

    def __init__(self):
        super(BasicAuthenticationClient, self).__init__()
        self.__username = None
        self.__password = None
        self.__credentials = (Credential(self.USERNAME_PROP_NAME, 'Username for basic authentication.', False),
                              Credential(self.PASSWORD_PROP_NAME, 'Password for basic authentication.', True))

    def get_required_credentials(self):
        pass

    def fetch_access_token(self):
        if not self.__username or not self.__password:
            raise ValueError('Base authentication client is not configured!')
        LOG.debug('Authentication is enabled in the gateway server. Authentication URI {}.', self.get_auth_url())

        request = urllib2.Request(self.getAuthUrl())
        base64string = base64.encodestring('%s:%s' % (self.__username, self.__password)).replace('\n|\r', '')
        request.add_header("Authorization", "Basic %s" % base64string)
        return urllib2.urlopen(request)

    USERNAME_PROP_NAME = 'security.auth.client.username'
    PASSWORD_PROP_NAME = 'security.auth.client.password'

    def configure(self, config_file):
        if self.__username or self.__password:
            raise ValueError('Client is already configured!')
        config = Config().read_from_file(config_file)
        self.__username = config.username
        if not self.__username:
            raise ValueError ('The username property cannot be empty.')

        self.__password = Config.password
        if not self.__password:
            raise ValueError ('The password property cannot be empty.')
