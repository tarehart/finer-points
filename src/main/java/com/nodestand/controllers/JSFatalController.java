package com.nodestand.controllers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class JSFatalController {

    private final Log logger = LogFactory.getLog(getClass());

    @RequestMapping(value = "/jsfatal", method = RequestMethod.POST)
    public void handleJsFatal(@RequestBody JsFatalInput jsFatal) {
        logJsFatal(jsFatal);
    }

    /**
     * The POST method is preferred, but sometimes Googlebot rewrites to GET, and I don't want to drop that on the floor.
     */
    @RequestMapping(value = "/jsfatalGet", method = RequestMethod.GET)
    public void handleJsFatalGet(@RequestParam String errorUrl, @RequestParam String errorMessage) {
        JsFatalInput jsFatal = new JsFatalInput();
        jsFatal.errorUrl = errorUrl;
        jsFatal.errorMessage = errorMessage;
        logJsFatal(jsFatal);
    }

    private void logJsFatal(JsFatalInput jsFatal) {

        String message = String.format("JavaScriptException: \"%s\" URL: %s", jsFatal.errorMessage, jsFatal.errorUrl);

        if (jsFatal.stackTrace != null) {
            List<String> stackList = jsFatal.stackTrace.stream().map(JsStackFrame::toString).collect(Collectors.toList());
            message += "\n" + String.join("\n", stackList);
        }

        if (jsFatal.cause != null && !jsFatal.cause.isEmpty()) {
            message += "\nCause: " + jsFatal.cause;
        }

        if (jsFatal.console != null && jsFatal.console.size() > 0) {
            message += "\nConsole:\n" + String.join("\n", jsFatal.console);
        }

        logger.error(message);
    }

    public static class JsFatalInput {
        public JsFatalInput() {}
        public String cause;
        public String errorMessage;
        public String errorUrl;
        public List<JsStackFrame> stackTrace;
        public List<String> console;
    }

    public static class JsStackFrame {
        public JsStackFrame() {}
        public String functionName;
        public String fileName;
        public int lineNumber;
        public int columnNumber;
        public String toString() {
            return String.format("\tat %s:%s:%s %s", functionName, lineNumber, columnNumber, fileName);
        }
    }
}
