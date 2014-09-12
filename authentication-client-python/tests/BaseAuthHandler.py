import httplib
from BaseHTTPServer import BaseHTTPRequestHandler


class BaseAuthHandler (BaseHTTPRequestHandler):
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
        status_code = httplib.UNAUTHORIZED
        auth_uri = '{"auth_uri":["http://%s:%d/token"]}' % ( self.AUTH_HOST, self.AUTH_PORT)
        self.send_response(status_code)
        self.send_header("Content-type", "application/json")
        self.end_headers()
        self.wfile.write(auth_uri)

    pass

