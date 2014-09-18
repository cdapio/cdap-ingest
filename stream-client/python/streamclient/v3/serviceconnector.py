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


import requests
from config import Config


class NotFoundError(Exception):

    def __init__(self, code, msg):
        super().__init__()
        self.__errorCode = code
        self.__errorMsg = msg

    def code(self):
        return self.__errorCode

    def message(self):
        return self.__errorMsg

    def __str__(self):
        return "Code: %s \nMessage: %s" % (self.__errorCode, self.__errorMsg)


class ConnectionErrorChecker:

    __HTTP_OK = 200

    def check_response_errors(self, httpResponse):
        if self.__HTTP_OK is not httpResponse.status_code:
            raise NotFoundError(httpResponse.status_code, httpResponse.reason)

        return httpResponse


class ServiceConnector:

    __DEFAULT_CONFIG = u'config.json'
    __base_url = '{0}://{1}:{2}'
    __defaultHeaders = {
        'Authorization': '{0} {1}'
    }

    def __init__(self, config=Config.read_from_file(__DEFAULT_CONFIG)):
        if not isinstance(config, Config):
            raise TypeError('parameter should be of type Config')

        self.__connectionConfig = config

        if self.__connectionConfig.ssl:
            self.__protocol = 'https'
        else:
            self.__protocol = 'http'

        self.__base_url = self.__base_url.format(
            self.__protocol,
            self.__connectionConfig.host,
            self.__connectionConfig.port
        )

    def request(self, method, uri, body=None, headers=None):
        headersToSend = self.__defaultHeaders
        url = '{0}{1}'.format(self.__base_url, uri)

        if self.__connectionConfig.is_auth_enabled:
            headersToSend['Authorization'] = headersToSend['Authorization']\
                .format(self.__connectionConfig.auth_token.token_type,
                        self.__connectionConfig.auth_token.value)

        if headers is not None:
            headersToSend.update(headers)

        return requests.request(method, url, data=body, headers=headersToSend)

    def send(self, uri, fields=None, headers=None):
        headersToSend = self.__defaultHeaders
        url = '{0}{1}'.format(self.__base_url, uri)

        if self.__connectionConfig.is_auth_enabled:
            headersToSend['Authorization'] = headersToSend['Authorization']\
                .format(self.__connectionConfig.auth_token.token_type,
                        self.__connectionConfig.auth_token.value)

        if headers is not None:
            headersToSend.update(headers)

        return requests.post(url, files=fields, headers=headersToSend)
