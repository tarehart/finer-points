require('./stacktrace-service');

(function () {
    'use strict';

    angular
        .module('jsFatals')
        .service('ErrorLogService', ErrorLogService);

    // Largely following this: https://www.bennadel.com/blog/2542-logging-client-side-errors-with-angularjs-and-stacktrace-js.htm
    function ErrorLogService($log, $window, StacktraceService) {
        this.log = function(exception, cause) {
            try {
                var errorMessage = exception.toString();
                StacktraceService.print(exception, function(stack) {
                    sendError($window.location.href, errorMessage, stack, cause);
                });

            } catch (loggingError) {
                // For Developers - log the log-failure.
                $log.warn("Error logging failed");
                $log.log(loggingError);
            }
        };
    }

    function sendError(url, message, stack, cause) {
        $.ajax({
            type: "POST",
            url: "/jsfatal",
            contentType: "application/json",
            data: JSON.stringify({
                errorUrl: url,
                errorMessage: message,
                stackTrace: stack,
                cause: ( cause || "" )
            })
        });
    }

    window.onerror = function(errorMsg, url) {
        sendError(url, errorMsg, null, null);
    }

})();