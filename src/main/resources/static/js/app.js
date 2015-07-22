var nodeStandApp = angular.module('nodeStandApp', [
    'ngRoute',
    'markdown',
    'ui.bootstrap',
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
                templateUrl: 'partials/explorer.html'
            }).
            when('/create', {
                templateUrl: 'partials/create.html'
            }).
            when('/graphDiagnostic/:rootId', {
                templateUrl: 'partials/graphDiagnostic.html',
                controller: 'GraphController'
            }).
            when('/d/:id', {
                templateUrl: 'partials/detail.html',
                controller: 'DetailController'
            }).
            otherwise({
                redirectTo: '/graph'
            });
    }]);