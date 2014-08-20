#! /usr/bin/env python3
# -*- coding: utf-8 -*-

from configparser import ConfigParser

class Config:
    __host = 'localhost'
    __port = 10000
    __useSSL = False
    __apiKey = ''

    __sections = {
        'server': {
            'sectionName': 'ServerConnection',
            'options': {
                'host': 'hostname',
                'port': 'port',
                'ssl': 'SSL',
                'apikey': 'APIKey'
            }
        }
    }

    def __init__(self):
        pass

    def getHost(self):
        return self.__host

    def setHost(self, hostname):
        self.__host = hostname

    def getPort(self):
        return self.__port

    def setPort(self, port):
        self.__port = port

    def getSSL(self):
        return self.__useSSL

    def setSSL(self, ssl):
        self.__useSSL = ssl

    def getAPIKey(self):
        return self.__apiKey

    def setAPIKey(self, apiKey):
        self.__apiKey = apiKey

    def readFromFile(filename):
        newConfig = Config()
        configParser = ConfigParser()
        configParser.read(filename)

        try:
            host = configParser.get(
                Config.__sections['server']['sectionName'],
                Config.__sections['server']['options']['host']
            )
            newConfig.setHost(host)
        except:
            pass

        try:
            port = configParser.getint(
                Config.__sections['server']['sectionName'],
                Config.__sections['server']['options']['port']
            )
            newConfig.setPort(port)
        except:
            pass

        try:
            useSSL = configParser.getboolean(
                Config.__sections['server']['sectionName'],
                Config.__sections['server']['options']['ssl']
            )
            newConfig.setSSL(useSSL)
        except:
            pass

        try:
            apiKey = configParser.get(
                Config.__sections['server']['sectionName'],
                Config.__sections['server']['options']['apikey']
            )
            newConfig.setAPIKey(apiKey)
        except:
            pass

        return newConfig
