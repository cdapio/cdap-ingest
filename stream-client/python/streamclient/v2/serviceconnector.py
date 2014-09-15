import requests
from config import Config


class NotFoundError(Exception):

    def __init__(self, code, msg):
        super(self.__class__, self).__init__()
        self.__errorCode = code
        self.__errorMsg = msg

    def code(self):
        return self.__errorCode

    def message(self):
        return self.__errorMsg

    def __str__(self):
        return u"Code: %s \nMessage: %s" % (self.__errorCode, self.__errorMsg)


class ConnectionErrorChecker(object):

    __HTTP_OK = 200

    def check_response_errors(self, httpResponse):
        if self.__HTTP_OK is not httpResponse.status_code:
            raise NotFoundError(httpResponse.status_code, httpResponse.reason)

        return httpResponse


class ServiceConnector(object):

    __DEFAULT_CONFIG = u'config.json'
    __defaultHeaders = {
        u'Authorization': u'Bearer {0}'
    }

    def __init__(self, config=Config.read_from_file(__DEFAULT_CONFIG)):
        self.__base_url = u'{0}://{1}:{2}'

        if not isinstance(config, Config):
            raise TypeError(u'parameter should be of type Config')

        self.__connectionConfig = config

        if self.__connectionConfig.ssl:
            self.__protocol = u'https'
        else:
            self.__protocol = u'http'

        self.__base_url = self.__base_url.format(
            self.__protocol,
            self.__connectionConfig.host,
            self.__connectionConfig.port
        )

    def request(self, method, uri, body=None, headers=None):
        headersToSend = self.__defaultHeaders
        url = u'{0}{1}'.format(self.__base_url, uri)

        headersToSend[u'Authorization'] = headersToSend[u'Authorization']\
            .format(self.__connectionConfig.auth_token)

        if headers is not None:
            headersToSend.update(headers)

        return requests.request(method, url, data=body, headers=headersToSend)

    def send(self, uri, fields=None, headers=None):
        headersToSend = self.__defaultHeaders
        url = u'{0}{1}'.format(self.__base_url, uri)

        headersToSend[u'Authorization'] = headersToSend[u'Authorization']\
            .format(self.__connectionConfig.auth_token)

        if headers is not None:
            headersToSend.update(headers)

        return requests.post(url, files=fields, headers=headersToSend)
