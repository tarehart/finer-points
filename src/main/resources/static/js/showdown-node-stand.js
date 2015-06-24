(function() {
    'use strict';

    var a = function(a) {
        return [{
            type: "lang",
            regex: /{{\[([0-9]+)\](.+?)(?=}})}}/g,
            replace: function(a, b, c) {
                return '<span class="node-link"><span class="node-id">' + b + '</span>' + c + '</span>';
            }
        }]
    };
    typeof window != "undefined" && window.showdown && window.showdown.extension && (window.showdown.extension('nodestand', a))
})();