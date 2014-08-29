#! /usr/bin/env python3
# -*- coding: utf-8 -*-

from os import path
import mimetypes
from threading import Thread, Lock
from types import FunctionType
from serviceconnector import ServiceConnector, ConnectionErrorChecker


class StreamPromise(ConnectionErrorChecker):

    """
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
        """
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
            raise TypeError('"serviceConnector" parameter \
                            should be of type ServiceConnector')

        self.__serviceConnector = serviceConnector

        self.__handlersLock = Lock()

        self.__workerThread = Thread(target=self.__workerTarget,
                                     args=(uri, data))
        self.__handlerThread = Thread(target=self.__responseCheckTarget)
        self.__workerThread.start()
        self.__handlerThread.start()

    def __workerTarget(self, uri, dataDict):
        """
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

        if 'message' not in dataDict and 'file' not in dataDict:
            raise TypeError('parameter should contain "message" \
                            or "file" field')

        if 'message' in dataDict and not isinstance(dataDict['message'], str) \
           and not isinstance(dataDict['message'], bytes):
            raise TypeError('"message" field should be of type \
                            "string" or "bytes"')

        if 'message' in dataDict and isinstance(dataDict['message'], str) \
           and 'charset' not in dataDict:
            raise TypeError('parameter should contain "charset" field \
                            in case if "message" is string')

        if 'headers' in dataDict and dataDict['headers'] \
           and not isinstance(dataDict['headers'], dict):
            raise TypeError('"headers" field should be of type dict')

        """
        In case if message is of type 'bytes' it would not be rewriten
        by next if block
        """
        if 'message' in dataDict:
            dataToSend = dataDict['message']

        if 'message' in dataDict and isinstance(dataDict['message'], str):
            if 'charset' in dataDict and dataDict['charset']:
                dataToSend = dataDict['message'].encode(dataDict['charset'])
            else:
                dataToSend = dataDict['message'].encode()

        if 'headers' in dataDict:
            headersToSend = dataDict['headers']

        if 'file' not in dataDict:
            self.__serviceResponse = self.__serviceConnector.request(
                'POST', uri, dataToSend, headersToSend)
        else:
            filepath, filename = path.split(dataDict['file'])
            filemime, fileenc = mimetypes.guess_type(dataDict['file'], False)

            if 'mimetype' in dataDict and dataDict['mimetype'] is not None:
                filemime = dataDict['mimetype']

            file = open(dataDict['file'])

            fields = {
                'file': (
                    filename,
                    file.read(),
                    filemime
                )
            }

            file.close()

            self.__serviceResponse = self.__serviceConnector.send(
                uri, fields, headersToSend)

    def __responseCheckTarget(self):
        """
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
        """
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
            raise TypeError('parameters should be functions')

        self.__handlersLock.acquire()
        self.__onOkHandler = ok
        if None == error:
            self.__onErrorHandler = ok
        else:
            self.__onErrorHandler = error

        self.__handlersLock.release()
        self.__responseCheckTarget()
