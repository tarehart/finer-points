(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .directive('nodeType', nodeType);

    function nodeType() {
        return {
            restrict: "A",
            scope: {
                nodeType: "=",
                showTooltip: "@"
            },
            controller: function ($scope) {
                var iconMap = {
                    source: {icon: 'fa-book', tooltip: 'Source card'},
                    subject: {icon: 'fa-address-card-o', tooltip: 'Subject card'},
                    interpretation: {icon: 'fa-quote-left', tooltip: 'Interpretation card'},
                    assertion: {icon: 'fa-cube', tooltip: 'Opinion card'}
                };

                var config = iconMap[$scope.nodeType];
                $scope.iconName = config.icon;
                $scope.tooltip = config.tooltip;
            },
            template: '<i ng-class="[\'fa\', \'node-type-icon\', iconName]"><md-tooltip ng-if="showTooltip" md-direction="right">{{tooltip}}</md-tooltip></i>'
        }
    }

})();