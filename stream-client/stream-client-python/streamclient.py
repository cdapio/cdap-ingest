#! /usr/bin/env python3
# -*- coding: utf-8 -*-

from serviceconnector import *
import json

class StreamClient(ConnectionErrorChecker):
    __serviceConnector = None

    __BASE_URL = '/v2'
    __REQUEST_PLACEHOLDERS = {
        'streamid': '<streamid>'
    }
    __REQUESTS = { 'streams': __BASE_URL + '/streams' }
    __REQUESTS['stream'] = __REQUESTS['streams'] + '/' + __REQUEST_PLACEHOLDERS['streamid']
    __REQUESTS['consumerid'] = __REQUESTS['stream'] + '/consumer-id'
    __REQUESTS['dequeue'] = __REQUESTS['stream'] + '/dequeue'
    __REQUESTS['config'] = __REQUESTS['stream'] + '/config'
    __REQUESTS['info'] = __REQUESTS['stream'] + '/info'
    __REQUESTS['truncate'] = __REQUESTS['stream'] + '/truncate'
    
    def __init__(self, config = Config()):
        self.__serviceConnector = ServiceConnector(config)

    def __prepareUri(self, requestName, placeholderName = 'streamid', data = ''):
        return self.__REQUESTS[requestName].replace(self.__REQUEST_PLACEHOLDERS[placeholderName], data)

    def create(self, stream):
        uri = self.__prepareUri('stream', data=stream)
        
        self.__serviceConnector.request('PUT', uri)
        
    def setTTL(self, stream, ttl):
        objectToSend = {
            'ttl': ttl
        }
        uri = self.__prepareUri('config', data=stream)
        data = json.dumps(objectToSend)

        self.checkResponseErrors(
            self.__serviceConnector.request('PUT', uri, data)
        )

    def getTTL(self, stream):
        uri = self.__prepareUri('info', data=stream)
        response = self.checkResponseErrors(
            self.__serviceConnector.request('GET', uri)
        )

        ttl = json.loads(response.data.decode('utf-8'))['ttl']
        
        return ttl

    def truncate(self, stream):
        uri = self.__prepareUri('truncate', data=stream)

        self.checkResponseErrors(
            self.__serviceConnector.request('POST', uri)
        )
