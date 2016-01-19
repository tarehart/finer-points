(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .directive('nodeComments', ['$http', nodeComments]);

    function nodeComments($http) {
        return {
            restrict: "A",
            scope: {
                node: "="
            },
            templateUrl: "partials/comments.html",
            link: function (scope) {
                initializeComments(scope, $http);
            }
        }
    }

    function initializeComments($scope, $http) {

        fetchComments($http, $scope.node);

        $scope.newComment = {};

        $scope.setComment = function(comment, text) {
            comment.body = text;
        };

        $scope.saveComment = function(comment) {
            toastr.info("Saving comment with text: " + comment.body);

            var parentId = $scope.node.body.id;

            saveComment($http, comment.body, parentId, function(comment) {
                $scope.node.comments = $scope.node.comments || [];
                $scope.node.comments.push(comment);
                $scope.editingTopLevel = false;
            }, function(err) {
                toastr.error(err.message);
            });
        };

        $scope.beginEditingTopLevel = function() {
            $scope.editingTopLevel = true;
        };

        $scope.hasComment = function(node) {
            return node.comments && node.comments.length;
        };
    }

    function saveComment($http, body, parentId, successCallback, errorCallback) {

        $http.post('/saveComment',
            {
                body: body,
                parentId: parentId
            })
            .success(function (data) {
                if (successCallback) {
                    successCallback(data);
                }
            })
            .error(function(err) {
                if (errorCallback) {
                    errorCallback(err);
                }
            });
    }

    function fetchComments($http, node, force) {

        if (node.comments && !force) {
            return;
        }

        $http.get('/comments', {params: {"id": node.id}}).success(function (data) {

            // we're dealing with comments here!
            // TODO: make sure we're aggregating comments across everything within the major version and
            // indicating which minor version they are attributed to.
            var commentables = {};
            for (var i = 0; i < data.nodes.length; i++) {
                var commentableId = data.nodes[i].id;
                if (commentableId == node.body.id) {

                    // This line is tricky. The node.id does NOT match returnedId. This will ultimately have the affect
                    // of attributing comments to the ArgumentNode when really they belong to the ArgumentBod(ies).
                    commentables[commentableId] = node;
                } else {
                    commentables[commentableId] = data.nodes[i];
                }
            }

            $.each(commentables, function(id, commentable) {
                commentable.comments = [];
                var edges = data.edges.filter(function (el) {
                    return el.end == id; // Use end here because comments point to their parents, so we want to match the tip of the arrow
                });

                for (var j = 0; j < edges.length; j++) {
                    commentable.comments.push(commentables[edges[j].start]);
                }
            });

        });
    }

})();