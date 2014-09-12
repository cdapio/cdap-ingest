from BaseHTTPServer import BaseHTTPRequestHandler
import base64
import httplib
import BasicAuthenticationClientTest as bact


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
        request_ulr= self.requestline['/token']
        if not request_ulr :
            status_code = httplib.UNAUTHORIZED
            auth_uri = '{"auth_uri":["http://%s:%d/token"]}' % ( self.AUTH_HOST, self.AUTH_PORT)
            self.send_response(status_code)
            self.send_header("Content-type", "application/json")
            self.end_headers()
            self.wfile.write(auth_uri)
        else:
            request_counter = 0
            auth_header_val = self.headers['Authorization']
            if auth_header_val:
                auth_header_val.replace('Basic ','')
                credentials_str = base64.b64decode(auth_header_val)
                credentials = credentials_str.split(':', 1)
                username = credentials[0]
                password = credentials[1]
                if bact.USERNAME == username and bact.PASSWORD == password:
                    status_code = httplib.OK
                    self.wfile.write(self.create_resp_body(bact.TOKEN, bact.TOKEN_TYPE, bact.TOKEN_LIFE_TIME))
                elif bact.EMPTY_TOKEN_USERNAME == username:
                    self.wfile.write(self.create_resp_body('', bact.TOKEN_TYPE, bact.TOKEN_LIFE_TIME))
                    status_code = httplib.OK
                elif bact.EXPIRED_TOKEN_USERNAME == username:
                    if self.request_counter == 1:
                        resp = self.create_resp_body(bact.TOKEN, bact.TOKEN_TYPE, 5)
                    else:
                        resp = self.create_resp_body(bact.NEW_TOKEN, bact.TOKEN_TYPE, bact.TOKEN_LIFE_TIME)
                    self.wfile.write(resp)
                    status_code = httplib.OK
                else:
                    status_code = httplib.UNAUTHORIZED
            else:
                status_code = httplib.BAD_REQUEST

            self.send_response(status_code)
            self.send_header("Content-type", "application/json")
            self.end_headers()

    @staticmethod
    def create_resp_body(value, type, expires_in):
        return "{'access_token':'" + value + "','token_type':'" + type + "','expires_in':" + expires_in + "}";



