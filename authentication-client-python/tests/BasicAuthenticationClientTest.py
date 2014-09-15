import SocketServer
import socket
import threading
try:
    import unittest2 as unittest
except ImportError:
    import unittest as unittest

from BasicAuthenticationClient import BasicAuthenticationClient
from tests.AuthHandler import AuthenticationHandler
import TestConstants

class BasicAuthenticationClientTest(unittest.TestCase):

    def setUp(self):

        self.__authentication_client = BasicAuthenticationClient()
        self.__local_test_server = MyTCPServer((u"localhost", TestConstants.SERVER_PORT), AuthenticationHandler)
        self.__local_test_server.allow_reuse_address = True
        self.__server_thread = threading.Thread(target=self.__local_test_server.serve_forever)
        self.__server_thread.start()
        self.__authentication_client .set_connection_info(u'localhost', TestConstants.SERVER_PORT, False)
        AuthenticationHandler.AUTH_HOST =u'localhost'
        AuthenticationHandler.AUTH_PORT = TestConstants.SERVER_PORT
        # BaseHandler.auth_host = self.SERVER_PORT

    def tearDown(self):
        self.__local_test_server.server_close()
        self.__local_test_server.shutdown()


    def test_auth_is_auth_enabled(self):
        self.__authentication_client.configure(u"auth_config.json")
        assert(self.__authentication_client.is_auth_enabled())

    def test_success_get_accessToken(self):

        self.__authentication_client.configure(u"auth_config.json")
        access_token = self.__authentication_client.get_access_token()
        assert access_token
        self.assertEqual(TestConstants.TOKEN, access_token.value)
        self.assertEqual(TestConstants.TOKEN_TYPE, access_token.token_type)
        self.assertEqual(TestConstants.TOKEN_LIFE_TIME, access_token.expires_in)
        self.__local_test_server.server_close()

class MyTCPServer(SocketServer.TCPServer):
    def server_bind(self):
        self.socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.socket.bind(self.server_address)


