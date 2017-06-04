require('./node-factory');


(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .factory('NodeCache', NodeCache);

    function NodeCache($http, Node) {

        var cache = {};

        cache.nodes = {};
        cache.edges = [];

        var DRAFT_ID = Node.DRAFT_ID;

        cache.get = function(id) {
            return cache.nodes[id];
        };

        cache.getByStableId = function(stableId) {
            var foundNode = null;

            $.each(cache.nodes, function(id, node) {
                if (node && node.stableId === stableId) {
                    foundNode = node;
                    return false; // break out of the loop
                }
            });

            return foundNode;
        };

        cache.getOrCreateNode = function(id) {
            if (cache.get(id)) {
                return cache.get(id);
            }

            var node = new Node();
            node.id = id;
            insertNode(node);
            return node;
        };

        cache.getOrCreateDraftNode = function(nodeType) {

            if (cache.nodes[DRAFT_ID]) {
                return cache.nodes[DRAFT_ID];
            }

            var node = makeDraftNode(nodeType);
            insertNode(node);
            return node;
        };

        function makeDraftNode(nodeType) {
            var node = new Node();
            node.id = Node.DRAFT_ID;
            node.inEditMode = true;
            node.type = nodeType;
            node.body = {draft: true, qualifier: "Original version"};
            return node;
        }

        cache.createNodeWithSupport = function(supportingNode, alias, successCallback) {

            var supportingType = supportingNode.type;
            var newType = supportingType === 'source' || supportingType === 'subject' ? 'interpretation' : 'assertion';

            var node = makeDraftNode(newType);

            node.body.body = "{{[" + supportingNode.body.majorVersion.stableId + "]" + supportingNode.body.title + "}}";

            node.addChild(supportingNode);

            saveNewNode(node, alias, function(data) {
                // Next time the draft node is requested, a fresh blank one should be built.
                cache.nodes[DRAFT_ID] = null;
                if (successCallback) {
                    successCallback(data);
                }
            });
        };

        // This refers to whether this node is completely unsaved and does not exist in the database,
        // NOT whether it has been published (which is what node.isDraft determines).
        cache.isBlankSlateNode = function(node) {
            return node === cache.get(DRAFT_ID);
        };

        cache.saveBlankSlateNode = function(alias, successCallback, errorCallback) {
            var node = cache.get(DRAFT_ID);
            node.body.title = node.body.title || 'Untitled';
            saveNewNode(node, alias, function(data) {
                // Next time the draft node is requested, a fresh blank one should be built.
                cache.nodes[DRAFT_ID] = null;
                if (successCallback) {
                    successCallback(data); // This callback probably ought to change the URL to incorporate the new id.
                }
            }, errorCallback);
        };

        cache.saveSketchNode = function(node, alias, successCallback, errorCallback) {

            saveNewNode(node, alias, successCallback, errorCallback);
        };

        cache.createAndSaveNode = function(nodeSkeleton, alias, successCallback, errorCallback) {

            var node = new Node();
            node.assimilateData(nodeSkeleton);

            saveNewNode(node, alias, successCallback, errorCallback);
        };

        function saveNewNode(node, alias, successCallback, errorCallback) {
            if (node.getType() == "assertion") {
                saveNewAssertion(node, alias, successCallback, errorCallback);
            } else if (node.getType() == "interpretation") {
                saveNewInterpretation(node, alias, successCallback, errorCallback);
            } else if (node.getType() == "source") {
                saveNewSource(node, alias, successCallback, errorCallback);
            } else if (node.getType() == "subject") {
                saveNewSubject(node, alias, successCallback, errorCallback);
            }
        }

        function saveNewAssertion(node, alias, successCallback, errorCallback) {

            var links = node.children.map(function(child) {
                return child.id;
            });

            $http.post('/createAssertion',
                {
                    title: node.body.title,
                    qualifier: node.body.qualifier,
                    body: node.body.body,
                    links: links,
                    authorStableId: alias.stableId
                })
                .success(function (data) {
                    node.assimilateData(data);
                    insertNode(node);

                    if (successCallback) {
                        successCallback(node); // This callback probably ought to change the URL to incorporate the new id.
                    }
                })
                .error(function(err) {
                    if (errorCallback) {
                        errorCallback(err);
                    }
                });
        }

        function saveNewInterpretation(node, alias, successCallback, errorCallback) {

            var leafId = null;
            if (node.children && node.children.length) {
                leafId = node.children[0].id;
            }

            $http.post('/createInterpretation',
                {
                    title: node.body.title,
                    qualifier: node.body.qualifier,
                    body: node.body.body,
                    leafId: leafId,
                    authorStableId: alias.stableId
                })
                .success(function (data) {
                    node.assimilateData(data);
                    insertNode(node);

                    if (successCallback) {
                        successCallback(node); // This callback probably ought to change the URL to incorporate the new id.
                    }
                })
                .error(function(err) {
                    if (errorCallback) {
                        errorCallback(err);
                    }
                });
        }

        function saveNewSource(node, alias, successCallback, errorCallback) {

            $http.post('/createSource',
                {
                    title: node.body.title,
                    qualifier: node.body.qualifier,
                    url: node.body.url,
                    authorStableId: alias.stableId
                })
                .success(function (data) {
                    node.assimilateData(data);
                    insertNode(node);

                    if (successCallback) {
                        successCallback(node); // This callback probably ought to change the URL to incorporate the new id.
                    }
                })
                .error(function(err) {
                    if (errorCallback) {
                        errorCallback(err);
                    }
                });
        }

        function saveNewSubject(node, alias, successCallback, errorCallback) {

            $http.post('/createSubject',
                {
                    title: node.body.title,
                    qualifier: node.body.qualifier,
                    url: node.body.url,
                    authorStableId: alias.stableId
                })
                .success(function (data) {
                    node.assimilateData(data);
                    insertNode(node);

                    if (successCallback) {
                        successCallback(node); // This callback probably ought to change the URL to incorporate the new id.
                    }
                })
                .error(function(err) {
                    if (errorCallback) {
                        errorCallback(err);
                    }
                });
        }

        cache.saveNodeEdit = function(node, successCallback, errorCallback) {
            if (node.getType() == "assertion") {
                saveAssertionEdit(node, successCallback, errorCallback);
            } else if (node.getType() == "interpretation") {
                saveInterpretationEdit(node, successCallback, errorCallback);
            } else if (node.getType() == "source") {
                saveSourceEdit(node, successCallback, errorCallback);
            } else if (node.getType() == "subject") {
                saveSubjectEdit(node, successCallback, errorCallback);
            } else {
                console.log("Can't edit node because its type is unknown!");
            }
        };

        function handleDraftCreation(draftResponse) {
            var draftNode = cache.addOrUpdateNode(draftResponse.editedNode);

            if (draftResponse.graph) {
                inductQuickGraph(draftResponse.graph);
            }

            return draftNode;
        }

        cache.makeDraft = function(node, alias, successCallback, errorCallback) {
            $http.post('/makeDraft',
                {
                    nodeId: node.id,
                    authorStableId: alias.stableId
                })
                .success(function (data) {
                    var draftNode = handleDraftCreation(data);

                    if (successCallback) {
                        successCallback(draftNode, data); // This callback probably ought to change the URL to incorporate the new id.
                    }
                })
                .error(function(err) {
                    if (errorCallback) {
                        errorCallback(err);
                    }
                });
        };

        function handleNodeEdit(editedNode) {
            return cache.addOrUpdateNode(editedNode);
        }

        function saveAssertionEdit(node, successCallback, errorCallback) {

            var links = node.children.map(function(child) {
                return child.id;
            });

            // Iff the node is not a draft, this operation will produce a new draft node with a new id.
            $http.post('/editAssertion',
                {
                    nodeId: node.id,
                    title: node.body.title,
                    qualifier: node.body.qualifier,
                    body: node.body.body,
                    links: links
                })
                .success(function (data) {
                    var editedNode = handleNodeEdit(data);

                    if (successCallback) {
                        successCallback(editedNode);
                    }
                })
                .error(function(err) {
                    if (errorCallback) {
                        errorCallback(err);
                    }
                });
        }

        function saveInterpretationEdit(node, successCallback, errorCallback) {

            var sourceId = null;
            if (node.children && node.children.length) {
                sourceId = node.children[0].id;
            }

            // Iff the node is not a draft, this operation will produce a new draft node with a new id.
            $http.post('/editInterpretation',
                {
                    nodeId: node.id,
                    title: node.body.title,
                    qualifier: node.body.qualifier,
                    body: node.body.body,
                    sourceId: sourceId
                })
                .success(function (data) {
                    var editedNode = handleNodeEdit(data);

                    if (successCallback) {
                        successCallback(editedNode);
                    }
                })
                .error(function(err) {
                    if (errorCallback) {
                        errorCallback(err);
                    }
                });
        }

        function saveSourceEdit(node, successCallback, errorCallback) {

            // Iff the node is not a draft, this operation will produce a new draft node with a new id.
            $http.post('/editSource',
                {
                    nodeId: node.id,
                    title: node.body.title,
                    qualifier: node.body.qualifier,
                    url: node.body.url
                })
                .success(function (data) {
                    var editedNode = handleNodeEdit(data);

                    if (successCallback) {
                        successCallback(editedNode);
                    }
                })
                .error(function(err) {
                    if (errorCallback) {
                        errorCallback(err);
                    }
                });
        }

        function saveSubjectEdit(node, successCallback, errorCallback) {

            // Iff the node is not a draft, this operation will produce a new draft node with a new id.
            $http.post('/editSubject',
                {
                    nodeId: node.id,
                    title: node.body.title,
                    qualifier: node.body.qualifier,
                    url: node.body.url
                })
                .success(function (data) {
                    var editedNode = handleNodeEdit(data);

                    if (successCallback) {
                        successCallback(editedNode);
                    }
                })
                .error(function(err) {
                    if (errorCallback) {
                        errorCallback(err);
                    }
                });
        }

        cache.publishNode = function(node, successCallback, errorCallback) {
            $http.post('/publishNode',
                {
                    nodeId: node.id
                })
                .success(function (data) {
                    inductQuickGraph(data);

                    if (data.rootId != node.id) {
                        // The publish has resulted in the draft node being replaced, and probably destroyed.
                        // In any case, we should forget about the draft node. Remove it from the cache.

                        forgetNode(node.id);
                    }

                    if (successCallback) {
                        successCallback(data); // This callback probably ought to change the URL to incorporate the new id.
                    }
                })
                .error(function(err) {
                    if (errorCallback) {
                        errorCallback(err);
                    }
                });
        };

        function insertNode(node) {
            cache.nodes[node.id] = node;
        }

        cache.addOrUpdateNode = function(data) {
            var cachedNode = cache.get(data.id);
            if (cachedNode) {
                // Node is already in the cache, so perform updates
                cachedNode.assimilateData(data);

            } else {
                // node must be created and added to the cache
                var node = new Node();
                node.assimilateData(data);
                insertNode(node);
            }

            return cache.get(data.id);
        };

        // nodes is a list of objects that have ids. Returns a map of the added nodes for further processing.
        cache.addNodesUnlinked = function(nodeData) {
            var addedNodes = {};
            for (var i = 0; i < nodeData.length; i++) {
                var node = cache.addOrUpdateNode(nodeData[i]);
                addedNodes[node.id] = node;
            }
            return addedNodes;
        };

        cache.discardDraft = function(node, successCallback) {
            $http.post('/discardDraft', node.stableId).success(function () {

                forgetNode(node.id);

                if (successCallback) {
                    successCallback();
                }
            });
        };

        function forgetNode(nodeId) {
            var nodeToForget = cache.get(nodeId);
            if (!nodeToForget) {
                return;
            }

            $.each(cache, function(id, node) {
                var idx;
                if (node.children) {
                    idx = node.children.indexOf(nodeToForget);
                    if (idx > -1) {
                        // Remove nodeToForget from the list of children.
                        node.children.splice(idx, 1);
                    }
                }

                if (node.consumers) {
                    idx = node.consumers.indexOf(nodeToForget);
                    if (idx > -1) {
                        // Remove nodeToForget from the list of children.
                        node.consumers.splice(idx, 1);
                    }
                }
            });

            delete cache[nodeId];
        }

        function populateChildren(nodeMap, edgeList) {
            Object.keys(nodeMap).forEach(function (id) {
                var node = cache.get(id);
                node.children = [];
                var edges = edgeList.filter(function (el) {
                    return el.start.toString() === id;
                });

                for (var j = 0; j < edges.length; j++) {
                    var child = cache.get(edges[j].end);
                    if (child === undefined) {
                        console.error("Could not locate child in cache with id of " + edges[j].end);
                    } else {
                        node.addChild(child);
                    }
                }

                node.sortChildren();

                if (isInfinite(node)) {
                    // That's illegal! Remove those children.
                    node.children = [];
                    console.log("Took away the children of " + node.stableId + " because they create a cycle!");
                }
            });
        }

        cache.hasCycle = function(nodeMap) {
            var safeNodes = {};

            for (var id in nodeMap) {
                if (nodeMap.hasOwnProperty(id)) {
                    if (isInfinite(nodeMap[id], safeNodes)) {
                        return true;
                    }
                }
            }
            return false;
        };

        function isInfinite(node, safeNodes) {

            var stackSet = {};
            safeNodes = safeNodes || {};

            function isInfiniteHelper(node) {
                if (safeNodes[node.id]) {
                    return false;
                }
                if (stackSet[node.id]) {
                    return true;
                }
                stackSet[node.id] = 1;

                for (var i = 0; i < node.children.length; i++) {
                    if (isInfiniteHelper(node.children[i])) {
                        return true;
                    }
                }

                safeNodes[node.id] = 1;
                delete stackSet[node.id];
                return false;
            }

            return isInfiniteHelper(node);
        }

        cache.fetchGraphForId = function(stableId, successCallback, errorCallback, force) {

            var rootNode = cache.getByStableId(stableId);

            if (rootNode && rootNode.hasFullGraph && !force) {
                if (successCallback) {
                    successCallback(rootNode);
                }
            } else {
                $http.get('/graph', {params: {"rootStableId": stableId}}).success(function (data) {

                    inductQuickGraph(data);
                    rootNode = cache.getByStableId(stableId);
                    rootNode.hasFullGraph = true;
                    if (successCallback) {
                        successCallback(rootNode);
                    }
                }).error(function(err) {
                    if (errorCallback) {
                        errorCallback(err);
                    }
                });
            }
        };

        function inductQuickGraph(quickGraphResponse) {
            var addedNodes = cache.addNodesUnlinked(quickGraphResponse.nodes);

            var consumersMap = cache.addNodesUnlinked(quickGraphResponse.consumers);
            cache.get(quickGraphResponse.rootId).consumers = $.map(consumersMap, function(n) {return n;});

            populateChildren(addedNodes, quickGraphResponse.edges);

            console.log(cache.get(quickGraphResponse.rootId));
        }

        cache.getFullDetail = function(nodeStableId) {

            $http.get('/fullDetail', {params: {stableId: nodeStableId}}).success(function (node) {

                // returns the current ArgumentNode in full detail
                cache.addOrUpdateNode(node);
            });
        };

        cache.getLinkChoices = function(majorVersionId, successCallback, errorCallback) {

            $http.get('/nodesInMajorVersion', {params: {"majorVersionId": majorVersionId}}).success(function (nodeList) {
                var nodes = [];
                $.each(nodeList, function(index, node) {
                    nodes.push(cache.addOrUpdateNode(node));
                });
                successCallback(nodes);

            }).error(function(err) {
                if (errorCallback) {
                    errorCallback(err);
                }
            });
        };

        return cache;

    }

})();