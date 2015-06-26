(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .controller('CreateController', ['$scope', '$http', '$modal', 'NodeCache', CreateController]);

    function CreateController($scope, $http, $modal, NodeCache) {

        $scope.starterNode = NodeCache.createDraftNode();

        prepareNodeForEditing($scope.starterNode);

        $scope.submit = function () {
            $http.post('/create', {title: $scope.starterNode.title, body: $scope.starterNode.body, parentId: null})
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
            $scope.starterNode.body = text;
        };



        function prepareNodeForEditing(node) {


            node.setBody = function(text) {
                node.body = text;
            }
            node.stopEditingBody = function() {
                node.editingBody = false;
            }
            node.linkChild = function(linkCallback) {

                function nodeChosenForLinking(child) {
                    node.children.push(child);
                    linkCallback(child.body.majorVersion.id, child.body.title);
                }

                $modal.open({
                    templateUrl: "partials/link-child.html",
                    controller: "LinkChildController",
                    resolve: {
                        linkCallback: function() {return nodeChosenForLinking; }
                    }
                });

            }
            node.isEditable = true;
        }
    }

})();