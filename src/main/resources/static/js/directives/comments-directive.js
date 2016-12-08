require('./comment-vote-directive');
require('../../sass/comments.scss');
require('../services/toast-service');

(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .directive('nodeComments', nodeComments)
        .controller('CommentController', CommentController);
        

    function nodeComments() {
        return {
            restrict: "A",
            scope: {
                node: "="
            },
            templateUrl: "partials/comments.html",
            controller: "CommentController",
            controllerAs: "commCtrl"
        }
    }

    function CommentController($scope, $http, UserService, ToastService) {

        var ctrl = this;
        ctrl.node = $scope.node;

        ctrl.commentSort = 'score';

        fetchComments($http, ctrl.node);

        ctrl.newComment = {};

        ctrl.setComment = function(comment, text) {
            comment.body = text;
        };

        // saves a top-level comment
        ctrl.saveComment = function() {
            createComment($http, ctrl.newComment.body, ctrl.node.body.majorVersion.id, UserService.getActiveAlias(), function(comment) {
                ctrl.node.comments = ctrl.node.comments || [];
                ctrl.node.comments.push(comment);
                ctrl.editingTopLevel = false;
            }, function(err) {
                ToastService.error(err.message);
            });
        };

        ctrl.beginEditingTopLevel = function() {

            if (!UserService.getUser()) {
                ToastService.error("Must be signed in to comment!");
                return;
            }

            ctrl.editingTopLevel = true;
            ctrl.newComment.body = null;
        };

        ctrl.cancelTopLevelComment = function() {
            ctrl.editingTopLevel = false;
            ctrl.newComment.body = null;
            return;
        };

        ctrl.beginWritingReply = function (comment) {

            if (!UserService.getUser()) {
                ToastService.error("Must be signed in to comment!");
                return;
            }

            comment.writingReply = true;
            comment.newReply = comment.newReply || {};
            comment.newReply.body = null;
        };

        ctrl.setReplyText = function(comment, text) {
            comment.newReply.body = text;
        };

        ctrl.setEditText = function(comment, text) {
            comment.editedBody = text;
        };

        ctrl.saveReply = function(comment) {

            createComment($http, comment.newReply.body, comment.id, UserService.getActiveAlias(), function(reply) {
                comment.comments = comment.comments || [];
                comment.comments.push(reply);
                comment.writingReply = false;
            }, function(err) {
                ToastService.error(err.message);
            });
        };

        ctrl.cancelReply = function(comment) {
            comment.writingReply = false;
        };

        ctrl.saveEdit = function(comment) {

            editComment($http, comment, function(saved) {
                comment.dateEdited = saved.dateEdited;
                comment.body = saved.body;
                comment.editedBody = comment.body;
                comment.editing = false;
            }, function(err) {
                ToastService.error(err.message);
            });
        };

        ctrl.cancelEdit = function(comment) {
            comment.editing = false;
            // TODO: may need to reset content
        };

        ctrl.allowedToEdit = function(comment) {
            var user = UserService.getUser();

            return user && comment.author.id == user.id;
        };

        ctrl.hasComment = function(node) {
            return node.comments && node.comments.length;
        };

        ctrl.toggleComment = function(comment) {
            comment.hideComment = !comment.hideComment;
        }
    }

    function createComment($http, body, parentId, alias, successCallback, errorCallback) {

        $http.post('/createComment',
            {
                body: body,
                parentId: parentId,
                authorStableId: alias.stableId
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
                body: comment.editedBody,
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

        $http.get('/comments', {params: {"id": node.body.majorVersion.id}}).success(function (data) {
            
            var commentables = {};
            for (var i = 0; i < data.nodes.length; i++) {
                var commentableId = data.nodes[i].id;
                if (commentableId == node.body.majorVersion.id) {

                    // This line is tricky. The node.id does NOT match returnedId. This will ultimately have the affect
                    // of attributing comments to the ArgumentNode when really they belong to the MajorVersion.
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