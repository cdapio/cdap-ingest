#! /usr/bin/env python3
# -*- coding: utf-8 -*-

from urllib3 import HTTPConnectionPool, HTTPSConnectionPool
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
        if not self.__HTTP_OK == httpResponse.status:
            raise NoFoundException(httpResponse.status, httpResponse.reason)

        return httpResponse

class ServiceConnector:
    __MAX_CONNECTION_TO_SAVE = 10
    __connectionPool = None
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
            self.__connectionPool = HTTPSConnectionPool(
                self.__connectionConfig.getHost(),
                self.__connectionConfig.getPort(),
                maxsize = self.__MAX_CONNECTION_TO_SAVE,
                block = False )
        else:
            self.__connectionPool = HTTPConnectionPool(
                self.__connectionConfig.getHost(),
                self.__connectionConfig.getPort(),
                maxsize = self.__MAX_CONNECTION_TO_SAVE,
                block = False )

        self.__defaultHeaders['X-Continuuity-ApiKey'] = self.__connectionConfig.getAPIKey()

    def __del__(self):
        self.__connectionPool.close()

    def setAuthorizationToken(self, token):
        self.__defaultHeaders['Authorization'] = 'Bearer ' + token

    def request(self, method, uri, body = None, headers = None):
        headersToSend = self.__defaultHeaders

        if not None == headers:
            headersToSend.update(headers)

        return self.__connectionPool.urlopen(method, uri, body, headersToSend, release_conn = True)

    def send(self, uri, fields = None, headers = None):
        headersToSend = self.__defaultHeaders

        if not None == headers:
            headersToSend.update(headers)

        return self.__connectionPool.request_encode_body('POST', uri, fields, headersToSend)
