(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .provider('markdownConverter', markdownConverter)
        .directive("markdownEditor", markdownEditor)
        .directive("renderMarkdown", ['markdownConverter', renderMarkdown]);

    function markdownConverter() {
        return {
            $get: function () {
                var opts = {
                    extensions: ['finer-points-markdown']
                };
                return new showdown.Converter(opts);
            }
        };
    }

    function markdownEditor($timeout) {
        return {
            restrict: "A",
            scope: {
                node: "=",
                setText: "=",
                linkFn: "="
            },
            link:     function (scope, element, attrs, ngModel) {

                var additionalButtons = [];

                if (scope.linkFn) {
                    additionalButtons.push({
                        name: 'groupNode',
                        data: [{
                            name: 'nodelink',
                            toggle: false,
                            hotkey: 'Ctrl+K',
                            title: 'Link Node',
                            btnText: 'Link Node',
                            btnClass: 'btn btn-primary btn-sm',
                            icon: { glyph: 'glyphicon glyphicon-link', fa: 'fa fa-link', 'fa-3': 'icon-link' },
                            callback: function(e){
                                var selection = e.getSelection();

                                // The performReplace function will be called much later after some external
                                // code decides what node should be inserted.
                                function performReplace(nodeId, nodeTitle) {
                                    var tagText = selection.text || nodeTitle;
                                    e.replaceSelection("{{[" + nodeId + "]" + tagText + "}}");
                                    var offset = ("" + nodeId).length + 4;
                                    e.setSelection(selection.start + offset, selection.start + offset + tagText.length);
                                    scope.setText(scope.node, e.getContent());
                                }
                                scope.linkFn(scope.node, performReplace);
                            }
                        }]
                    });
                }

                $(element).markdown({
                    savable:false,
                    onChange: function(e){
                        $timeout(function() {
                            var text = e.getContent();
                            scope.setText(scope.node, text);
                        }, 0);
                    },
                    hiddenButtons: ['Preview', 'Image', 'cmdUrl'],
                    fullscreen: {enable: false},
                    iconlibrary: "fa", // Use font-awesome
                    additionalButtons: [additionalButtons]
                });
            }
        }
    }

    function renderMarkdown(markdownConverter) {
        return {
            restrict: "A",
            scope: {
                ngMarkdown: "="
            },
            // ng-bind-html sanitizes the html behind the scenes to prevent XSS.
            // https://docs.angularjs.org/api/ngSanitize/service/$sanitize
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

    // This used to live in js/local/showdown-node-stand.js
    var markdownExtension = function(a) {
        return [{
            type: "lang",
            regex: /{{\[([0-9a-z]{1,25})\](.+?)(?=}})}}/g,
            replace: function(a, b, c) {
                return '<span class="node-link"><span class="node-id">' + b + '</span>' + c + '</span>';
            }
        }]
    };
    typeof window != "undefined" && window.showdown && window.showdown.extension && (window.showdown.extension('finer-points-markdown', markdownExtension))


})();