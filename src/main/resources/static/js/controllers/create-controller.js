(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .controller('CreateController', ['$scope', '$http', '$modal', 'NodeCache', CreateController]);

    function CreateController($scope, $http, $modal, NodeCache) {

        $scope.starterNode = NodeCache.createDraftNode();

        prepareNodeForEditing($scope.starterNode);

        function nodeSavedCallback(data, originalNode) {
            var savedNode = NodeCache.addOrUpdateNode(data);
            if (originalNode == $scope.starterNode) {
                $scope.starterNode = savedNode;
            }
        }

        function errorCallback(err) {
            // TODO: remember the request the user attempted, sign in via ajax,
            // and let them try again.
            window.location = '/signin';
        }

        $scope.submit = function () {

            saveNewNode($scope.starterNode, null, nodeSavedCallback, errorCallback);
        };

        $scope.setText = function (text) {
            $scope.starterNode.body.body = text;
        };


        function saveNewNode(node, parentId, successCallback, errorCallback) {
            var links = node.children.map(function(child) {
                return child.id;
            });

            $http.post('/create',
                {
                    title: node.body.title,
                    body: node.body.body,
                    parentId: parentId,
                    links: links
                })
                .success(function (data) {
                    if (successCallback) {
                        successCallback(data, node);
                    }
                })
                .error(function(err) {
                    if (errorCallback) {
                        errorCallback(err);
                    }
                });
        }

        function nodeHasRealId(node) {
            return node.id != "draft";
        }

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
                    // TODO: go ahead and save the draft node here, and then fetch its graph
                    if (nodeHasRealId(node)) {

                    } else {
                        saveNewNode(node, null, nodeSavedCallback, errorCallback);
                    }

                    //NodeCache.fetchGraphForId(child.id);
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