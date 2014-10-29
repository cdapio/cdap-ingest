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


import locale
from serviceconnector import ServiceConnector, ConnectionErrorChecker
from streampromise import StreamPromise


class StreamWriter(object):

    def __init__(self, serviceConnector, uri):
        u"""
        Object constructor

        Keyword arguments:
        serviceConnector -- reference to connection pool to communicate
                            with gateway server.
        uri -- REST URL part to perform request.
               Example: '/v2/streams/myStream'
        data -- data to proceed by worker thread.  Please read
                '__workerTarget' documentation.
        """
        if not isinstance(serviceConnector, ServiceConnector):
            raise TypeError(u'parameter should be of type ServiceConnector')

        self.__serviceConnector = serviceConnector
        self.__serviceUri = uri

    def write(self, message, charset=None, headers=None):
        u"""
        Ingest a stream event with a string as body.

        Keyword arguments:
        message -- Data to transmit to REST server.
                   Could be of type None if file field is presented.
        charset -- Message field content charset. Could be of type None.
                   Default value: 'utf-8'
        headers -- Additional HTTP headers. Should be of type 'dict'.

        Returns:
        StreamPromise instance for further handling
        """
        promiseData = {
            u'message': message,
            u'charset': charset,
            u'headers': headers
        }

        return StreamPromise(self.__serviceConnector, self.__serviceUri,
                             promiseData)
