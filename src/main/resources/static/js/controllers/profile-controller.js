require('../directives/node-list-directive');

(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .controller('ProfileController', ProfileController);

    function ProfileController($scope, $routeParams, $http, UserService, NodeCache) {

        var self = this;
        
        self.subject = {};
        self.stableId = $routeParams.authorStableId;
        self.draftNodes = [];
        self.publishedNodes = [];

        $http.get('/getProfile', {params: {stableId: self.stableId}}).success(function (data) {
            self.subject = data;
        });

        if (UserService.userControlsAlias(self.stableId)) {
            fetchDrafts();
        } else {
            UserService.subscribeSuccessfulLogin($scope, function() {
                if (UserService.userControlsAlias(self.stableId)) {
                    fetchDrafts();
                }
            });
        }

        function fetchDrafts() {
            $http.get('/draftNodes', {params: {authorStableId: self.stableId}}).success(function (data) {
                self.draftNodes.push.apply(self.draftNodes, data); // Push data to nodes
            });
        }

        $http.get('/nodesPublishedByUser', {params: {stableId: self.stableId}}).success(function (data) {
            self.publishedNodes.push.apply(self.publishedNodes, data); // Push data to nodes
        });

        self.discardDraft = function(node) {
            NodeCache.discardDraft(node, function() {
                var idx = self.draftNodes.indexOf(node);
                self.draftNodes.splice(idx, 1);
            });
        };
    }

})();