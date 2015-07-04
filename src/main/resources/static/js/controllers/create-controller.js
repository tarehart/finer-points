(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .controller('CreateController', ['$scope', '$http', '$modal', 'NodeCache', CreateController]);

    function CreateController($scope, $http, $modal, NodeCache) {

        $scope.starterNode = NodeCache.createDraftNode();

        prepareNodeForEditing($scope.starterNode);

        $scope.submit = function () {
            var links = $scope.starterNode.children.map(function(child) {
                return child.id;
            });

            $http.post('/create',
                {title: $scope.starterNode.body.title,
                    body: $scope.starterNode.body.body,
                    parentId: null,
                    links: links
                })
                .success(function (data) {
                    alert("Success! " + data);
                })
                .error(function() {
                    // TODO: remember the request the user attempted, sign in via ajax,
                    // and let them try again.
                    window.location = '/signin';
                });
        };

        $scope.setText = function (text) {
            $scope.starterNode.body.body = text;
        };



        function prepareNodeForEditing(node) {


            node.setBody = function(text) {
                node.body.body = text;
            };
            node.stopEditingBody = function() {
                node.editingBody = false;
            };
            node.linkChild = function(linkCallback) {

                function nodeChosenForLinking(child) {
                    child = NodeCache.addOrUpdateNode(child);
                    node.children.push(child);
                    NodeCache.fetchGraphForId(child.id);
                    linkCallback(child.body.majorVersion.id, child.body.title);
                }

                $modal.open({
                    templateUrl: "partials/link-child.html",
                    controller: "LinkChildController",
                    resolve: {
                        linkCallback: function() {return nodeChosenForLinking; }
                    }
                });

            };
            node.isEditable = true;
        }
    }

})();