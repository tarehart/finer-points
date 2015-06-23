(function() {
    'use strict';

    angular
        .module('markdown', ['ngSanitize'])
        .provider('markdownConverter', markdownConverter)
        .directive("markdownEditor", ['$sanitize', 'markdownConverter', markdownEditor]);

    function markdownConverter() {
        var opts = {};
        return {
            config: function (newOpts) {
                opts = newOpts;
            },
            $get: function () {
                return new Showdown.converter(opts);
            }
        };
    }

    function markdownEditor($sanitize, markdownConverter) {
        return {
            restrict: "A",
            scope: {
                setText: "=setText",
                setHtml: "=setHtml"
            },
            link:     function (scope, element, attrs, ngModel) {
                $(element).markdown({
                    savable:false,
                    onChange: function(e){
                        var text = e.getContent();
                        scope.setText(text);
                        var rawHtml = markdownConverter.makeHtml(text);
                        var sanitized = $sanitize(rawHtml);
                        scope.setHtml(sanitized);
                        scope.$apply();
                    }
                });
            }
        }
    }


})();