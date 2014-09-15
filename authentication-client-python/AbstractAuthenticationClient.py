import httplib
import json
import logging
from random import randint
import time
import datetime
import requests
from AccessToken import AccessToken

from AuthenticationClient import AuthenticationClient
from RestClientUtils import RestClientUtils

LOG = logging.getLogger(__name__)


class AbstractAuthenticationClient(AuthenticationClient):
    ACCESS_TOKEN_KEY = u"access_token"
    AUTH_URI_KEY = u"auth_uri"
    AUTHENTICATION_HEADER_PREFIX_BASIC = u"Basic "
    HTTP_PROTOCOL = u"http"
    HTTPS_PROTOCOL = u"https"
    EXPIRES_IN_KEY = u"expires_in"
    TOKEN_TYPE_KEY = u"token_type"
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
            raise ValueError(u"Connection info is already configured!")
        self.__base_url = u'%s://%s:%d' % (self.HTTPS_PROTOCOL if ssl else self.HTTP_PROTOCOL, host, port)

    def fetch_auth_url(self):
        if self.__base_url is None:
            raise ValueError(u"Base authentication client is not configured!")
        LOG.debug(u"Try to get the authentication URI from the gateway server: {}.", self.__base_url)

        response = requests.get(self.__base_url)

        if response.status_code == httplib.UNAUTHORIZED:
            uri_list = response.json()[self.AUTH_URI_KEY]
            if uri_list:
                result = uri_list[randint(0, (len(uri_list) - 1))]
            return result

    def get_access_token(self):
        if not self.is_auth_enabled():
            raise IOError(u"Authentication is disabled in the gateway server.")
        if self.__access_token is None or self.is_token_expired():
            request_time = int(round(time.time() * 1000))
            access_token = self.fetch_access_token()
            expire_time = access_token.expires_in
            expiration_time = request_time + access_token.expires_in - self.SPARE_TIME_IN_MILLIS
            LOG.debug(u"Received the access token successfully. Expiration date is {}.",
                      datetime.datetime.fromtimestamp(expiration_time/1000).strftime(u'%Y-%m-%d %H:%M:%S.%f'))
        return access_token

    def execute(self, request_str):
        response = requests.get(self.auth_url, headers=json.loads(request_str))
        status_code = response.status_code
        headers = response.headers

        RestClientUtils.verify_response_code(status_code)
        t= response.content
        token_value = response.json()[self.ACCESS_TOKEN_KEY]
        token_type = response.json()[self.TOKEN_TYPE_KEY]
        expires_in_str = response.json()[self.EXPIRES_IN_KEY]
        if not token_value or not token_type or not expires_in_str:
            raise IOError(u'Unexpected response was received from the authentication server.')
        return AccessToken(token_value, expires_in_str, token_type)

    @property
    def auth_url(self):
        return self.__auth_url
