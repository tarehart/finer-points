(function() {
    'use strict';

    angular.module('nodeStandControllers', []);

    var nodeStandApp = angular.module('nodeStandApp', [
        'ngRoute',
        'ngMaterial',
        'markdown',
        'ui.bootstrap',
        'particles',
        'nodeStandControllers'
    ]);

    nodeStandApp.config(['$routeProvider', '$mdThemingProvider',
        function ($routeProvider, $mdThemingProvider) {
            $routeProvider.
                when('/', {
                    templateUrl: 'partials/gateway.html',
                    controller: 'GatewayController'
                }).
                when('/login', {
                    templateUrl: 'partials/signin.html',
                    controller: 'LoginController'
                }).
                when('/graph', {
                    templateUrl: 'partials/node-menu.html',
                    controller: 'NodeMenuController'
                }).
                when('/graph/:rootStableId', {
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
                    redirectTo: '/'
                });

            $mdThemingProvider.theme('default')
                .primaryPalette('teal')
                .accentPalette('cyan');
        }]);

    nodeStandApp.run(['$rootScope', 'UserService', function ($rootScope, UserService) {
        $rootScope.user = UserService.getUser();

        UserService.subscribeSuccessfulLogin($rootScope, function () {
            $rootScope.user = UserService.getUser();
        });
    }]);

})();