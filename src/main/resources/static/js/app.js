var nodeStandApp = angular.module('nodeStandApp', [
    'ngRoute',
    'nodeStandControllers'
]);

nodeStandApp.config(['$routeProvider',
    function($routeProvider) {
        $routeProvider.
            when('/graph', {
                templateUrl: 'partials/node-menu.html',
                controller: 'NodeMenuController'
            }).
            when('/graph/:rootId', {
                templateUrl: 'partials/graph.html',
                controller: 'GraphController'
            }).
            otherwise({
                redirectTo: '/graph'
            });
    }]);