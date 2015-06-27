(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .directive('autoFocus', ['$timeout', AutoFocus]);

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

})();