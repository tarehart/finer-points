(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .directive('vivaGraph', ['$routeParams', '$modal', 'NodeCache', vivaGraph]);

    function vivaGraph($routeParams, $modal, NodeCache) {
        return {
            restrict: "A",
            scope: {
                starterNode: "="
            },
            templateUrl: "partials/viva-graph.html",
            link: function (scope) {
                initializeGraph(scope, $routeParams, $modal, NodeCache);
            }
        }
    }

    function initializeGraph($scope, $routeParams, $modal, NodeCache) {

        var graph = Viva.Graph.graph();
        graph.addLink(1, 2);

        var renderer = Viva.Graph.View.renderer(graph);
        renderer.run();
        
        // Currently, this just appends a <svg> tag to the body willy-nilly. The nodes are not visible because they are
        // outside the svg area of 150x300 and overflow is hidden. It's possible to pan the canvas by dragging to the
        // left to make them visible.

        // Need a way to specify a rendering container that's already on the page.
    }

})();