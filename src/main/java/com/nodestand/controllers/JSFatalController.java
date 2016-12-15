package com.nodestand.controllers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class JSFatalController {

    private final Log logger = LogFactory.getLog(getClass());

    @RequestMapping("/jsfatal")
    public void handleJsFatal(@RequestBody JsFatalInput jsFatal) {

        String message = String.format("JavaScriptException: \"%s\" URL: %s", jsFatal.errorMessage, jsFatal.errorUrl);

        if (jsFatal.stackTrace != null) {
            List<String> stackList = jsFatal.stackTrace.stream().map(JsStackFrame::toString).collect(Collectors.toList());
            message += "\n" + String.join("\n", stackList);
        }

        logger.error(message);
    }

    public static class JsFatalInput {
        public JsFatalInput() {}
        public String cause;
        public String errorMessage;
        public String errorUrl;
        public List<JsStackFrame> stackTrace;
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
