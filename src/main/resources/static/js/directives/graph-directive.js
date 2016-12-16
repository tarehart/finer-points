require('../../sass/graph.scss');
require('../controllers/node-cache');
require('../services/user-service');
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
                draftType: "="
            },
            template: require("../../partials/graph.html"),
            controller: 'GraphController',
            controllerAs: 'graphCtrl'
        }
    }

    function GraphController($scope, $routeParams, $location, $mdDialog, NodeCache, ToastService, UserService) {

        var self = this;

        self.publishableNodes = [];

        $scope.$watch('draftType', function() {
            var draftType = $scope.draftType;

            if (draftType) {

                if (self.rootNode) {
                    self.rootNode.type = draftType;
                } else {

                    var starterNode = NodeCache.getOrCreateDraftNode(draftType);
                    self.rootNodes = [starterNode];
                    self.rootNode = self.rootNodes[0];
                    self.draftNodes = [starterNode];
                    self.enterEditMode(starterNode);
                    starterNode.isSelected = true;
                }

                $scope.$broadcast("rootData", self.rootNode);
            }
        });


        function setHighlighted(node) {
            self.highlightedNode = node;
        }

        $scope.$on("nodeHighlighted", function(e, node) {
            setHighlighted(node);
            revealChild(self.rootNode, node);
        });

        $scope.$on("nodeSaved", function(e, node) {
            self.problemReport = buildProblemReport(self.rootNode);
        });

        function revealChild(node, child) {
            if (node === child) {
                return true;
            }

            var holdsChild = false;

            $.each(node.children, function(index, n) {
                 if (revealChild(n, child)) {
                     holdsChild = true;
                 }
            });

            if (holdsChild) {
                node.hideChildren = false;
                return true;
            }

            return false;
        }

        self.enterEditMode = function (node) {
            if (node.body.public) {

                if (node !== self.rootNode) {
                    ToastService.error("Please navigate to the node before attempting to edit.");
                }

                var alias = UserService.getActiveAlias();

                NodeCache.makeDraft(node, alias, function(draftNode, data) {

                    $location.path("/graph/" + data.graph.rootStableId);

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

            self.rootStableId = $routeParams.rootStableId;

            NodeCache.fetchGraphForId($routeParams.rootStableId, function() {
                self.rootNode = NodeCache.getByStableId($routeParams.rootStableId);
                self.rootNodes = [self.rootNode]; // We have this in array form because it's handy for kicking off the angular template recursion.

                if (self.rootNode.children && self.rootNode.children.length > 1) {
                    $.each(self.rootNode.children, function (index, child) {
                        child.hideChildren = true;
                    });
                }
                self.problemReport = buildProblemReport(self.rootNode);
                $scope.$broadcast("rootData", self.rootNode);
            });
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

            return true;
        };

        self.highlightNode = function (node) {
            $scope.$broadcast("nodeHighlighted", node);
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
            return UserService.getUser() && (node === self.rootNode || !node.body.public || (parent && !parent.body.public));
        };

        function ensureDetail(node) {
            if (!hasFullDetail(node)) {
                NodeCache.getFullDetail(node.stableId);
            }
        }

        function hasFullDetail(node) {
            return node.body && node.body.author && node.body.author.stableId;
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

        self.discardDraft = function() {
            NodeCache.discardDraft(self.rootNode, function() {
                $location.path("/");
            });
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

        function allowsPublish(node) {
            return self.problemReport && !self.problemReport.find(n => n.node === node);
        }

        function buildProblemReport(node) {

            var visitedNodes = new Set();

            var problemReport = [];

            buildReport(node);

            return problemReport;

            function buildReport(node) {

                if (visitedNodes.has(node) || node.body.draft === false) {
                    return;
                }

                visitedNodes.add(node);

                if (!node.body.title) {
                    problemReport.push({message: "You need to give your card a title.", node: node});
                }

                if (node.body.title === 'Untitled') {
                    problemReport.push({message: 'You need to write a title other than "Untitled."', node: node});
                }

                if (node.type == "source") {
                    if (!node.body.url) {
                        problemReport.push({message: "You need a URL for your source node.", node: node});
                    }
                }
                else if (node.type == "interpretation") {
                    if (!node.body.body) {
                        problemReport.push({message: "You need some text in your interpretation.", node: node});
                    }

                    if (!node.children.length) {
                        problemReport.push({message: "You need to attach a source card support your interpretation.", node: node});
                    }
                    else {
                        buildReport(node.children[0]);
                    }
                }
                else if (node.type == "assertion") {
                    if (!node.body.body) {
                        problemReport.push({message: "You need some text in your opinion.", node: node});
                    }

                    if (!node.children.length) {
                        problemReport.push({message: "You need to attach cards to support your opinion.", node: node});
                    }

                    for (var i = 0; i < node.children.length; i++) {
                        buildReport(node.children[i]);
                    }
                }
            }
        }

    }

})();