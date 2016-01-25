(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .directive('nodeComments', ['$http', 'UserService', nodeComments]);

    function nodeComments($http, UserService) {
        return {
            restrict: "A",
            scope: {
                node: "="
            },
            templateUrl: "partials/comments.html",
            link: function (scope) {
                initializeComments(scope, $http, UserService);
            }
        }
    }

    function initializeComments($scope, $http, UserService) {

        fetchComments($http, $scope.node);

        $scope.newComment = {};

        $scope.setComment = function(comment, text) {
            comment.body = text;
        };

        // saves a top-level comment
        $scope.saveComment = function(comment) {

            createComment($http, comment.body, $scope.node.body.id, function(comment) {
                $scope.node.comments = $scope.node.comments || [];
                $scope.node.comments.push(comment);
                $scope.editingTopLevel = false;
            }, function(err) {
                toastr.error(err.message);
            });
        };

        $scope.beginEditingTopLevel = function() {

            if (!UserService.getUser()) {
                toastr.error("Must be signed in to comment!");
                return;
            }

            $scope.editingTopLevel = true;
            $scope.newComment.body = null;
        };

        $scope.beginWritingReply = function (comment) {

            if (!UserService.getUser()) {
                toastr.error("Must be signed in to comment!");
                return;
            }

            comment.writingReply = true;
            comment.newReply = comment.newReply || {};
            comment.newReply.body = null;
        };

        $scope.setReplyText = function(comment, text) {
            comment.newReply.body = text;
        };

        $scope.setEditText = function(comment, text) {
            comment.body = text;
        };

        $scope.saveReply = function(comment) {

            createComment($http, comment.newReply.body, comment.id, function(reply) {
                comment.comments = comment.comments || [];
                comment.comments.push(reply);
                comment.writingReply = false;
            }, function(err) {
                toastr.error(err.message);
            });
        };

        $scope.saveEdit = function(comment) {

            editComment($http, comment, function(saved) {
                comment.dateEdited = saved.dateEdited;
                comment.editing = false;
            }, function(err) {
                toastr.error(err.message);
            });
        };

        $scope.allowedToEdit = function(comment) {
            var user = UserService.getUser();

            return user && comment.author.id == user.id;
        };

        $scope.hasComment = function(node) {
            return node.comments && node.comments.length;
        };

        $scope.toggleComment = function(comment) {
            comment.hideComment = !comment.hideComment;
        }
    }

    function createComment($http, body, parentId, successCallback, errorCallback) {

        $http.post('/createComment',
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

    function editComment($http, comment, successCallback, errorCallback) {

        $http.post('/editComment',
            {
                body: comment.body,
                commentId: comment.id
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
                    var child = commentables[edges[j].start];
                    commentable.comments.push(child);
                    child.parent = commentable;
                }
            });

        });
    }

})();