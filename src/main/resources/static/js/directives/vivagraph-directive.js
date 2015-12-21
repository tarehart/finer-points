(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .directive('vivaGraph', ['$routeParams', '$modal', 'NodeCache', vivaGraph]);

    function vivaGraph($routeParams, $modal, NodeCache) {
        return {
            restrict: "A",
            scope: { },
            templateUrl: "partials/viva-graph.html",
            link: function (scope) {

                scope.$on("rootData", function(event, rootNode) {
                    initializeGraph(rootNode);
                });
            }
        }
    }

    function initializeGraph(rootNode) {

        var graph = Viva.Graph.graph();

        var addedNodes = {};

        addVivaNodesRecursive(rootNode, null);

        function addVivaNodesRecursive(node, parent) {

            if (!addedNodes[node.id]) {


                graph.addNode(node.id, {
                    node : node,
                    color: getColor(node),
                    isRoot: !parent
                });

                addedNodes[node.id] = 1;

                for(var i = 0; i < node.children.length; i++) {
                    addVivaNodesRecursive(node.children[i], node);
                }
            }

            if (parent) {
                graph.addLink(parent.id, node.id);
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

        var layout = Viva.Graph.Layout.forceDirected(graph, {
            springLength : 40,
            springCoeff : 0.0008,
            dragCoeff : 0.02,
            gravity : -1.2
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
        })
        .placeNode(function(nodeUI, pos){
            nodeUI.attr('cx', pos.x).attr('cy', pos.y);
        });


        var renderer = Viva.Graph.View.renderer(graph, {
            container  : document.getElementById('viva-container'),
            graphics: graphics,
            layout: layout,
            interactive: 'node drag'
        });
        renderer.run();
        
        // Currently, this just appends a <svg> tag to the body willy-nilly. The nodes are not visible because they are
        // outside the svg area of 150x300 and overflow is hidden. It's possible to pan the canvas by dragging to the
        // left to make them visible.

        // Need a way to specify a rendering container that's already on the page.
    }

})();