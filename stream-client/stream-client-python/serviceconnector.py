#! /usr/bin/env python3
# -*- coding: utf-8 -*-

import requests
from config import Config

class NoFoundException(Exception):
    __errorCode = -1
    __errorMsg = ''

    def __init__(self, code, msg):
        super().__init__()
        self.__errorCode = code
        self.__errorMsg = msg

    def code(self):
        return self.__errorCode

    def message(self):
        return self.__errorMsg

class ConnectionErrorChecker:
    __HTTP_OK = 200

    def checkResponseErrors(self, httpResponse):
        if not self.__HTTP_OK == httpResponse.status_code:
            raise NoFoundException(httpResponse.status_code, httpResponse.reason)

        return httpResponse

class ServiceConnector:
    __base_url = ''
    __connectionConfig = None
    __defaultHeaders = {
        'Authorization': 'Bearer ',
        'X-Continuuity-ApiKey': ''
    }

    def __init__(self, config = Config()):
        if not isinstance(config, Config):
            raise TypeError('parameter should be of type Config')

        self.__connectionConfig = config

        if self.__connectionConfig.getSSL():
            self.__base_url = 'https://'
        else:
            self.__base_url = 'http://'

        self.__base_url += '{0}:{1}'.format(
            self.__connectionConfig.getHost(),
            self.__connectionConfig.getPort()
        )

        self.__defaultHeaders['X-Continuuity-ApiKey'] = self.__connectionConfig.getAPIKey()

    def setAuthorizationToken(self, token):
        self.__defaultHeaders['Authorization'] = 'Bearer ' + token

    def request(self, method, uri, body = None, headers = None):
        headersToSend = self.__defaultHeaders
        url = '{0}{1}'.format(self.__base_url, uri)

        if not None == headers:
            headersToSend.update(headers)

        return requests.request(
            method,
            url,
            data = body,
            headers = headersToSend
        )

    def send(self, uri, fields = None, headers = None):
        headersToSend = self.__defaultHeaders
        url = '{0}{1}'.format(self.__base_url, uri)

        if not None == headers:
            headersToSend.update(headers)

        return requests.post(
            url,
            files = fields,
            headers = headersToSend
        )
