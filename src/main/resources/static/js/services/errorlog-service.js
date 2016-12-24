require('./stacktrace-service');

(function () {
    'use strict';

    angular
        .module('jsFatals')
        .service('ErrorLogService', ErrorLogService);

    // Largely following this: https://www.bennadel.com/blog/2542-logging-client-side-errors-with-angularjs-and-stacktrace-js.htm
    function ErrorLogService($log, $window, StacktraceService) {

        var buffer = [];

        // This will be called when an error occurs inside angular execution
        this.log = function(exception, cause) {
            try {
                var errorMessage = exception.toString();
                StacktraceService.print(exception, function(stack) {
                    sendError($window.location.href, errorMessage, stack, cause, buffer);
                    buffer = [];
                });

            }
            catch (loggingError) {

                try {
                    // For Developers - log the log-failure.
                    $log.warn("Error logging failed");
                    $log.log(loggingError);
                    sendError(null, loggingError, null, null, buffer);
                    buffer = [];
                }
                catch (secondError) {
                    // Just eat it.
                }
            }
        };

        initConsoleSpy();

        // This will be called if there's an uncaught error outside angular.
        window.onerror = function(errorMsg, url, line, col, error) {
            try {
                sendError(url, errorMsg, null, JSON.stringify(error), buffer);
                buffer = [];
            }
            catch (e) {
                $log.log(e);
            }
        };

        function sendError(url, message, stack, cause, console) {
            $.ajax({
                type: "POST",
                url: "/jsfatal",
                contentType: "application/json",
                data: JSON.stringify({
                    errorUrl: url || "",
                    errorMessage: message || "",
                    stackTrace: stack || null,
                    cause: cause || "",
                    console: console || null
                })
            });
        }

        function initConsoleSpy() {

            if (typeof console  != "undefined") {
                if (typeof console.log != 'undefined') {
                    console.olog = console.olog || console.log;
                }
                else {
                    console.olog = function () {};
                }
            }

            console.log = function(message) {
                console.olog(message);
                buffer.push(message);
            };

            console.error = console.debug = console.info =  console.log;
        }
    }

})();