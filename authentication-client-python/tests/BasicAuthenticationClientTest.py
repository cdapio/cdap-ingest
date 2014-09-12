import SocketServer
import threading
import unittest
from BasicAuthenticationClient import BasicAuthenticationClient
import AuthenticationHandler
from tests import BaseAuthHandler



class BasicAuthenticationClientTest(unittest.TestCase):

    USERNAME = "admin"
    PASSWORD = "realtime"
    TOKEN = "SuccessGeneratedToken"
    NEW_TOKEN = "SuccessGeneratedSecondToken"
    TOKEN_TYPE = "Bearer"
    EMPTY_TOKEN_USERNAME = "emptyToken"
    EXPIRED_TOKEN_USERNAME = "expiredToken"
    TOKEN_LIFE_TIME = 86400
    USERNAME_PROP_NAME = "security.auth.client.username"
    PASSWORD_PROP_NAME = "security.auth.client.password"
    SERVER_PORT = 10002
    __authentication_client = None
    __local_test_server = None
    __server_thread = None

    def setUp(self):
        self.__authentication_client = BasicAuthenticationClient()
        self.__local_test_server = SocketServer.TCPServer(("localhost", self.SERVER_PORT), AuthenticationHandler)
        self.__server_thread = threading.Thread(target=self.__local_test_server.serve_forever)
        self.__server_thread.start()
        self.__authentication_client .set_connection_info('localhost', self.SERVER_PORT, False)
        BaseAuthHandler.AUTH_HOST ='localhost'
        BaseAuthHandler.AUTH_PORT = self.SERVER_PORT
        # BaseHandler.auth_host = self.SERVER_PORT

    def tearDown(self):
        self.__local_test_server.server_close()


    def test_auth_is_auth_enabled(self):
        self.__authentication_client.configure("auth_config.json")
        assert(self.__authentication_client.is_auth_enabled())

    def test_success_get_accessToken(self):

        # self.__local_test_server = SocketServer.TCPServer(("", self.SERVER_PORT), AuthenticationHandler)
        #
        # self.__server_thread = threading.Thread(target=self.__local_test_server.serve_forever)
        # self.__server_thread.start()
        self.__authentication_client.configure("auth_config.json")
        access_token = self.__authentication_client.get_access_token();
        assert access_token
        self.assertEqual(self.TOKEN, access_token.token)
        self.assertEqual(self.TOKEN_TYPE, access_token.token_type)
        self.assertEqual(self.TOKEN_LIFE_TIME, access_token.token_life_time)
        self.__local_test_server.server_close()




