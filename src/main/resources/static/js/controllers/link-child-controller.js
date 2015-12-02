(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .controller('LinkChildController', ['$scope', '$http', '$modalInstance', 'NodeCache', 'linkCallback', 'currentNode', LinkChildController]);

    function LinkChildController($scope, $http, $modalInstance, NodeCache, linkCallback, currentNode) {

        var linkableTypes = [];

        if (currentNode.getType() == "assertion") {
            linkableTypes = ["assertion", "interpretation"];
        } else if (currentNode.getType() == "interpretation") {
            linkableTypes = ["source"];
        }

        function canLinkTo(type) {
            return linkableTypes.indexOf(type) >= 0;
        }

        $scope.getSearchResults = function(query) {

            return $http.get('/search', {params: {query: query, types:linkableTypes}})
                .then(function(response){

                    var bodyList = response.data;
                    return bodyList;
                });
        };

        $scope.searchResultSelected = function(bodyNode) {
            NodeCache.getLinkChoices(bodyNode.id, function(nodes) {
                // Although the nodes param is a list, it's actually associated with a single selection in the search
                // box. That's because search results are rolled up by major version to prevent the perception of
                // duplicates. In the future, we may allow the user to select from among these nodes.
                // For now, get the most recent node for linking.
                sortNodesByVersion(nodes);

                $scope.chosenNode = nodes[nodes.length - 1];
                $scope.isResultSelected = true;

            }, function(err) {
                alert("There was an error: " + err);
            });

        };

        function sortNodesByVersion(nodes) {
            nodes.sort(function(n, m) {
                var nVersion = n.getVersionString().split(".");
                var mVersion = m.getVersionString().split(".");

                for (var i = 0; i < nVersion.length && i < mVersion.length; i++) {
                    var difference = nVersion[i] - mVersion[i];
                    if (difference != 0) {
                        return difference;
                    }
                }

                return 0;
            });
        }

        $scope.clearSelection = function() {
            $scope.isResultSelected = false;
            $scope.chosenNode = null;
        };

        $scope.select = function() {
            $modalInstance.close();
            linkCallback({chosenNode: $scope.chosenNode});
        };

        $scope.createAssertion = function() {
            createNew("assertion");
        };

        $scope.createInterpretation = function() {
            createNew("interpretation");
        };

        $scope.createSource = function() {
            createNew("source");
        };

        function createNew(type) {
            $modalInstance.close();
            linkCallback({newTitle: $scope.selectedResult, type: type});
        };

        $scope.cancel = function() {
            $modalInstance.close();
        };

        $scope.canCreateAssertion = function() {
            return canLinkTo("assertion");
        };

        $scope.canCreateInterpretation = function() {
            return canLinkTo("interpretation");
        };

        $scope.canCreateSource = function() {
            return canLinkTo("source");
        };

    }

})();