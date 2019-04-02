# Copyright (c) The PyAMF Project.
# See LICENSE.txt for details.

"""
AMF3 RemoteObject support.

@see: U{RemoteObject on Adobe Help (external)
    <http://help.adobe.com/en_US/FlashPlatform/reference/actionscript/3/mx/rpc/
    remoting/RemoteObject.html>}

@since: 0.1
"""

import calendar
import time
import uuid
import sys

import pyamf.python
from pyamf import remoting
from pyamf.flex import messaging


class BaseServerError(pyamf.BaseError):
    """
    Base server error.
    """


class ServerCallFailed(BaseServerError):
    """
    A catchall error.
    """
    _amf_code = 'Server.Call.Failed'


def generate_random_id():
    return str(uuid.uuid4())


def generate_acknowledgement(request=None):
    ack = messaging.AcknowledgeMessage()

    ack.messageId = generate_random_id()
    ack.clientId = generate_random_id()
    ack.timestamp = calendar.timegm(time.gmtime())

    if request:
        ack.correlationId = request.messageId

    return ack


def generate_error(request, cls, e, tb, include_traceback=False):
    """
    Builds an L{ErrorMessage<pyamf.flex.messaging.ErrorMessage>} based on the
    last traceback and the request that was sent.
    """
    import traceback

    if hasattr(cls, '_amf_code'):
        code = cls._amf_code
    else:
        code = cls.__name__

    details = None
    rootCause = e

    if include_traceback:
        buffer = pyamf.util.BufferedByteStream()

        traceback.print_exception(cls, e, tb, file=buffer)

        details = buffer.getvalue()

    faultDetail = None
    faultString = None

    if hasattr(e, 'message'):
        faultString = unicode(e.message)
    elif hasattr(e, 'args') and e.args:
        if isinstance(e.args[0], pyamf.python.str_types):
            faultString = unicode(e.args[0])

    if details:
        faultDetail = unicode(details)

    return messaging.ErrorMessage(
        messageId=generate_random_id(),
        clientId=generate_random_id(),
        timestamp=calendar.timegm(time.gmtime()),
        correlationId=request.messageId,
        faultCode=code,
        faultString=faultString,
        faultDetail=faultDetail,
        extendedData=details,
        rootCause=rootCause)


class RequestProcessor(object):
    def __init__(self, gateway):
        self.gateway = gateway

    @property
    def logger(self):
        if not self.gateway.logger:
            return None

        return self.gateway.logger

    def buildErrorResponse(self, request, error=None):
        """
        Builds an error response.

        @param request: The AMF request
        @type request: L{Request<pyamf.remoting.Request>}
        @return: The AMF response
        @rtype: L{Response<pyamf.remoting.Response>}
        """
        if error is not None:
            cls, e, tb = error
        else:
            cls, e, tb = sys.exc_info()

        return generate_error(request, cls, e, tb, self.gateway.debug)

    def _getBody(self, amf_request, ro_request, **kwargs):
        """
        @raise ServerCallFailed: Unknown request.
        """
        if isinstance(ro_request, messaging.CommandMessage):
            return self._processCommandMessage(
                amf_request,
                ro_request,
                **kwargs
            )
        elif isinstance(ro_request, messaging.RemotingMessage):
            return self._processRemotingMessage(
                amf_request,
                ro_request,
                **kwargs
            )
        elif isinstance(ro_request, messaging.AsyncMessage):
            return self._processAsyncMessage(
                amf_request,
                ro_request,
                **kwargs
            )
        else:
            raise ServerCallFailed("Unknown request: %s" % ro_request)

    def _processCommandMessage(self, amf_request, ro_request, **kwargs):
        """
        @raise ServerCallFailed: Unknown Command operation.
        @raise ServerCallFailed: Authorization is not supported in
            RemoteObject.
        """
        ro_response = generate_acknowledgement(ro_request)
        operation = ro_request.operation

        if operation == messaging.CommandMessage.PING_OPERATION:
            ro_response.body = True

            return remoting.Response(ro_response)
        elif operation == messaging.CommandMessage.LOGIN_OPERATION:
            raise ServerCallFailed(
                "Authorization is not supported in RemoteObject"
            )
        elif operation == messaging.CommandMessage.DISCONNECT_OPERATION:
            return remoting.Response(ro_response)
        else:
            raise ServerCallFailed("Unknown Command operation %s" % (
                operation,
            ))

    def _processAsyncMessage(self, amf_request, ro_request, **kwargs):
        ro_response = generate_acknowledgement(ro_request)
        ro_response.body = True

        return remoting.Response(ro_response)

    def _processRemotingMessage(self, amf_request, ro_request, **kwargs):
        ro_response = generate_acknowledgement(ro_request)

        service_name = get_service_name(ro_request)
        service_request = self.gateway.getServiceRequest(
            amf_request,
            service_name
        )

        # fire the preprocessor (if there is one)
        self.gateway.preprocessRequest(
            service_request,
            *ro_request.body,
            **kwargs
        )

        ro_response.body = self.gateway.callServiceRequest(
            service_request,
            *ro_request.body,
            **kwargs
        )

        return remoting.Response(ro_response)

    def __call__(self, amf_request, **kwargs):
        """
        Processes an AMF3 Remote Object request.

        @param amf_request: The request to be processed.
        @type amf_request: L{Request<pyamf.remoting.Request>}

        @return: The response to the request.
        @rtype: L{Response<pyamf.remoting.Response>}
        """
        ro_request = amf_request.body[0]

        try:
            body = self._getBody(amf_request, ro_request, **kwargs)
        except (KeyboardInterrupt, SystemExit):
            raise
        except:
            fault = self.buildErrorResponse(ro_request)

            if hasattr(self.gateway, 'onServiceError'):
                self.gateway.onServiceError(ro_request, fault)
            elif self.logger:
                self.logger.exception(
                    'Unexpected error while processing request %r',
                    get_service_name(ro_request)
                )

            body = remoting.Response(fault, status=remoting.STATUS_ERROR)

        ro_response = body.body

        dsid = ro_request.headers.get('DSId', None)

        if not dsid or dsid == 'nil':
            dsid = generate_random_id()

        ro_response.headers.setdefault('DSId', dsid)

        return body


def get_service_name(ro_request):
    """
    Returns the full service name of a RemoteObject request.
    """
    service_name = ro_request.operation

    if hasattr(ro_request, 'destination') and ro_request.destination:
        service_name = '%s.%s' % (ro_request.destination, service_name)

    return service_name
