require('../services/body-text-service');

(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .provider('markdownConverter', markdownConverter)
        .directive("markdownEditor", markdownEditor)
        .directive("bindHtmlCompile", bindHtmlCompile)
        .directive("nodeLink", nodeLink)
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

    function markdownEditor($timeout, BodyTextService) {
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
                            title: 'Attach Card',
                            btnText: 'Attach Card',
                            btnClass: 'btn btn-primary btn-sm md-accent',
                            icon: { glyph: 'glyphicon glyphicon-link', fa: 'fa fa-link', 'fa-3': 'icon-link' },
                            callback: function(e){
                                var selection = e.getSelection();

                                // The performReplace function will be called much later after some external
                                // code decides what node should be inserted.
                                function performReplace(nodeId, nodeTitle, idToReplace) {

                                    if (idToReplace) {
                                        var linkCode = BodyTextService.buildLinkCode(nodeId, nodeTitle);
                                        var regex = new RegExp("\\{\\{\\[" + idToReplace + "\\].+?(?=}})\\}\\}");
                                        e.setContent(e.getContent().replace(regex, linkCode));
                                    }
                                    else {
                                        var tagText = nodeTitle;
                                        e.setSelection(e.getContent().length, e.getContent().length); // Put cursor at end
                                        e.replaceSelection(BodyTextService.buildLinkCode(nodeId, tagText));
                                        var offset = ("" + nodeId).length + 4;
                                        e.setSelection(selection.start + offset, selection.start + offset + tagText.length);
                                    }

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

                scope.$on('$destroy', function() {
                    $(element).parent().remove();
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
            template: '<div bind-html-compile="html"></div>',
            link: function (scope, element, attrs, ngModel) {
                scope.$watch('ngMarkdown', function (v) {
                    if (!v) {
                        v = "";
                    }

                    var rawHtml = markdownConverter.makeHtml(v);
                    scope.html = rawHtml;

                }, true);
            }
        }
    }

    function bindHtmlCompile($compile) {
        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                scope.$watch(function () {
                    return scope.$eval(attrs.bindHtmlCompile);
                }, function (value) {
                    // In case value is a TrustedValueHolderType, sometimes it
                    // needs to be explicitly called into a string in order to
                    // get the HTML string.
                    element.html(value && value.toString());
                    // If scope is provided use it, otherwise use parent scope
                    var compileScope = scope;
                    if (attrs.bindHtmlScope) {
                        compileScope = scope.$eval(attrs.bindHtmlScope);
                    }
                    $compile(element.contents())(compileScope);
                });
            }
        };
    }

    function nodeLink() {
        return {
            restrict: 'A',
            link: function (scope, element, attrs) {
                element.on('click', function(event) {
                    scope.$emit("nodeLinkClick", attrs.nodeLink);
                    event.stopPropagation();
                    scope.$apply();
                });
            }
        };
    }


    // This used to live in js/local/showdown-node-stand.js
    var markdownExtension = function(a) {
        return [{
            type: "lang",
            regex: /{{\[([0-9a-z]{1,25})\](.+?)(?=}})}}/g,
            replace: function(a, b, c) {
                return '<span class="node-link" node-link="' + b + '">' + c + '</span>';
            }
        }]
    };
    typeof window != "undefined" && window.showdown && window.showdown.extension && (window.showdown.extension('finer-points-markdown', markdownExtension))


})();