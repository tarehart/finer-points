require('../../sass/graph.scss');
require('../controllers/node-cache');
require('./vivagraph-directive');
require('./comments-directive');
require('./consumers-directive');
require('./node-editor-directive');
require('./vote-button-directive');
require('./markdown-directive');

(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .directive('nodeGraph', nodeGraph)
        .controller('GraphController', GraphController);

    function nodeGraph() {
        return {
            restrict: "A",
            scope: {
                starterNode: "=starterNode"
            },
            templateUrl: "partials/graph.html",
            controller: 'GraphController',
            controllerAs: 'graphCtrl'
        }
    }

    function GraphController($scope, $routeParams, $location, $mdDialog, NodeCache, ToastService) {

        var self = this;

        self.publishableNodes = [];

        function setHighlighted(node) {
            self.highlightedNode = node;
        }

        $scope.$on("nodeHighlighted", function(e, node) {
            setHighlighted(node);
        });

        function swapInChild(newChild, oldChild, rootNode) {
            if (!rootNode.body.public) {
                $.each(rootNode.children, function(index, child) {
                    if (child === oldChild) {
                        // modify rootNode to point to the draft instead of the original
                        rootNode.children[index] = newChild;
                        // Save root node
                        saveChanges(rootNode);
                        return false; // break out of $.each
                    } else {
                        swapInChild(newChild, oldChild, child);
                    }
                });
            }
        }

        self.enterEditMode = function (node) {
            if (node.body.public) {
                NodeCache.makeDraft(node, function(draftNode, data) {

                    if (node === self.rootNode) {
                        $location.path("/graph/" + data.graph.rootStableId);
                    } else {
                        // Modify all the parent node so it points to the new draft. There may be multiple
                        // parent nodes in the tree; modify them all.
                        // TODO: consider doing this server side, as we do with publish
                        swapInChild(data.editedNode, node, self.rootNode);
                    }
                    self.enterEditMode(data.editedNode);
                    data.editedNode.isSelected = true;

                }, function (err) {
                    ToastService.error(err.message);
                });

            } else {
                node.inEditMode = true;

                // Proactively get the children too because we might need their majorVersion id's during editing
                $.each(node.children, function(index, child) {
                    ensureDetail(child);
                });
            }
        };

        if ($routeParams && $routeParams.rootStableId) {
            NodeCache.fetchGraphForId($routeParams.rootStableId, function() {
                self.rootNodes = [];
                self.rootNodes.push(NodeCache.getByStableId($routeParams.rootStableId));
                self.rootNode = self.rootNodes[0];
                ensureDetail(self.rootNode);
                $scope.$broadcast("rootData", self.rootNode);
            });
        } else if ($scope.starterNode) {
            self.rootNodes = [$scope.starterNode];
            self.rootNode = self.rootNodes[0];
            $scope.$broadcast("rootData", self.rootNode);
        } else {
            var starterNode = NodeCache.getOrCreateDraftNode();
            self.rootNodes = [starterNode];
            self.rootNode = self.rootNodes[0];
            $scope.$broadcast("rootData", self.rootNode);
            self.draftNodes = [starterNode];
            self.enterEditMode(starterNode);
            starterNode.isSelected = true;
        }

        self.hasChild = function (node) {
            return node.children && node.children.length;
        };

        // Selection stuff. I have functions for these even though they could
        // fit in the html because in html there are a bunch of nested scopes
        // and access to $scope there is weird.

        self.isSelected = function (node) {
            return node.isSelected;
        };

        self.isPersisted = function (node) {
            return node && node.id !== "draft";
        };

        self.toggleSelect = function (node) {
            node.isSelected = !node.isSelected;
            if (node.isSelected) {
                ensureDetail(node);
            }

            $scope.$broadcast("nodeHighlighted", node);

            return true;
        };

        self.toggleChildren = function (node) {
            node.hideChildren = !node.hideChildren;
        };

        self.navigateToNode = function (node) {
            // Change the page url and reload the graph. All the UI state should stay the same because
            // the nodes are in the NodeCache.
            $location.path("/graph/" + node.stableId);
        };

        self.authorizedForEdit = function (node, parent) {
            // Currently, I only want to allow child editing if the children are already private,
            // or if the child's immediate parent is private.
            // This is because I don't intend to mess with draft propagation anymore.
            // If you want to edit a public child, the user needs to navigate to it first,
            // or start edit its parent.
            return node === self.rootNode || !node.body.public || (parent && !parent.body.public);
        };

        function ensureDetail(node) {
            if (!hasFullDetail(node)) {
                NodeCache.getFullDetail(node.id);
            }
            if (!node.consumers) {
                NodeCache.fetchConsumers(node.id);
            }
        }

        function hasFullDetail(node) {
            return node.body && node.body.author && node.body.author.stableId;
        }

        self.saveNode = function(node) {
            saveChanges(node, function() {
                node.inEditMode = false;
            });
        };

        function saveChanges(node, successCallback) {
            if (NodeCache.isBlankSlateNode(node)) {
                NodeCache.saveBlankSlateNode(function(newNode) {

                    // Change the page url and reload the graph. All the UI state should stay the same because
                    // the nodes are in the NodeCache.
                    $location.path("/graph/" + newNode.stableId);
                }, function(err) {
                    ToastService.error(err.message);
                });
            } else {

                NodeCache.saveNodeEdit(node, function(editedNode, data) {
                    if (successCallback) {
                        successCallback();
                    }
                    ToastService.success("Saved successfully!");
                }, function (err) {
                    ToastService.error(err.message);
                });
            }
        }

        self.setBody = function(node, text) {
            node.body.body = text;
        };

        self.readyToPublish = function(node) {
            return (!node.body.public) && allowsPublish(node, {});
        };

        self.publishNode = function(node) {
            var publishableSet = {};
            if (allowsPublish(node, publishableSet)) {

                // Problem: draft -> draft -> published
                // Publish the middle one
                // Now the middle one appears to have no child and no author.
                // Upon refresh, it has correct child and author.

                NodeCache.publishNode(node, function(quickGraph) {

                    var rootNode = self.rootNode;

                    if (node === rootNode) {
                        $location.path("/graph/" + quickGraph.rootStableId); // Change url back to public version
                    }
                });
            } else {
                // TODO: display an error
            }
        };


        self.toggleConsumers = function(node) {

            $mdDialog.show({
                template: '<div node-consumers node="consumersCtrl.rootNode"></div>',
                controller: ConsumersDialogController,
                controllerAs: "consumersCtrl",
                clickOutsideToClose: true,
                openFrom: "#consumers-btn"
            });

        };

        function ConsumersDialogController($scope, $mdDialog) {
            var consumersCtrl = this;
            consumersCtrl.rootNode = self.rootNode;

            $scope.$on('$locationChangeStart', function() {
                $mdDialog.hide();
            });
        }

        function allowsPublish(node, publishableSet) {

            if (publishableSet[node.id] || node.body.draft === false) {
                return true;
            }

            if (!node.body.title) {
                return false;
            }

            if (node.type == "source") {
                if (node.body.url) {
                    publishableSet[node.id] = 1;
                    return true;
                }
                return false;
            }

            if (node.type == "interpretation") {
                if (node.body.body && node.children.length == 1 && allowsPublish(node.children[0], publishableSet)) {
                    publishableSet[node.id] = 1;
                    return true;
                }
                return false;
            }

            if (node.type == "assertion") {
                if (node.body.body && node.children.length) {
                    for (var i = 0; i < node.children.length; i++) {
                        if (!allowsPublish(node.children[i], publishableSet)) {
                            return false;
                        }
                    }
                    publishableSet[node.id] = 1;
                    return true;
                } else {
                    return false;
                }
            }
        }

    }

})();