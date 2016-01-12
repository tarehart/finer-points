(function () {
    'use strict';

    angular
        .module('nodeStandControllers')
        .service('UserService', ['$http', '$rootScope', UserService]);

    function UserService($http, $rootScope) {
        var self = this;

        self.loggedInUser = null;

        getUserFromServer();

        self.getLoggedInUser = function() {
            return self.loggedInUser;
        };

        // http://www.codelord.net/2015/05/04/angularjs-notifying-about-changes-from-services-to-controllers/
        self.subscribeSuccessfulLogin = function(scope, callback) {
            var handler = $rootScope.$on('successful-login', callback);
            scope.$on('$destroy', handler);
        };

        function notifySuccessfulLogin() {
            $rootScope.$emit('successful-login');
        }

        function getUserFromServer() {
            $http.get('/currentUser')
                .success(function (user) {
                    self.loggedInUser = user;
                    notifySuccessfulLogin();
                })
                .error(function(err) {
                    if (err.status === 401) {
                        return; // This is expected, it just means that nobody is logged in right now.
                    }
                    toastr.error(err.message);
                });
        }
    }

})();