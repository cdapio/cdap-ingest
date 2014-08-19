#! /usr/bin/env python3
# -*- coding: utf-8 -*-

from threading import Thread, Condition
from types import FunctionType
from serviceconnector import *

class Promise(ConnectionErrorChecker):
    __workerThread = None
    __handlerThread = None
    __serviceConnector = None
    __serviceResponse = None

    def __init__(self, serviceConnector):
        if not isinstance(serviceConnector, ServiceConnector):
            raise TypeError('"serviceConnector" parameter should be of type ServiceConnector')

        self.__serviceConnector = serviceConnector

    def __workerTarget(self, dataDict):
        dictKeys = dataDict.keys()
        dataToSend = None
        
        if not 'message' in dictKeys and not 'file' in dictKeys:
            raise TypeError('parameter should contain "message" or "file" field')

        if 'headers' in dictKeys and not isinstance(dataDict['headers'], dict):
            raise TypeError('"headers" field should be of type dict')

        

        self.__serviceResponse = self.__serviceConnector.request('POST', dataToTransmit)

    
        

class StreamWriter:
    __serviceConnector = None
    
    def __init__(self, serviceConnector):
        if not isinstance(serviceConnector, ServiceConnector):
            raise TypeError('parameter should be of type ServiceConnector')

        self.__serviceConnector = serviceConnector

    
