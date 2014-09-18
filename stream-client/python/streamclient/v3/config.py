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

import json
from caskauthclient.BasicAuthenticationClient import BasicAuthenticationClient


class Config:

    def __init__(self, host='localhost', port=10000, ssl=False,
                 ssl_disable_check=True, filename=u''):
        self.__host = host
        self.__port = port
        self.__ssl = ssl
        self.__ssl_disable_check = ssl_disable_check
        self.__authClient = BasicAuthenticationClient()
        self.__authClient.set_connection_info(self.__host,
                                              self.__port, self.__ssl)
        if filename:
            self.__authClient.configure(filename)

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
    def auth_token(self):
        try:
            return self.__authClient.get_access_token()
        except IOError:
            return u''

    @property
    def is_auth_enabled(self):
        return self.__authClient.is_auth_enabled()

    def read_from_file(filename):
        newConfig = None
        jsonConfig = None

        with open(filename) as configFile:
            jsonConfig = json.loads(configFile.read())

        newConfig = Config(jsonConfig['hostname'],
                           jsonConfig['port'], jsonConfig['SSL'],
                           jsonConfig[u'security_ssl_cert_check'])

        return newConfig
