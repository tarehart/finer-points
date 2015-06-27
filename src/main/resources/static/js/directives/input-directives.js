(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .directive('autoFocus', ['$timeout', AutoFocus])
        .directive('enterFn', [EnterFn]);

    function AutoFocus($timeout) {
        return {
            restrict: 'AC',
            link: function(_scope, _element) {
                $timeout(function(){
                    _element[0].focus();
                }, 0);
            }
        };
    };

    function EnterFn() {
        return {
            restrict: 'AC',
            link: function (scope, element, attrs) {
                element.bind("keydown keypress", function (event) {
                    if(event.which === 13) {
                        scope.$apply(function (){
                            scope.$eval(attrs.enterFn);
                        });

                        event.preventDefault();
                    }
                });
            }
        };
    }

})();