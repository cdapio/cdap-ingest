import httplib
import logging


LOG = logging.getLogger(__name__)


class RestClientUtils:
    def verifyResponseCode(self, response):
        status = response.getcode()
        self.check_status(status)

    def check_status(self, status):
        return {
            httplib.OK: LOG.debug("Success operation result code."),
            httplib.NOT_FOUND: self.raise_not_found_error(status),
            httplib.BAD_REQUEST: self.raise_base_request_eror(status),
            httplib.CONFLICT: self.raise_conflict_error(status),
            httplib.UNAUTHORIZED: self.raise_unauthorized_error(status),
            httplib.FORBIDDEN: self.raise_forbidden_error(status),
            httplib.METHOD_NOT_ALLOWED: self.raise_method_not_allowed(status),
            httplib.INTERNAL_SERVER_ERROR: self.raise_internal_server_error(status)
        }[self.raise_not_supported_error(status)]

    def raise_not_found_error(self, status):
        return NotFoundError(status, 'Not found HTTP code was received from gateway server.')

    def raise_base_request_eror(self, status):
        return BadRequestError(status, 'Bad request HTTP code was received from gateway server.')

    def raise_conflict_error(self, status):
        return ConflictError(status, 'Conflict HTTP code was received from gateway server.')

    def raise_unauthorized_error(self, status):
        return UnauthorizedError(status, 'Authorization error code was received from server. ')

    def raise_forbidden_error(self, status):
        return ForbiddenError(status, 'Forbidden HTTP code was received from gateway server')

    def raise_method_not_allowed(self, status):
        return MethodNotAllowed(status, 'Method not allowed code was received from gateway server')

    def raise_internal_server_error(self, status):
        return InternalServerError(status, 'Internal server exception during operation process. ')

    def raise_not_supported_error(self, status):
        return NotSupportedError(status, 'Operation is not supported by gateway server')


class BaseHttpError(Exception):
    def __init__(self, code, msg):
        super(self.__class__, self).__init__()
        self.__errorCode = code
        self.__errorMsg = msg

    def code(self):
        return self.__errorCode

    def message(self):
        return self.__errorMsg

    def __str__(self):
        return u"Code: %s \nMessage: %s" % (self.__errorCode, self.__errorMsg)

class BadRequestError(BaseHttpError):
    pass

class NotFoundError(BaseHttpError):
    pass


class ConflictError(BadRequestError):
    pass


class UnauthorizedError(BadRequestError):
    pass


class ForbiddenError(BadRequestError):
    pass


class MethodNotAllowed(BadRequestError):
    pass


class InternalServerError(BadRequestError):
    pass


class NotSupportedError(BadRequestError):
    pass

