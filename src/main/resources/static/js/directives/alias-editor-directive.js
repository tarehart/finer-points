require('../../sass/vote-button.scss');
require('../services/toast-service');

(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .directive('aliasEditor', aliasEditor);

    function aliasEditor() {
        return {
            restrict: "A",
            templateUrl: "partials/alias-editor.html",
            controller: AliasEditorController,
            controllerAs: 'aliasEditorCtrl',
            scope: {
                alias: "=",
                showHyperlink: "@"
            }
        }
    }

    function AliasEditorController($scope, $http, ToastService) {

        var self = this;
        self.alias = $scope.alias;
        self.showHyperlink = !!$scope.showHyperlink;

        self.startEditingAlias = function() {
            self.editingAlias = true;
            self.modifiedAliasName = self.alias.displayName;
        };

        self.saveAlias = function() {
            save(self.alias.stableId, self.modifiedAliasName);
            self.editingAlias = false;
        };

        self.cancel = function() {
            self.editingAlias = false;
        };

        populateEditableFlag(self.alias.stableId);

        function populateEditableFlag(authorStableId) {
            $http.get('/canChangeAuthorName', { params: {authorStableId: authorStableId }})
                .success(function (data) {

                    self.canEditAlias = !!data;
                })
                .error(function(err) {
                    ToastService.error(err.message);
                });
        }

        function save(authorStableId, displayName) {
            $http.get('/changeAuthorName', { params: {authorStableId: authorStableId, authorName: displayName }})
                .success(function (data) {
                    self.alias.displayName = displayName;
                })
                .error(function(err) {
                    ToastService.error(err.message);
                });
        }

    }

})();