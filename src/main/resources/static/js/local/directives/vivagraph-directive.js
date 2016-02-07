(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .directive('vivaGraph', ['$routeParams', '$modal', 'NodeCache', vivaGraph]);

    function vivaGraph($routeParams, $modal, NodeCache) {
        return {
            restrict: "A",
            scope: {
                rootNode: "="
            },
            templateUrl: "partials/viva-graph.html",
            link: function (scope) {
                setupEventHandlers(scope);
            }
        }
    }



    function setupEventHandlers(scope) {

        var graph = Viva.Graph.graph();

        var vivaContainer = document.getElementById('viva-container');

        var layout = Viva.Graph.Layout.forceDirected(graph, {
            springLength : 40, // default is 30
            springCoeff : 0.0008, // default is 0.0008, higher coeff = more stiffness
            dragCoeff : 0.03, // default is 0.02
            gravity : -1.2 // default is -1.2. More negative is more node repulsion.
        });

        var graphics = Viva.Graph.View.svgGraphics();
        graphics.node(function(node) {
            // The function is called every time renderer needs a ui to display node
            var circle = Viva.Graph.svg('circle', {
                r: node.data.isRoot? 18 : 14,
                stroke: '#000',
                fill: node.data.color
            });

            circle.addEventListener('click', function () {
                layout.pinNode(node, true);
            });

            return circle;

        }).placeNode(function(nodeUI, pos){
            nodeUI.attr('cx', pos.x).attr('cy', pos.y);
        });

        function makeRenderer(forFullscreen) {
            return Viva.Graph.View.renderer(graph, {
                container  : vivaContainer,
                graphics: graphics,
                layout: layout,
                interactive: forFullscreen? 'scroll drag node': 'node' // Only allow zooming and panning in fullscreen
            });
        }

        var renderer = makeRenderer(false);
        renderer.run();

        if (scope.rootNode) {
            addVivaNodesRecursive(scope.rootNode, null, graph, {});
        }

        scope.$on("rootData", function(event, rootNode) {
            addVivaNodesRecursive(rootNode, null, graph, {});
        });

        scope.$on("nodeAdded", function(event, parent, child) {
            addVivaNodesRecursive(child, parent, graph, {});
        });

        scope.$on("edgeAdded", function(event, parent, child) {
            graph.addLink(parent.id, child.id);
        });

        scope.$on("edgeRemoved", function(event, parent, child) {

            var linkToRemove = null;

            graph.forEachLinkedNode(parent.id, function(linkedNode, link){
                if (linkedNode.id === child.id) {
                    linkToRemove = link;
                }
            });

            graph.removeLink(linkToRemove);
        });

        $(document).on('webkitfullscreenchange mozfullscreenchange fullscreenchange MSFullscreenChange', handleFullscreenChange);

        function handleFullscreenChange() {
            var fullscreenEl =
                document.isFullscreen ||
                document.fullscreenElement ||
                document.webkitFullscreenElement ||
                document.mozFullscreenElement ||
                document.msFullscreenElement;

            scope.fullscreen = !!fullscreenEl;

            console.log("scope.fullscreen: " + scope.fullscreen);

            renderer.dispose();
            renderer = makeRenderer(scope.fullscreen); // Get a renderer with the appropriate interactivity
            renderer.run();

            // Give the browser a moment to get to the right dimensions.
            setTimeout(function() {
                renderer.reset(); // This will cause the graph to re-center.
            }, 200);


            scope.$apply();
        }

        scope.goFullscreen = function() {

            var elem = vivaContainer;
            if (elem.requestFullscreen) {
                elem.requestFullscreen();
            } else if (elem.msRequestFullscreen) {
                elem.msRequestFullscreen();
            } else if (elem.mozRequestFullScreen) {
                elem.mozRequestFullScreen();
            } else if (elem.webkitRequestFullscreen) {
                elem.webkitRequestFullscreen();
            }
        }
    }

    function getColor(node) {
        if (node.getType() === 'assertion') {
            return "#8888DD";
        }
        if (node.getType() === 'interpretation') {
            return "#88DD88";
        }
        if (node.getType() === 'source') {
            return "#DD8888";
        }

        return "#000000";
    }


    function addVivaNodesRecursive(node, parent, graph, addedNodes) {

        if (!addedNodes[node.id]) {


            graph.addNode(node.id, {
                node : node,
                color: getColor(node),
                isRoot: !parent
            });

            addedNodes[node.id] = 1;

            for(var i = 0; i < node.children.length; i++) {
                addVivaNodesRecursive(node.children[i], node, graph, addedNodes);
            }
        }

        if (parent) {
            graph.addLink(parent.id, node.id);
        }

    }

})();