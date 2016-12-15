require('../sass/common.scss');

(function() {
    'use strict';

    angular.module('jsFatals', []);
    angular.module('nodeStandControllers', ['ngCookies', 'jsFatals']);

    require('./services/token-interceptor');
    require('./controllers/gateway-controller');
    require('./controllers/profile-controller');
    require('./controllers/create-controller');
    require('./services/user-service');
    require('./directives/graph-directive');
    require('./directives/edit-history-directive');
    require('./controllers/route-params-controller');
    require('./services/errorlog-service');

    var nodeStandApp = angular.module('nodeStandApp', [
        'ngRoute',
        'ngMaterial',
        'ngSanitize',
        'nodeStandControllers'
    ]);

    nodeStandApp.config(function ($routeProvider, $mdThemingProvider, $locationProvider, $provide) {

            // If you edit this, edit MvcConfig.java!
            $routeProvider.
                when('/', {
                    template: require('../partials/gateway.html'),
                    controller: 'GatewayController'
                }).
                when('/login', {
                    template: require('../partials/signin.html')
                }).
                when('/graph/:rootStableId', {
                    template: require('../partials/explorer.html')
                }).
                when('/create', {
                    template: require('../partials/create.html'),
                    controller: 'CreateController',
                    controllerAs: 'createCtrl'
                }).
                when('/profile/:authorStableId', {
                    template: require('../partials/user.html'),
                    controller: 'ProfileController',
                    controllerAs: 'profileCtrl'
                }).
                when('/history/:nodeStableId', {
                    template: require('../partials/history.html'),
                    controller: 'RouteParamsController',
                    controllerAs: 'routeParamsCtrl'
                }).
                otherwise({
                    redirectTo: '/'
                });

            $mdThemingProvider.theme('default')
                .primaryPalette('blue-grey')
                .accentPalette('light-blue');

            $locationProvider.html5Mode(true);

            $provide.decorator('$exceptionHandler', function ($delegate, ErrorLogService) {
                return function(exception, cause) {
                    $delegate(exception, cause);
                    ErrorLogService.log(exception, cause);
                };
            });

        });

    nodeStandApp.run(['$rootScope', 'UserService', function ($rootScope, UserService) {
        $rootScope.user = UserService.getUser();

        UserService.subscribeSuccessfulLogin($rootScope, function () {
            $rootScope.user = UserService.getUser();
        });
    }]);

})();