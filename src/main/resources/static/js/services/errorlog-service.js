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
                    $.ajax({
                        type: "POST",
                        url: "/jsfatal",
                        contentType: "application/json",
                        data: angular.toJson({
                            errorUrl: $window.location.href,
                            errorMessage: errorMessage,
                            stackTrace: stack,
                            cause: ( cause || "" )
                        })
                    });
                });

            } catch (loggingError) {
                // For Developers - log the log-failure.
                $log.warn("Error logging failed");
                $log.log(loggingError);
            }
        };
    }

})();