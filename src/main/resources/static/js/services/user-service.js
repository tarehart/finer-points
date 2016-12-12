require('./toast-service');
require('./token-storage');

(function () {
    'use strict';

    angular
        .module('nodeStandControllers')
        .service('UserService', UserService);

    function UserService($http, $rootScope, $cookies, ToastService, TokenStorage) {
        var self = this;

        self.loggedInUser = null;

        getUserFromServer();

        self.getUser = function() {
            return self.loggedInUser;
        };

        self.getActiveAlias = function() {
            return self.loggedInUser.activeAlias;
        };

        self.userControlsAlias = function(aliasStableId) {
            if (!self.loggedInUser || !self.loggedInUser.aliases) {
                return false;
            }

            return !!$.grep(self.loggedInUser.aliases, function(val, idx) { return val.stableId = aliasStableId });
        };

        // http://www.codelord.net/2015/05/04/angularjs-notifying-about-changes-from-services-to-controllers/
        self.subscribeSuccessfulLogin = function(scope, callback) {
            var handler = $rootScope.$on('successful-login', callback);
            scope.$on('$destroy', handler);
        };

        function notifySuccessfulLogin() {
            $rootScope.$emit('successful-login');
        }

        $rootScope.logout = function () {
            TokenStorage.clear();
            $rootScope.user = null;
            self.loggedInUser = null;
        };

        function getUserFromServer() {

            var authCookie = $cookies.get('AUTH-TOKEN');
            if (authCookie) {
                TokenStorage.store(authCookie);
                $cookies.remove('AUTH-TOKEN');
            }

            $http.get('/currentUser')
                .success(function (data) {
                    data.user.bodyVotes = data.bodyVotes;
                    data.user.commentVotes = data.commentVotes;
                    self.loggedInUser = data.user;
                    self.loggedInUser.activeAlias = data.user.aliases[0];
                    notifySuccessfulLogin();
                })
                .error(function(err) {
                    if (err.status === 403) {
                        return; // This is expected, it just means that nobody is logged in right now.
                    }
                    ToastService.error(err.message);
                });
        }
    }

})();