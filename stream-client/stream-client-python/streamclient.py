#! /usr/bin/env python3
# -*- coding: utf-8 -*-

from config import Config
from serviceconnector import ServiceConnector, ConnectionErrorChecker
from streamwriter import StreamWriter
import json

class StreamClient(ConnectionErrorChecker):
    __serviceConnector = None
    __serviceConfig = None

    __BASE_VER = '/v2'
    __REQUEST_PLACEHOLDERS = {
        'streamid': '<streamid>'
    }
    __REQUESTS = { 'streams': __BASE_VER + '/streams' }
    __REQUESTS['stream'] = __REQUESTS['streams'] + '/' + __REQUEST_PLACEHOLDERS['streamid']
    __REQUESTS['consumerid'] = __REQUESTS['stream'] + '/consumer-id'
    __REQUESTS['dequeue'] = __REQUESTS['stream'] + '/dequeue'
    __REQUESTS['config'] = __REQUESTS['stream'] + '/config'
    __REQUESTS['info'] = __REQUESTS['stream'] + '/info'
    __REQUESTS['truncate'] = __REQUESTS['stream'] + '/truncate'

    def __init__(self, config = Config()):
        self.__serviceConfig = config
        self.__serviceConnector = ServiceConnector(self.__serviceConfig)

    def __prepareUri(self, requestName, placeholderName = 'streamid', data = ''):
        return self.__REQUESTS[requestName].replace(self.__REQUEST_PLACEHOLDERS[placeholderName], data)

    def create(self, stream):
        """
        Creates a stream with the given name.

        Keyword arguments:
        stream -- stream name to create
        """
        uri = self.__prepareUri('stream', data=stream)

        self.__serviceConnector.request('PUT', uri)

    def setTTL(self, stream, ttl):
        """
        Set the Time-To-Live (TTL) property of the given stream.

        Keyword arguments:
        stream -- stream name to create or to retrieve
        ttl -- Time-To-Live in seconds
        """
        objectToSend = {
            'ttl': ttl
        }
        uri = self.__prepareUri('config', data=stream)
        data = json.dumps(objectToSend)

        self.checkResponseErrors(
            self.__serviceConnector.request('PUT', uri, data)
        )

    def getTTL(self, stream):
        """
        Retrieves the Time-To-Live (TTL) property of the given stream.

        Keyword arguments:
        stream -- stream name to retrieve ttl for

        Return value:
        Time-To-Live property in seconds
        """
        uri = self.__prepareUri('info', data=stream)
        response = self.checkResponseErrors(
            self.__serviceConnector.request('GET', uri)
        )

        ttl = response.json()['ttl']

        return ttl

    def truncate(self, stream):
        """
        Truncates all existing events in the give stream.

        Keyword arguments:
        stream -- stream name to truncate
        """
        uri = self.__prepareUri('truncate', data=stream)

        self.checkResponseErrors(
            self.__serviceConnector.request('POST', uri)
        )

    def createWriter(self, stream):
        """
        Creates a {@link StreamWriter} instance for writing events to the given stream.

        Keyword arguments:
        stream -- stream name to get StreamWrite instance for
        """

        """
        A bit ugly, but effective method to check if stream exists.
        The main idea is there is could not be presented info for
        invalid stream.
        """
        self.getTTL(stream)

        uri = self.__prepareUri('stream', data=stream)

        return StreamWriter(
            ServiceConnector(self.__serviceConfig),
            uri
        )
