import httplib
import logging


LOG = logging.getLogger(__name__)


class RestClientUtils:

    @staticmethod
    def verify_response_code(response_code):
        RestClientUtils.check_status(response_code)

    @staticmethod
    def check_status(status):
        return {
            httplib.OK: LOG.debug("Success operation result code."),
            httplib.NOT_FOUND: RestClientUtils.raise_not_found_error(status),
            httplib.BAD_REQUEST: RestClientUtils.raise_base_request_eror(status),
            httplib.CONFLICT: RestClientUtils.raise_conflict_error(status),
            httplib.UNAUTHORIZED: RestClientUtils.raise_unauthorized_error(status),
            httplib.FORBIDDEN: RestClientUtils.raise_forbidden_error(status),
            httplib.METHOD_NOT_ALLOWED: RestClientUtils.raise_method_not_allowed(status),
            httplib.INTERNAL_SERVER_ERROR: RestClientUtils.raise_internal_server_error(status)
        }.get(status, RestClientUtils.raise_not_supported_error(status))

    @staticmethod
    def raise_not_found_error(status):
        return NotFoundError(status, u'Not found HTTP code was received from gateway server.')

    @staticmethod
    def raise_base_request_eror(status):
        return BadRequestError(status, u'Bad request HTTP code was received from gateway server.')

    @staticmethod
    def raise_conflict_error(status):
        return ConflictError(status, u'Conflict HTTP code was received from gateway server.')

    @staticmethod
    def raise_unauthorized_error(status):
        return UnauthorizedError(status, u'Authorization error code was received from server. ')

    @staticmethod
    def raise_forbidden_error(status):
        return ForbiddenError(status, u'Forbidden HTTP code was received from gateway server')

    @staticmethod
    def raise_method_not_allowed(status):
        return MethodNotAllowed(status, u'Method not allowed code was received from gateway server')

    @staticmethod
    def raise_internal_server_error(status):
        return InternalServerError(status, u'Internal server exception during operation process. ')

    @staticmethod
    def raise_not_supported_error(status):
        return NotSupportedError(status, u'Operation is not supported by gateway server')


class BaseHttpError(Exception):
    def __init__(self, code, msg):

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

