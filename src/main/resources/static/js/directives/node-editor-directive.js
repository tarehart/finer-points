require('../../sass/bootstrap-markdown-material.scss');
require('../../sass/node-linker.scss');
require('./markdown-directive');
require('../services/toast-service');
require('../services/user-service');
require('../services/body-text-service');

(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .directive('nodeEditor', nodeEditor);

    angular
        .module('nodeStandControllers')
        .controller('NodeEditorController', NodeEditorController);

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

    function NodeEditorController($scope, $rootScope, $http, $mdDialog, $location, ToastService, NodeCache, UserService, BodyTextService) {

        var self = this;

        self.node = $scope.node;

        self.backupCopy = {
            body: {}
        };
        copyFields(self.node, self.backupCopy);

        self.saveNode = function() {

            stopEditingBody(self.node);
        };

        self.cancel = function() {
            copyFields(self.backupCopy, self.node);

            // This will cause vivagraph to reset, and probably do other useful stuff too.
            $scope.$emit("canceledEdit", self.node);

            self.node.inEditMode = false;
        };

        self.getRenderType = function(node) {
            if (node.type == 'assertion' || node.type == 'interpretation') {
                return 'markdown';
            }
            return 'url';
        };

        function copyFields(sourceNode, targetNode) {
            targetNode.body.title = sourceNode.body.title;
            targetNode.body.body = sourceNode.body.body;
            targetNode.body.url = sourceNode.body.url;
            targetNode.body.qualifier = sourceNode.body.qualifier;

            targetNode.children = [];
            for (var i = 0; i < sourceNode.children.length; i++) {
                targetNode.children.push(sourceNode.children[i]);
            }
        }

        function saveChanges(node, successCallback) {
            if (NodeCache.isBlankSlateNode(node)) {
                var alias = UserService.getActiveAlias();
                NodeCache.saveBlankSlateNode(alias, function(newNode) {
                    if(successCallback) {
                        successCallback(newNode);
                    }
                    // Change the page url and reload the graph. All the UI state should stay the same because
                    // the nodes are in the NodeCache.
                    $location.path("/graph/" + newNode.stableId);
                }, function(err) {
                    ToastService.error(err.message);
                });
            } else {

                NodeCache.saveNodeEdit(node, function(editedNode, data) {
                    ToastService.success("Saved successfully!");
                    $scope.$emit("nodeSaved", editedNode);
                    if (successCallback) {
                        successCallback(editedNode);
                    }
                }, function (err) {
                    ToastService.error(err.message);
                });
            }
        }

        function stopEditingBody(node) {

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
                    $rootScope.$broadcast("edgeRemoved", node, child);

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
                        $rootScope.$broadcast("edgeAdded", node, nodeForId);
                    }
                });
            }

            saveChanges(node, function() { node.inEditMode = false });
        }

        self.setBody = function(node, text) {
            node.body.body = text;
        };

        self.linkChild = function(node, linkCallback) {

            // idToReplace is optional.
            function attachChild(child, idToReplace) {

                NodeCache.addChildToNode(node, child);

                if (idToReplace) {
                    var oldChild = $.grep(node.children, function(c) {
                        return c.body.majorVersion.stableId === idToReplace;
                    })[0];

                    // Remove the old child
                    node.children = node.children.filter(function(c){
                        return c !== oldChild;
                    });

                    $rootScope.$broadcast("edgeRemoved", node, oldChild);
                }

                // This thing inserts the proper majorVersionId into the text.
                // Probably defined in markdown-directive.js
                linkCallback(child.body.majorVersion.stableId, child.body.title, idToReplace);

                NodeCache.getFullDetail(child.stableId);
                NodeCache.fetchGraphForId(child.stableId, function() {
                    $rootScope.$broadcast("nodeAdded", node, child);
                });
            }

            function errorHandler(err) {
                ToastService.error(err.message);
            }

            function nodeChosenForLinking(result) {
                if (result.chosenNode) {
                    // There's an existing node that we want to attach.
                    var child = NodeCache.addOrUpdateNode(result.chosenNode);
                    attachChild(child);
                } else {
                    // We have a brand new node that we want to save.
                    var alias = UserService.getActiveAlias();

                    var node = {
                        body: {title: result.newTitle, qualifier:result.newQualifier},
                        children: [],
                        type: result.type
                    };

                    var idToReplace;

                    if (result.insertTarget) {
                        node.children.push(result.insertTarget);
                        console.log(result.insertTarget);

                        node.body.body = BodyTextService.buildLinkCode(
                            result.insertTarget.body.majorVersion.stableId,
                            result.insertTarget.body.title);

                        console.log("Insert target: " + result.insertTarget);

                        // TODO: replace reference(s) to the target from root node's body text, replace child
                        idToReplace = result.insertTarget.body.majorVersion.stableId;
                    }

                    NodeCache.createAndSaveNode(node, alias,
                        function(result) { attachChild(result, idToReplace)},
                        errorHandler);
                }
            }

            $mdDialog.show({
                templateUrl: "partials/link-child.html",
                controller: LinkDialogController,
                controllerAs: "linkCtrl"
            });

            function LinkDialogController($scope, $mdDialog) {

                var self = this;

                var linkableTypes = [];

                if (node.getType() == "assertion") {
                    linkableTypes = ["assertion", "interpretation"];
                } else if (node.getType() == "interpretation") {
                    linkableTypes = ["source", "subject"];
                }

                self.newQualifier = "Original version"; // Default this field.

                self.canLinkTo = function(type) {
                    return linkableTypes.indexOf(type) >= 0;
                };

                self.toggleMakeNew = function() {
                    self.makeNew = !self.makeNew;
                };

                self.getSearchResults = function(query) {

                    return $http.get('/search', {params: {query: query, types:linkableTypes}})
                        .then(function(response){

                            var bodyList = response.data;

                            // Insert a dummy object at the beginning of the list. We know how to deal with this in searchResultSelected.
                            bodyList.splice(0, 0, {createNew: true});

                            return bodyList;
                        });
                };

                self.searchTextChanged = function() {
                    self.newTitle = self.searchQuery;
                };

                self.searchResultSelected = function(bodyNode) {

                    if (!bodyNode) {
                        self.chosenNode = null;
                        self.isResultSelected = false;
                        return;
                    }
                    
                    if (bodyNode.createNew) {
                        self.chosenNode = null;
                        self.isResultSelected = false;
                        self.toggleMakeNew();
                        return;
                    }

                    NodeCache.getLinkChoices(bodyNode.majorVersion.id, function(nodes) {
                        // Although the nodes param is a list, it's actually associated with a single selection in the search
                        // box. That's because search results are rolled up by major version to prevent the perception of
                        // duplicates. In the future, we may allow the user to select from among these nodes.
                        // For now, get the most recent node for linking.
                        sortNodesByVersion(nodes);

                        self.chosenNode = nodes[nodes.length - 1];
                        self.isResultSelected = true;

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

                self.getInsertOptions = function() {
                    var options = [];
                    if (node.getType() === 'assertion' && self.newNodeType === 'assertion') {
                        for (var i = 0; i < node.children.length; i++) {
                            var type = node.children[i].getType();
                            if (type !== 'source' && type != 'subject') {
                                options.push(node.children[i]);
                            }
                        }
                    }

                    return options;
                };

                self.hasInsertOptions = function() {
                    return !!self.getInsertOptions().length;
                };

                self.clearSelection = function() {
                    self.isResultSelected = false;
                    self.chosenNode = null;
                };

                self.select = function() {
                    $mdDialog.cancel();
                    nodeChosenForLinking({chosenNode: self.chosenNode});
                };

                self.createNewNode = function() {
                    $mdDialog.cancel();
                    var insertTarget;
                    if (self.shouldInsertBefore && self.selectedInsertId) {
                        insertTarget = getSelectedInsertNode();
                    }

                    nodeChosenForLinking({
                        newTitle: self.newTitle,
                        newQualifier: self.newQualifier,
                        type: self.newNodeType,
                        insertTarget: insertTarget
                    });
                };

                function getSelectedInsertNode() {
                    if (!self.selectedInsertId) {
                        return null;
                    }

                    var insertOptions = self.getInsertOptions();
                    return $.grep(insertOptions, function(opt) {return opt.stableId === self.selectedInsertId;})[0];
                }



                self.selectedInsertText = function() {
                    var node = getSelectedInsertNode();
                    return node ? node.body.title : "";
                };

                self.cancel = function() {
                    $mdDialog.cancel();
                };
            }

        };
    }



})();