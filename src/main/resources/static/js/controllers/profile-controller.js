require('../directives/node-list-directive');

(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .controller('ProfileController', ProfileController);

    function ProfileController($scope, $routeParams, $http) {

        var self = this;
        
        self.subject = {};
        self.stableId = $routeParams.userStableId;
        self.draftNodes = [];
        self.publishedNodes = [];

        $http.get('/getProfile', {params: {stableId: self.stableId}}).success(function (data) {
            self.subject = data;
        });
        
        $http.get('/draftNodes').success(function (data) {
            self.draftNodes.push.apply(self.draftNodes, data); // Push data to nodes
        });

        $http.get('/nodesPublishedByUser', {params: {stableId: self.stableId}}).success(function (data) {
            self.publishedNodes.push.apply(self.publishedNodes, data); // Push data to nodes
        });
    }

})();