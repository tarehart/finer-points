require('../../sass/bootstrap-markdown-material.scss');
require('./markdown-directive');

(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .directive('nodeEditor', nodeEditor);

    angular
        .module('nodeStandControllers')
        .controller('NodeEditorController', ['$scope', '$http', '$mdDialog', '$location', 'NodeCache', NodeEditorController]);

    function nodeEditor() {
        return {
            restrict: "A",
            scope: {
                node: "="
            },
            templateUrl: "partials/node-editor.html",
            controller: 'NodeEditorController',
            controllerAs: 'nodeEditCtrl'
        }
    }

    function NodeEditorController($scope, $http, $mdDialog, $location, NodeCache) {

        var self = this;

        self.node = $scope.node;

        self.saveNode = function(node) {
            saveChanges(node, function() {
                node.inEditMode = false;
            });
        };

        function saveChanges(node, successCallback) {
            if (NodeCache.isDraftNode(node)) { // A draft in the sense that it's brand new. This should be renamed.
                NodeCache.saveDraftNode(function(newNode) {

                    // Change the page url and reload the graph. All the UI state should stay the same because
                    // the nodes are in the NodeCache.
                    $location.path("/graph/" + newNode.stableId);
                }, function(err) {
                    toastr.error(err.message);
                });
            } else {

                NodeCache.saveNodeEdit(node, $scope.rootNode, function(editedNode, data) {
                    if (successCallback) {
                        successCallback();
                    }
                    toastr.success("Saved successfully!");
                }, function (err) {
                    toastr.error(err.message);
                });
            }
        }

        self.stopEditingBody = function(node) {
            node.editingBody = false;

            var idsInBody = [];
            var regex = /{{\[([0-9a-z]{1,25})\](.+?)(?=}})}}/g;
            var match = regex.exec(node.body.body);
            while (match != null) {
                idsInBody.push(match[1]);
                match = regex.exec(node.body.body);
            }

            // Remove any children that are no longer supported by the body text.
            for (var i = node.children.length - 1; i >= 0; i--) {
                var child = node.children[i];
                var expectedId = child.body.majorVersion.stableId; // This is a pretty deep reference chain, make sure you populate
                if (idsInBody.indexOf(expectedId) < 0) {
                    // Remove the child
                    node.children.splice(i, 1);
                    $scope.$broadcast("edgeRemoved", node, child);

                    // Keep the removed child around to support a text-based undo of the deletion.
                    node.deletedChildren = node.deletedChildren || {};
                    node.deletedChildren[child.body.majorVersion.stableId] = child;
                }
            }

            // If the user manually restored the text of a link that they previously deleted,
            // restore the link.
            if (node.deletedChildren) {
                $.each(idsInBody, function (index, id) {
                    var nodeForId = node.deletedChildren[id];
                    if (nodeForId && node.children.indexOf(nodeForId) < 0) {
                        node.children.push(nodeForId);
                        $scope.$broadcast("edgeAdded", node, nodeForId);
                    }
                });
            }

            saveChanges(node);
        };

        self.setBody = function(node, text) {
            node.body.body = text;
        };

        self.linkChild = function(node, linkCallback) {

            function attachChild(child) {
                node.children.push(child); // TODO: insert the child in the right order
                linkCallback(child.body.majorVersion.stableId, child.body.title);

                saveChanges(node);
                NodeCache.getFullDetail(child.id);
                NodeCache.fetchGraphForId(child.stableId, function() {
                    $scope.$broadcast("nodeAdded", node, child);
                });
            }

            function errorHandler(err) {
                toastr.error(err.message);
            }

            function nodeChosenForLinking(result) {
                if (result.chosenNode) {
                    var child = NodeCache.addOrUpdateNode(result.chosenNode);
                    attachChild(child);
                } else {
                    NodeCache.createAndSaveNode(result.newTitle, result.type, attachChild, errorHandler);
                }
            }

            $mdDialog.show({
                templateUrl: "partials/link-child.html",
                controller: LinkDialogController
            });

            function LinkDialogController($scope, $mdDialog) {

                var linkableTypes = [];

                if (node.getType() == "assertion") {
                    linkableTypes = ["assertion", "interpretation"];
                } else if (node.getType() == "interpretation") {
                    linkableTypes = ["source"];
                }

                $scope.canLinkTo = function(type) {
                    return linkableTypes.indexOf(type) >= 0;
                };

                $scope.toggleMakeNew = function() {
                    $scope.makeNew = !$scope.makeNew;
                };

                $scope.getSearchResults = function(query) {

                    return $http.get('/search', {params: {query: query, types:linkableTypes}})
                        .then(function(response){

                            var bodyList = response.data;
                            return bodyList;
                        });
                };

                $scope.searchResultSelected = function(bodyNode) {
                    NodeCache.getLinkChoices(bodyNode.majorVersion.id, function(nodes) {
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
                    $mdDialog.cancel();
                    nodeChosenForLinking({chosenNode: $scope.chosenNode});
                };

                $scope.createNewNode = function() {
                    $mdDialog.cancel();
                    nodeChosenForLinking({newTitle: $scope.searchQuery, type: $scope.newNodeType});
                };

                $scope.cancel = function() {
                    $mdDialog.cancel();
                };
            }

        };
    }



})();