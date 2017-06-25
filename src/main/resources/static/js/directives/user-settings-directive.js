require('../../sass/vote-button.scss');
require('../services/toast-service');

(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .directive('userSettings', userSettings);

    function userSettings() {
        return {
            restrict: "A",
            templateUrl: "partials/user-settings.html",
            controller: UserSettingsController
        }
    }

    function UserSettingsController($scope, $rootScope, $http, UserService, ToastService) {

        var editableMap = {};

        $scope.canEditAlias = function (authorStableId) {
            return editableMap[authorStableId];
        };

        var user = UserService.getUser();
        if (user == null) {
            UserService.subscribeSuccessfulLogin($scope, function() {
                user = UserService.getUser();
                setUpAliases();
            });
        } else {
            setUpAliases();
        }


        function setUpAliases() {
            var aliases = UserService.getUser().aliases;

            for (var i = 0; i < aliases.length; i++) {
                populateEditableFlag(aliases[i].stableId)
            }
        }

        function populateEditableFlag(authorStableId) {
            $http.get('/canChangeAuthorName', { params: {authorStableId: authorStableId }})
                .success(function (data) {

                    editableMap[authorStableId] = !!data;
                })
                .error(function(err) {
                    ToastService.error(err.message);
                });
        }

    }

})();