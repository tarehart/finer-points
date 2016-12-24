(function () {
    'use strict';

    initConsoleSpy();

    function sendError(url, message, stack, cause, console) {

        var xmlhttp = new XMLHttpRequest();

        xmlhttp.open(
            "POST",
            "/jsfatal",
            true);

        xmlhttp.setRequestHeader("Content-type", "application/json;charset=UTF-8");

        xmlhttp.send(
            JSON.stringify({
                errorUrl: url || "",
                errorMessage: message || "",
                stackTrace: stack || null,
                cause: cause || "",
                console: console || null
            })
        );
    }

    function initConsoleSpy() {
        var buffer = [];

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

        window.onerror = function(errorMsg, url, line, col, error) {
            try {
                sendError(url, errorMsg, null, JSON.stringify(error), buffer);
                buffer = [];
            }
            catch (e) {
                console.log(e);
            }
        };
    }

})();