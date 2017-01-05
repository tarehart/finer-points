require('./toast-service');

(function () {
    'use strict';

    angular
        .module('nodeStandControllers')
        .service('UserService', UserService);

    function UserService($http, $auth, $rootScope, $mdDialog, ToastService) {
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

            return !!$.grep(self.loggedInUser.aliases, function(val, idx) {
                return val.stableId === aliasStableId
            }).length;
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
            $auth.logout();
            $rootScope.user = null;
            self.loggedInUser = null;
        };

        $rootScope.showLogin = function(ev) {
            $mdDialog.show({
                controller: LoginController,
                controllerAs: 'loginCtrl',
                templateUrl: 'partials/signin.html',
                parent: angular.element(document.body),
                targetEvent: ev,
                clickOutsideToClose:true
            });
        };

        function LoginController($mdDialog, $auth) {
            var self = this;

            self.authenticate = function(provider) {
                $auth.authenticate(provider)
                    .then(function(response) {
                        getUserFromServer();
                        $mdDialog.cancel();
                    })
                    .catch(function(response) {
                        ToastService.error(response);
                    });

            };

            self.cancel = function() {
                $mdDialog.cancel();
            };
        }

        function getUserFromServer() {

            $http.get('/currentUser')
                .success(function (data) {
                    if (data) {
                        data.user.bodyVotes = data.bodyVotes;
                        data.user.commentVotes = data.commentVotes;
                        self.loggedInUser = data.user;
                        self.loggedInUser.activeAlias = data.user.aliases[0];
                        notifySuccessfulLogin();
                    }

                    // If there's no data, then the user is not signed in.
                })
                .error(function(err) {
                    ToastService.error(err.message);
                });
        }
    }

})();