import abc
import httplib
import logging
from random import randint
import time
import datetime
import requests
import six
from AccessToken import AccessToken

from AuthenticationClient import AuthenticationClient
import urllib2
import RestClientUtils

LOG = logging.getLogger(__name__)


class AbstractAuthenticationClient (AuthenticationClient):

    ACCESS_TOKEN_KEY = "access_token"
    AUTH_URI_KEY = "auth_uri"
    AUTHENTICATION_HEADER_PREFIX_BASIC = "Basic "
    HTTP_PROTOCOL = "http"
    HTTPS_PROTOCOL = "https"
    EXPIRES_IN_KEY = "expires_in"
    TOKEN_TYPE_KEY = "token_type"
    SPARE_TIME_IN_MILLIS = 5000

    def __init__(self):
        self.__access_token = None
        self.__auth_enabled = None
        self.__auth_url = None
        self.__base_url = None

    def invalidate_token(self):
        self.__access_token = None

    def is_auth_enabled(self):
        if not self.__auth_enabled:
            self.__auth_url = self.fetch_auth_url()
        self.__auth_enabled = True if self.__auth_url else False
        return self.__auth_enabled

    def set_connection_info(self, host, port, ssl):
        if self.__base_url:
            raise ValueError("Connection info is already configured!")
        self.__base_url = '%s://%s:%d' % (self.HTTPS_PROTOCOL if ssl else self.HTTP_PROTOCOL, host, port)

    def fetch_auth_url(self):
        if self.__base_url is None:
            raise ValueError("Base authentication client is not configured!")
        LOG.debug("Try to get the authentication URI from the gateway server: {}.", self.__base_url)

        response = requests.get(self.__base_url)

        if response.status_code == httplib.UNAUTHORIZED:
            uri_list = response.json()[self.AUTH_URI_KEY]
            # uri_list = headers[self.AUTH_URI_KEY]
            if uri_list:
                result = uri_list[randint(0, (len(uri_list)-1) ) ]
            return result

    def get_access_token(self):
        if not self.is_auth_enabled():
            raise IOError("Authentication is disabled in the gateway server.")
        if self.__access_token is None or self.is_token_expired():
            request_time = int(round(time.time() * 1000))
            access_token = self.fetch_access_token()
            expiration_time = request_time + access_token.getExpiresIn()*1000 - self.SPARE_TIME_IN_MILLIS
            LOG.debug("Received the access token successfully. Expiration date is {}.",
                      datetime.datetime.fromtimestamp(expiration_time).strftime('%Y-%m-%d %H:%M:%S'))
        return access_token

    def execute(self, request):
        http_response = urllib2.urlopen(request)
        headers = http_response.info()

        RestClientUtils.verifyResponseCode(http_response)
        token_value = headers[self.ACCESS_TOKEN_KEY]
        token_type = headers[self.TOKEN_TYPE_KEY]
        expires_in_str = headers[self.EXPIRES_IN_KEY]
        if token_value or token_type or expires_in_str:
            raise IOError('Unexpected response was received from the authentication server.')
        return AccessToken(token_value, expires_in_str, token_type)

    def get_auth_url(self):
        return self.__auth_url
