require('../sass/common.scss');

(function() {
    'use strict';

    angular.module('nodeStandControllers', ['ngCookies']);

    require('./services/token-interceptor');
    require('./controllers/gateway-controller');
    require('./controllers/profile-controller');
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
                    templateUrl: 'partials/signin.html'
                }).
                when('/graph/:rootStableId', {
                    templateUrl: 'partials/explorer.html'
                }).
                when('/create', {
                    templateUrl: 'partials/create.html'
                }).
                when('/user/:userStableId', {
                    templateUrl: 'partials/user.html',
                    controller: 'ProfileController',
                    controllerAs: 'profileCtrl'
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