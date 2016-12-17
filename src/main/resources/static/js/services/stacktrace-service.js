(function () {
    'use strict';

    angular
        .module('jsFatals')
        .service('StacktraceService', StacktraceService);

    function StacktraceService() {
        // Global object provided by Stacktrace.js
        this.print = function (exception, callback) {
            if (window.StackTrace) {
                window.StackTrace.fromError(exception).then(callback);
            } else {
                callback(null);
            }
        }
    }
})();