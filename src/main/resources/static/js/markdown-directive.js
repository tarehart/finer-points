(function() {
    'use strict';

    angular
        .module('markdown', ['ngSanitize'])
        .provider('markdownConverter', markdownConverter)
        .directive("markdownEditor", ['$sanitize', 'markdownConverter', markdownEditor])
        .directive("renderMarkdown", ['$sanitize', 'markdownConverter', renderMarkdown]);

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
                setText: "=",
                doneFn: "="
            },
            link:     function (scope, element, attrs, ngModel) {
                $(element).markdown({
                    savable:false,
                    onChange: function(e){
                        var text = e.getContent();
                        scope.setText(text);
                        scope.$apply();
                    },
                    hiddenButtons: ['Preview', 'Image', 'cmdUrl'],
                    fullscreen: {enable: false},
                    additionalButtons: [
                        {
                            name: 'groupUtil',
                            data: [{
                                name: 'done',
                                toggle: false,
                                hotkey: 'Ctrl+S',
                                title: 'Done',
                                btnText: 'Done',
                                btnClass: 'btn btn-success btn-sm',
                                icon: { glyph: 'glyphicon glyphicon-ok', fa: 'fa fa-check', 'fa-3': 'icon-search' },
                                callback: function(e){
                                    scope.doneFn(e);
                                }
                            }]
                        }
                    ]
                });
            }
        }
    }

    function renderMarkdown($sanitize, markdownConverter) {
        return {
            restrict: "A",
            scope: {
                ngMarkdown: "="
            },
            template: '<div ng-bind-html="html"></div>',
            link: function (scope, element, attrs, ngModel) {
                scope.$watch(function(scope) {return scope.ngMarkdown;}, function (v) {
                    if (!v) {
                        v = "";
                    }

                    var rawHtml = markdownConverter.makeHtml(v);
                    scope.html = rawHtml;

                }, true);
            }
        }
    }


})();