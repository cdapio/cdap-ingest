#! /usr/bin/env python3
# -*- coding: utf-8 -*-

from os import path
from threading import Thread
from types import FunctionType
import locale
import mimetypes
from serviceconnector import *

class StreamPromise(ConnectionErrorChecker):
    __workerThread = None
    __handlerThread = None
    __serviceConnector = None
    __serviceResponse = None
    __onOkHandler = None
    __onErrorHandler = None
    __handlersRaised = False

    def __init__(self, serviceConnector, uri, data):
        if not isinstance(serviceConnector, ServiceConnector):
            raise TypeError('"serviceConnector" parameter should be of type ServiceConnector')

        self.__serviceConnector = serviceConnector

        self.__workerThread = Thread(target=self.__workerTarget, args=(uri, data))
        self.__handlerThread = Thread(target=self.__responseCheckTarget)
        self.__workerThread.start()
        self.__handlerThread.start()

    def __workerTarget(self, uri, dataDict):
        dataToSend = None
        headersToSend = None

        if not 'message' in dataDict and not 'file' in dataDict:
            raise TypeError('parameter should contain "message" or "file" field')

        if 'message' in dataDict and not isinstance(dataDict['message'], str) and not isinstance(dataDict['message'], bytes):
            raise TypeError('"message" field should be of type "string" or "bytes"')

        if 'message' in dataDict and isinstance(dataDict['message'], str) and not 'charset' in dataDict:
            raise TypeError('parameter should contain "charset" field in case if "message" is string')

        if 'headers' in dataDict and dataDict['headers'] and not isinstance(dataDict['headers'], dict):
            raise TypeError('"headers" field should be of type dict')

        """
        " In case if message is of bytes type it would not be rewriten by next if block
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

        if not 'file' in dataDict:
            self.__serviceResponse = self.__serviceConnector.request(
                'POST', uri, dataToSend, headersToSend )
        else:
            filepath, filename = path.split(dataDict['file'])
            filemime, fileenc = mimetypes.guess_type(dataDict['file'], False)

            if 'mimetype' in dataDict and not None == dataDict['mimetype']:
                filemime = dataDict['mimetype']

            fields = {
                'file': (
                    filename,
                    open(dataDict['file']).read(),
                    filemime
                )
            }

            self.__serviceResponse = self.__serviceConnector.send(
                uri, fields, headersToSend )

    def __responseCheckTarget(self):
        self.__workerThread.join()

        if self.__serviceResponse and self.__onOkHandler and self.__onErrorHandler:
            try:
                self.__onOkHandler(
                    self.checkResponseErrors(self.__serviceResponse)
                )
            except NoFoundException:
                self.__onErrorHandler(self.__serviceResponse)
            finally:
                self.__onOkHandler = self.__onErrorHandler = None

    def onResponse(self, ok, error=None):
        if not isinstance(ok, FunctionType) or (not None == error and not isinstance(error, FunctionType)):
            raise TypeError('parameters should be functions')

        self.__onOkHandler = ok
        if None == error:
            self.__onErrorHandler = ok
        else:
            self.__onErrorHandler = error

        self.__responseCheckTarget()

class StreamWriter:
    __serviceConnector = None
    __serviceUri = None

    def __init__(self, serviceConnector, uri):
        if not isinstance(serviceConnector, ServiceConnector):
            raise TypeError('parameter should be of type ServiceConnector')

        self.__serviceConnector = serviceConnector
        self.__serviceUri = uri

    def write(self, message, charset=None, headers=None):
        dataForPromise = {
            'message': message,
            'charset': charset,
            'headers': headers
        }

        return StreamPromise(self.__serviceConnector, self.__serviceUri, dataForPromise)

    def send(self, file, mimetype=None):
        dataForPromise = {
            'file': file,
            'mimetype': mimetype
        }

        return StreamPromise(self.__serviceConnector, self.__serviceUri, dataForPromise)
