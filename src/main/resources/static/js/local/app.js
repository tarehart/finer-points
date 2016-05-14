require('../../sass/common.scss');

(function() {
    'use strict';

    var angular = require('angular');

    angular.module('nodeStandControllers', []);

    require('./controllers/gateway-controller');
    require('./controllers/login-controller');
    require('./services/user-service');
    require('./directives/graph-directive');

    var nodeStandApp = angular.module('nodeStandApp', [
        'ngRoute',
        'ngMaterial',
        'ngSanitize',
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
                when('/graph/:rootStableId', {
                    templateUrl: 'partials/explorer.html'
                }).
                when('/create', {
                    templateUrl: 'partials/create.html'
                }).
                otherwise({
                    redirectTo: '/'
                });

            $mdThemingProvider.theme('default')
                .primaryPalette('blue-grey')
                .accentPalette('light-blue');
        }]);

    nodeStandApp.run(['$rootScope', 'UserService', function ($rootScope, UserService) {
        $rootScope.user = UserService.getUser();

        UserService.subscribeSuccessfulLogin($rootScope, function () {
            $rootScope.user = UserService.getUser();
        });
    }]);

})();