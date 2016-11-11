(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .directive('nodeEditHistory', nodeEditHistory)
        .controller('HistoryController', HistoryController);

    function nodeEditHistory() {
        return {
            restrict: "A",
            scope: {
                nodeStableId: "="
            },
            templateUrl: "partials/edit-history.html",
            controller: 'HistoryController',
            controllerAs: 'historyCtrl'
        }
    }

    function HistoryController($scope, $http) {

        var self = this;
        self.nodeStableId = $scope.nodeStableId;

        self.history = [];


        $http.get('/nodeEditHistory', {params: {'stableId': self.nodeStableId}}).success(function (data) {


            self.history = [];

            var currentBody = data.latestBody;

            while (currentBody != null) {
                self.history.push({body: currentBody, nodeStableId: data.bodyToStableId[currentBody.id]});
                currentBody = currentBody.previousVersion;
            }

        });
    }

})();