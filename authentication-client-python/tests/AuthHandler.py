from BaseHTTPServer import BaseHTTPRequestHandler
import base64
import httplib
import json
import TestConstants


class AuthenticationHandler(BaseHTTPRequestHandler):
    AUTH_PORT = None
    AUTH_HOST = None

    @property
    def auth_host(self):
        return self.AUTH_HOST

    @auth_host.setter
    def auth_host(self, auth_host):
        self.AUTH_HOST = auth_host

    @property
    def auth_port(self):
        return self.AUTH_PORT

    @auth_host.setter
    def auth_port(self, auth_port):
        self.AUTH_PORT = auth_port

    def do_GET(self):
        if 'token' not  in self.path:
            status_code = httplib.UNAUTHORIZED
            auth_uri = u'{"auth_uri":["http://%s:%d/token"]}' % ( self.AUTH_HOST, self.AUTH_PORT)
            self.send_response(status_code)
            self.send_header(u"Content-type", u"application/json")
            self.end_headers()
            self.wfile.write(auth_uri)

        else:
            self.__request_counter = 0
            auth_header_val = self.headers[u'Authorization']
            if auth_header_val:
                auth_header_val = auth_header_val.replace(u'Basic ', '')
                credentials_str = base64.b64decode(auth_header_val)
                credentials = credentials_str.split(':', 1)
                username = credentials[0]
                password = credentials[1]
                if TestConstants.USERNAME == username and TestConstants.PASSWORD == password:
                    status_code = httplib.OK
                    self.send_response(status_code)
                    self.send_header(u"Content-type", u"application/json")
                    self.end_headers()
                    self.wfile.write(self.create_resp_body(TestConstants.TOKEN, TestConstants.TOKEN_TYPE, TestConstants.TOKEN_LIFE_TIME))
                elif TestConstants.EMPTY_TOKEN_USERNAME == username:
                    status_code = httplib.OK
                    self.send_response(status_code)
                    self.send_header(u"Content-type", u"application/json")
                    self.end_headers()
                    self.wfile.write(self.create_resp_body('', TestConstants.TOKEN_TYPE, TestConstants.TOKEN_LIFE_TIME))

                elif TestConstants.EXPIRED_TOKEN_USERNAME == username:
                    if self.request_counter == 1:
                        resp = self.create_resp_body(TestConstants.TOKEN, TestConstants.TOKEN_TYPE, 5)
                    else:
                        resp = self.create_resp_body(TestConstants.NEW_TOKEN, TestConstants.TOKEN_TYPE, TestConstants.TOKEN_LIFE_TIME)

                    status_code = httplib.OK
                    self.send_response(status_code)
                    self.send_header(u"Content-type", u"application/json")
                    self.end_headers()
                    self.wfile.write(resp)

                else:
                    status_code = httplib.UNAUTHORIZED
                    self.send_response(status_code)
                    self.send_header(u"Content-type", u"application/json")
                    self.end_headers()
            else:
                status_code = httplib.BAD_REQUEST
                self.send_response(status_code)
                self.send_header(u"Content-type", u"application/json")
                self.end_headers()


    @staticmethod
    def create_resp_body(value, type, expires_in):
        return json.dumps({u'access_token': value, u'token_type': type, u'expires_in': expires_in});



