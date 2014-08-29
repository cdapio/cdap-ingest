#! /usr/bin/env python2
# -*- coding: utf-8 -*-

from os import path
import mimetypes
from threading import Thread, Lock
from types import FunctionType
from serviceconnector import ServiceConnector, ConnectionErrorChecker
from io import open


class StreamPromise(ConnectionErrorChecker):

    u"""
    Type to simulate ListenableFuture functionality of Guava framework.
    """
    __workerThread = None
    __handlerThread = None
    __serviceConnector = None
    __serviceResponse = None
    __onOkHandler = None
    __onErrorHandler = None
    __handlersLock = None

    def __init__(self, serviceConnector, uri, data):
        u"""
        Object constructor

        Keyword arguments:
        serviceConnector -- reference to connection pool to communicate with
                            gateway
        uri -- REST URL part to perform request.
               Example: '/v2/strems/mystream'
        data -- data to proceed by worker thread.
                Please read '__workerTarget' documentation.
        """
        if not isinstance(serviceConnector, ServiceConnector):
            raise TypeError(u'"serviceConnector" parameter \
                            should be of type ServiceConnector')

        self.__serviceConnector = serviceConnector

        self.__handlersLock = Lock()

        self.__workerThread = Thread(target=self.__workerTarget,
                                     args=(uri, data))
        self.__handlerThread = Thread(target=self.__responseCheckTarget)
        self.__workerThread.start()
        self.__handlerThread.start()

    def __workerTarget(self, uri, dataDict):
        u"""
        Represents logic for performing requests and repsonses handling.
        This method should be invoked in separate thread to reduce main
        thread locks.

        uri -- REST URL part to perform request.
               Example: '/v2/strems/myStream'
        dataDict -- parameters that should be passed to REST server:
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
                                 If is omitted, application would try to
                                 determine mimetype itself.
            'headers': dict      Additional HTTP headers.
        }
        """
        dataToSend = None
        headersToSend = None

        if u'message' not in dataDict and u'file' not in dataDict:
            raise TypeError(u'parameter should contain "message" \
                            or "file" field')

        if u'message' in dataDict and not isinstance(dataDict[u'message'], unicode) \
           and not isinstance(dataDict[u'message'], str):
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
        In case if message is of type 'bytes' it would not be rewriten
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

        if u'file' not in dataDict:
            self.__serviceResponse = self.__serviceConnector.request(
                u'POST', uri, dataToSend, headersToSend)
        else:
            filepath, filename = path.split(dataDict[u'file'])
            filemime, fileenc = mimetypes.guess_type(dataDict[u'file'], False)

            if u'mimetype' in dataDict and dataDict[u'mimetype'] is not None:
                filemime = dataDict[u'mimetype']

            file = open(dataDict[u'file'])

            fields = {
                u'file': (
                    filename,
                    file.read(),
                    filemime
                )
            }

            file.close()

            self.__serviceResponse = self.__serviceConnector.send(
                uri, fields, headersToSend)

    def __responseCheckTarget(self):
        u"""
        Checks for status of HTTP response from Gateway server and
        fires handlers according to status code.
        """
        self.__workerThread.join()

        self.__handlersLock.acquire()
        if self.__serviceResponse and self.__onOkHandler \
           and self.__onErrorHandler:
            try:
                self.__onOkHandler(
                    self.checkResponseErrors(self.__serviceResponse)
                )
            except NoFoundError:
                self.__onErrorHandler(self.__serviceResponse)
            finally:
                self.__onOkHandler = self.__onErrorHandler = None
        self.__handlersLock.release()

    def onResponse(self, ok, error=None):
        u"""
        Sets up handlers for successful and error responses.

        Keyword arguments:
        ok -- Handler to be called in case of successful response.
        error -- Handler to be called in case of failed response.
                 Could be of type None.  In that case would be the _same_
                 as for a successful case.

        Handlers should be a function daclared with next signature:

        def coolErrorHandler( httpResponseObject):
            ...
            fooling around with response
            ...
        """
        if not isinstance(ok, FunctionType) or \
           (error is not None and not isinstance(error, FunctionType)):
            raise TypeError(u'parameters should be functions')

        self.__handlersLock.acquire()
        self.__onOkHandler = ok
        if None == error:
            self.__onErrorHandler = ok
        else:
            self.__onErrorHandler = error

        self.__handlersLock.release()
        self.__responseCheckTarget()
