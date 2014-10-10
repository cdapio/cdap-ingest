# -*- coding: utf-8 -*-

#  Copyright © 2014 Cask Data, Inc.
#
#  Licensed under the Apache License, Version 2.0 (the "License"); you may not
#  use this file except in compliance with the License. You may obtain a copy of
#  the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
#  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
#  License for the specific language governing permissions and limitations under
#  the License.

from __future__ import with_statement


DEFAULT_HOST = u'localhost'
DEFAULT_PORT = 10000
DEFAULT_SSL = False
DEFAULT_VERIFY_SSL_CERT = True


class Config(object):

    def __init__(self, host=DEFAULT_HOST, port=DEFAULT_PORT, ssl=DEFAULT_SSL,
                 verify_ssl_cert=DEFAULT_VERIFY_SSL_CERT):
        self.__host = host
        self.__port = port
        self.__ssl = ssl
        self.__verify_ssl_cert = verify_ssl_cert
        self.__authClient = None

    def set_auth_client(self, client):
        self.__authClient = client

    @property
    def host(self):
        return self.__host

    @host.setter
    def host(self, hostname):
        self.__host = hostname

    @property
    def port(self):
        return self.__port

    @port.setter
    def port(self, port):
        self.__port = port

    @property
    def ssl(self):
        return self.__ssl

    @ssl.setter
    def ssl(self, ssl):
        self.__ssl = ssl

    @property
    def ssl_cert_check(self):
        return self.__verify_ssl_cert

    @ssl_cert_check.setter
    def ssl_cert_check(self, state):
        self.__verify_ssl_cert = state

    @property
    def auth_token(self):
        if self.__authClient is None:
            raise AttributeError("Authentication Client is not set.")
        return self.__authClient.get_access_token()

    @property
    def is_auth_enabled(self):
        return self.__authClient is not None and self.__authClient.is_auth_enabled()
