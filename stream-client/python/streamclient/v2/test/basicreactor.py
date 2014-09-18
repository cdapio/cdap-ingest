import requests

import os
import sys
import inspect
currentdir = os.path.dirname(
    os.path.abspath(inspect.getfile(inspect.currentframe())))
parentdir = os.path.dirname(currentdir)
sys.path.insert(0, parentdir)

from config import Config
from serviceconnector import NotFoundError
from streamwriter import StreamWriter
from streamclient import StreamClient
from caskauthclient.BasicAuthenticationClient import BasicAuthenticationClient
from caskauthclient.Config import Config as AuthConfig

# Should be used as parent class for integration tests.
# In children __host, __port, __ssl properties have to be set.

class BasicReactor(object):


    validStream = u'validStream'
    invalidStream = u'invalidStream'

    validFile = u'some.log'
    invalidFile = u'invalid.file'

    messageToWrite = u'some message'

    exit_code = 404

    @property
    def host(self):
        return self.__host

    @host.setter
    def host(self, host):
        self.__host = host

    @property
    def port(self):
        return self.__port

    @port.setter
    def port(self, port):
        self.__port = port

    @property
    def ssl(self):
        return self.__ssl

    @ssl.setter
    def ssl(self, ssl):
        self.__ssl = ssl

    def set_up(self):
        self.__BASE_URL = u'http://{0}:{1}/v2'.format(self.host, self.port)
        self.__REQUEST_PLACEHOLDERS = {
            u'streamid': u'<streamid>'
        }
        self.__REQUESTS = {u'base_stream_path': self.__BASE_URL + u'/streams'}
        self.__REQUESTS[u'stream'] = u'{0}/{1}'.format(
            self.__REQUESTS[u'base_stream_path'],
            self.__REQUEST_PLACEHOLDERS[u'streamid'])
        self.__REQUESTS[u'consumerid'] = u'{0}/{1}'.format(
            self.__REQUESTS[u'stream'], u'consumer-id')
        self.__REQUESTS[u'dequeue'] = u'{0}/{1}'.format(
            self.__REQUESTS[u'stream'], u'dequeue')
        self.__REQUESTS[u'config'] = u'{0}/{1}'.format(
            self.__REQUESTS[u'stream'], u'config')
        self.__REQUESTS[u'info'] = u'{0}/{1}'.format(
            self.__REQUESTS[u'stream'], u'info')
        self.__REQUESTS[u'truncate'] = u'{0}/{1}'.format(
            self.__REQUESTS[u'stream'], u'truncate')

        authConfig = AuthConfig().read_from_file(u'config.json')

        authClient = BasicAuthenticationClient()
        authClient.set_connection_info(self.host, self.port, self.ssl)
        authClient.configure(authConfig)

        config = Config(self.host, self.port, self.ssl)
        config.set_auth_client(authClient)

        self.sc = StreamClient(config)

    def test_reactor_successful_connection(self):
        try:
            self.sc.create(self.validStream)
        except:
            self.fail(u'Reactor connection failed')

    def test_reactor_failure_connection(self):
        url = self.__REQUESTS[u'stream'].replace(
            self.__REQUEST_PLACEHOLDERS[u'streamid'],
            self.validStream
        )

        url = url.replace(u'{0}'.format(self.port), u'0')

        self.assertRaises(
            Exception,
            requests.get,
            url
            )

    def test_create(self):
        url = self.__REQUESTS[u'stream'].replace(
            self.__REQUEST_PLACEHOLDERS[u'streamid'],
            self.validStream
        )

        response = requests.put(url)

        self.assertEqual(response.status_code, 200)

    def test_set_ttl_valid_stream(self):
        ttl = 88888

        try:
            self.sc.set_ttl(self.validStream, ttl)
        except NotFoundError:
            self.fail(u'StreamClient.set_ttl() failed')

    def test_set_ttl_invalid_stream(self):
        ttl = 88888

        self.assertRaises(
            NotFoundError,
            self.sc.set_ttl,
            self.invalidStream,
            ttl
        )

    def test_get_ttl_valid_stream(self):
        try:
            self.sc.get_ttl(self.validStream)
        except NotFoundError:
            self.fail(u'StreamClient.getTTL() failed')

    def test_get_ttl_invalid_stream(self):
        self.assertRaises(
            NotFoundError,
            self.sc.get_ttl,
            self.invalidStream
        )

    def test_create_writer_successful(self):
        self.assertIsInstance(
            self.sc.create_writer(self.validStream),
            StreamWriter
        )

    def test_create_writer_invalid_stream(self):

        self.assertRaises(
            NotFoundError,
            self.sc.create_writer,
            self.invalidStream)

    def test_stream_writer_successful_sending(self):
        sw = self.sc.create_writer(self.validStream)

        def on_response(response):
            self.exit_code = response.status_code

        q = sw.send(self.validFile)
        q.on_response(on_response)

        self.assertEqual(self.exit_code, 200)

    def test_stream_writer_successful_writing(self):
        sw = self.sc.create_writer(self.validStream)

        def on_response(response):
            self.exit_code = response.status_code

        q = sw.write(self.messageToWrite)
        q.on_response(on_response)

        self.assertEqual(self.exit_code, 200)
