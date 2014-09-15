import base64
import logging
import urllib2
from flask import json
from AbstractAuthenticationClient import AbstractAuthenticationClient
from Config import Config
from Credential import Credential

LOG = logging.getLogger(__name__)


class BasicAuthenticationClient(AbstractAuthenticationClient):

    USERNAME_PROP_NAME = u'security.auth.client.username'
    PASSWORD_PROP_NAME = u'security.auth.client.password'

    def __init__(self):
        super(BasicAuthenticationClient, self).__init__()
        self.__username = None
        self.__password = None
        self.__credentials = (Credential(self.USERNAME_PROP_NAME, u'Username for basic authentication.', False),
                              Credential(self.PASSWORD_PROP_NAME, u'Password for basic authentication.', True))

    def get_required_credentials(self):
        return self.__credentials

    def fetch_access_token(self):
        if not self.__username or not self.__password:
            raise ValueError(u'Base authentication client is not configured!')
        LOG.debug(u'Authentication is enabled in the gateway server. Authentication URI {}.', self.auth_url)

        base64string = base64.encodestring('%s:%s' % (self.__username, self.__password)).replace('\n', '')
        auth_header = json.dumps( {u"Authorization": u"Basic %s" % base64string })

        return self.execute(auth_header)

    def configure(self, config_file):
        if self.__username or self.__password:
            raise ValueError(u'Client is already configured!')
        config = Config().read_from_file(config_file)
        self.__username = config.username
        if not self.__username:
            raise ValueError(u'The username property cannot be empty.')

        self.__password = config.password
        if not self.__password:
            raise ValueError(u'The password property cannot be empty.')
