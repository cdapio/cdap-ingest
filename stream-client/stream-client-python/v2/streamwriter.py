import locale
from serviceconnector import ServiceConnector, ConnectionErrorChecker
from streampromise import StreamPromise


class StreamWriter(object):

    def __init__(self, serviceConnector, uri):
        u"""
        Object constructor

        Keyword arguments:
        serviceConnector -- reference to connection pool to communicate
                            with gateway server.
        uri -- REST URL part to perform request.
               Example: '/v2/strems/myStream'
        data -- data to proceed by worker thread.  Please read
                '__workerTarget' documentation.
        """
        if not isinstance(serviceConnector, ServiceConnector):
            raise TypeError(u'parameter should be of type ServiceConnector')

        self.__serviceConnector = serviceConnector
        self.__serviceUri = uri

    def write(self, message, charset=None, headers=None):
        u"""
        Ingest a stream event with a string as body.

        Keyword arguments:
        message -- Data to transmit to REST server.
                   Could be of type None if file field is presented.
        charset -- Message field content charset. Could be of type None.
                   Default value: 'utf-8'
        headers -- Additional HTTP headers. Should be of type 'dict'.

        Returns:
        StreamPromise instance for further handling
        """
        promiseData = {
            u'message': message,
            u'charset': charset,
            u'headers': headers
        }

        return StreamPromise(self.__serviceConnector, self.__serviceUri,
                             promiseData)

    def send(self, file, mimetype=None):
        u"""
        Sends the content of a file as multiple stream events.

        Keyword arguments:
        file -- path to file to be sent to Gateway
        mimetype -- mimetype of a file. If is not defined will attempt
                    to detect mimetype automaticaly.

        Returns:
        StreamPromise instance for further handling
        """
        promiseData = {
            u'file': file,
            u'mimetype': mimetype
        }

        return StreamPromise(self.__serviceConnector, self.__serviceUri,
                             promiseData)
