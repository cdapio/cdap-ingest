from BaseHTTPServer import BaseHTTPRequestHandler
import httplib


class AuthDisabledHandler(BaseHTTPRequestHandler):

    def do_GET(self):
        status_code = httplib.OK
        self.send_response(status_code)
        self.send_header("Content-type", "application/json")
        self.end_headers()
pass
