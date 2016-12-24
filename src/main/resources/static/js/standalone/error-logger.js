(function () {
    'use strict';

    initConsoleSpy();

    function sendError(url, message, stack, cause, consoleHistory) {

        var xmlhttp = new XMLHttpRequest();

        xmlhttp.open(
            "POST",
            "/jsfatal",
            true);

        xmlhttp.setRequestHeader("Content-type", "application/json;charset=UTF-8");

        xmlhttp.onreadystatechange = function() {
            // Maybe this will dissuade Googlebot from rewriting to GET...
            if (xmlhttp.readyState == XMLHttpRequest.DONE && xmlhttp.status != 200) {
                sendErrorGet(url, message);
            }
        };

        xmlhttp.send(
            JSON.stringify({
                errorUrl: url || "",
                errorMessage: message || "",
                stackTrace: stack || null,
                cause: cause || "",
                console: consoleHistory || null
            })
        );
    }

    function sendErrorGet(url, message) {

        var xmlhttp = new XMLHttpRequest();

        xmlhttp.open(
            "GET",
            "/jsfatalGet?errorUrl=" + encodeURIComponent(url) + "&errorMessage=" + encodeURIComponent(message),
            true);

        xmlhttp.send();
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
                error = error || undefined;
                sendError(url, errorMsg, null, JSON.stringify(error), buffer);
                buffer = [];
            }
            catch (e) {
                console.log(e);
            }
        };
    }

})();