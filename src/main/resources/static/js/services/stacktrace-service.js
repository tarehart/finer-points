(function () {
    'use strict';

    angular
        .module('jsFatals')
        .service('StacktraceService', StacktraceService);

    function StacktraceService() {
        // Global object provided by Stacktrace.js
        this.print = function (exception, callback) {
            window.StackTrace.fromError(exception).then(callback);
        }
    }
})();