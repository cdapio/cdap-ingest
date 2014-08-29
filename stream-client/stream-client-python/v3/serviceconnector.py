#! /usr/bin/env python3
# -*- coding: utf-8 -*-

import requests
from config import Config


class NoFoundError(Exception):

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

    def __str__(self):
        return "Code: %s \nMessage: %s" % (self.__errorCode, self.__errorMsg)


class ConnectionErrorChecker:

    __HTTP_OK = 200

    def checkResponseErrors(self, httpResponse):
        if self.__HTTP_OK is not httpResponse.status_code:
            raise NoFoundError(httpResponse.status_code, httpResponse.reason)

        return httpResponse


class ServiceConnector:

    __protocol = ''
    __base_url = '{0}://{1}:{2}'
    __connectionConfig = None
    __defaultHeaders = {
        'Authorization': 'Bearer '
    }

    def __init__(self, config=Config()):
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

    def setAuthorizationToken(self, token):
        self.__defaultHeaders['Authorization'] = 'Bearer ' + token

    def request(self, method, uri, body=None, headers=None):
        headersToSend = self.__defaultHeaders
        url = '{0}{1}'.format(self.__base_url, uri)

        if headers is not None:
            headersToSend.update(headers)

        return requests.request(method, url, data=body, headers=headersToSend)

    def send(self, uri, fields=None, headers=None):
        headersToSend = self.__defaultHeaders
        url = '{0}{1}'.format(self.__base_url, uri)

        if headers is not None:
            headersToSend.update(headers)

        return requests.post(url, files=fields, headers=headersToSend)
