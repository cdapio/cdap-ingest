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
from config import Config
from serviceconnector import ServiceConnector, ConnectionErrorChecker
from streamwriter import StreamWriter


class StreamClient(ConnectionErrorChecker):

    __serviceConnector = None
    __serviceConfig = None

    __GATEWAY_VERSION = '/v2'
    __REQUEST_PLACEHOLDERS = {
        'streamid': '<streamid>'
    }
    __REQUESTS = {'streams': __GATEWAY_VERSION + '/streams'}
    __REQUESTS['stream'] = '{0}/{1}'.format(__REQUESTS['streams'],
                                            __REQUEST_PLACEHOLDERS['streamid'])
    __REQUESTS['consumerid'] = '{0}/{1}'.format(__REQUESTS['stream'],
                                                'consumer-id')
    __REQUESTS['dequeue'] = '{0}/{1}'.format(__REQUESTS['stream'], 'dequeue')
    __REQUESTS['config'] = '{0}/{1}'.format(__REQUESTS['stream'], 'config')
    __REQUESTS['info'] = '{0}/{1}'.format(__REQUESTS['stream'], 'info')
    __REQUESTS['truncate'] = '{0}/{1}'.format(__REQUESTS['stream'], 'truncate')

    def __init__(self, config=Config()):
        self.__serviceConfig = config
        self.__serviceConnector = ServiceConnector(self.__serviceConfig)

    def __prepare_uri(self, requestName, placeholderName='streamid', data=''):
        return self.__REQUESTS[requestName].replace(
            self.__REQUEST_PLACEHOLDERS[placeholderName], data)

    def create(self, stream):
        """
        Creates a stream with the given name.

        Keyword arguments:
        stream -- stream name to create
        """
        uri = self.__prepare_uri('stream', data=stream)

        self.__serviceConnector.request('PUT', uri)

    def set_ttl(self, stream, ttl):
        """
        Set the Time-To-Live (TTL) property of the given stream.

        Keyword arguments:
        stream -- stream name to create or to retrieve
        ttl -- Time-To-Live in seconds
        """
        objectToSend = {
            'ttl': ttl
        }
        uri = self.__prepare_uri('config', data=stream)
        data = json.dumps(objectToSend)

        self.check_response_errors(
            self.__serviceConnector.request('PUT', uri, data)
        )

    def get_ttl(self, stream):
        """
        Retrieves the Time-To-Live (TTL) property of the given stream.

        Keyword arguments:
        stream -- stream name to retrieve ttl for

        Return value:
        Time-To-Live property in seconds
        """
        uri = self.__prepare_uri('info', data=stream)
        response = self.check_response_errors(
            self.__serviceConnector.request('GET', uri)
        )

        return response.json()['ttl']

    def truncate(self, stream):
        """
        Truncates all existing events in the give stream.

        Keyword arguments:
        stream -- stream name to truncate
        """
        uri = self.__prepare_uri('truncate', data=stream)

        self.check_response_errors(
            self.__serviceConnector.request('POST', uri)
        )

    def create_writer(self, stream):
        """
        Creates a {@link StreamWriter} instance for writing events
        to the given stream.

        Keyword arguments:
        stream -- stream name to get StreamWrite instance for
        """

        """
        A bit ugly, but effective method to check if stream exists.
        The main idea is there is could not be presented info for
        invalid stream.
        """
        self.get_ttl(stream)

        uri = self.__prepare_uri('stream', data=stream)

        return StreamWriter(
            ServiceConnector(self.__serviceConfig),
            uri
        )
