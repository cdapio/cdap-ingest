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


from os import path
import mimetypes
import logging
from threading import Thread, Lock
from types import FunctionType
from serviceconnector import ServiceConnector, ConnectionErrorChecker,\
    NotFoundError
from io import open

logging.basicConfig()
LOG = logging.getLogger(__name__)

class StreamPromise(ConnectionErrorChecker):
    __HTTP_OK = 200

    u"""
    Type to simulate ListenableFuture functionality of Guava framework.
    """

    def __init__(self, serviceConnector, uri, data):
        u"""
        Object constructor

        Keyword arguments:
        serviceConnector -- reference to connection pool to communicate with
                            gateway
        uri -- REST URL part to perform request.
               Example: '/v2/streams/mystream'
        data -- data to proceed by worker thread.
                Please read '__workerTarget' documentation.
        """
        if not isinstance(serviceConnector, ServiceConnector):
            raise TypeError(u'"serviceConnector" parameter \
                            should be of type ServiceConnector')

        self.__onOkHandlerList = []
        self.__onErrorHandlerList = []

        self.__serviceResponse = None

        self.__serviceConnector = serviceConnector

        self.__handlersLock = Lock()

        self.__workerThread = Thread(target=self.__worker_target,
                                     args=(uri, data))
        self.__workerThread.start()

    def __worker_target(self, uri, dataDict):
        u"""
        Represents logic for performing requests and response handling.
        This method should be invoked in a separate thread to reduce main
        thread locks.

        uri -- REST URL part to perform request.
               Example: '/v2/streams/mystream'
        dataDict -- parameters that are to be passed to REST server:
        {
            'message': '',       Data to transmit to REST server.
                                 Could be of type None if file field
                                 is presented.
            'charset': '',       Message field content charset.
                                 Could be of type None.
                                 Default value: 'utf-8'
            'file': '',          Path to a file which should be trasmited
                                 to REST server.
                                 Could be of type None if message field
                                 is presented.
            'mimetype': '',      Mimetype of a file which should be transmited.
                                 If omitted, application would try to
                                 determine mimetype itself.
            'headers': dict      Additional HTTP headers.
        }
        """
        dataToSend = None
        headersToSend = None

        if u'message' not in dataDict and u'file' not in dataDict:
            raise TypeError(u'parameter should contain "message" \
                            or "file" field')

        if u'message' in dataDict and not isinstance(dataDict[u'message'], (unicode, str)):
            raise TypeError(u'"message" field should be of type \
                            "string" or "bytes"')

        if u'message' in dataDict and isinstance(dataDict[u'message'], unicode) \
           and u'charset' not in dataDict:
            raise TypeError(u'parameter should contain "charset" field \
                            in case if "message" is string')

        if u'headers' in dataDict and dataDict[u'headers'] \
           and not isinstance(dataDict[u'headers'], dict):
            raise TypeError(u'"headers" field should be of type dict')

        u"""
        In case if message is of type 'bytes' it would not be rewritten
        by next if block
        """
        if u'message' in dataDict:
            dataToSend = dataDict[u'message']

        if u'message' in dataDict and isinstance(dataDict[u'message'], unicode):
            if u'charset' in dataDict and dataDict[u'charset']:
                dataToSend = dataDict[u'message'].encode(dataDict[u'charset'])
            else:
                dataToSend = dataDict[u'message'].encode()

        if u'headers' in dataDict:
            headersToSend = dataDict[u'headers']

        self.__serviceResponse = self.__serviceConnector.request(
            u'POST', uri, dataToSend, headersToSend)

        self.__response_check_target()


    def __response_check_target(self):
        u"""
        Checks for status of HTTP response from Gateway server and
        fires handlers according to status code.
        """
        self.__handlersLock.acquire()
        if self.__serviceResponse:
            funcList = []
            if self.__HTTP_OK == self.__serviceResponse.status_code:
                funcList = self.__onOkHandlerList
            else:
                funcList = self.__onErrorHandlerList

            for callback in funcList:
                try:
                    callback(self.__serviceResponse)
                except Exception as e:
                    LOG.exception("Failed to execute callback function.")

            del funcList[:]
        self.__handlersLock.release()

    def on_response(self, success_handler, error_handler=None):
        u"""
        Sets up handlers for success and error responses.

        Keyword arguments:
        success_handler -- Handler to be called in case of successful response.
        error_handler -- Handler to be called in case of failed response.
                 Could be of type None.  In that case would be _identical_
                 to a successful response.

        Handlers should be a function declared with next signature:

        def coolErrorHandler(httpResponseObject):
            ...
            Error handling response
            ...
        """
        if not isinstance(success_handler, FunctionType) or \
           (error_handler is not None \
            and not isinstance(error_handler, FunctionType)):
            raise TypeError(u'parameters should be functions')

        self.__handlersLock.acquire()
        self.__onOkHandlerList.append(success_handler)
        if error_handler:
            self.__onErrorHandlerList.append(error_handler)
        self.__handlersLock.release()

        self.__handlerThread = Thread(target=self.__response_check_target)
        self.__handlerThread.start()
