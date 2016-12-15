require('../services/toast-service');

(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .directive('commentVoteButton', voteButton)
        .controller('CommentVoteController', CommentVoteController);

    function voteButton() {
        return {
            restrict: "A",
            scope: {
                comment: "="
            },
            template: require("../../partials/comment-vote-button.html"),
            controller: 'CommentVoteController'
        }
    }

    function CommentVoteController($scope, $http, UserService, ToastService) {


        var user = UserService.getUser();
        var comment = $scope.comment;
        
        if (user == null) {
            UserService.subscribeSuccessfulLogin($scope, function() {
                user = UserService.getUser();
                getUserVote();
            });
        } else {
            getUserVote();
        }

        function getUserVote() {
            if (user && user.commentVotes) {
                $scope.userVote = user.commentVotes[comment.id];
                return $scope.userVote;
            }
            return null;
        }

        function setUserVote(vote) {
            if (user) {
                if (!user.commentVotes) {
                    user.commentVotes = {};
                }
                $scope.userVote = user.commentVotes[comment.id] = vote;
            }
        }

        $scope.voteClicked = function(voteType) {
            if (!user) {
                ToastService.error("Must be logged in to vote!");
                return;
            }

            var currentVote = getUserVote();
            if (voteType === currentVote) {
                revokeVote(currentVote);
            } else {
                registerVote(voteType, currentVote);
            }
        };

        // voteDelta is only allowed to be 1 or -1.
        function registerVote(voteDelta, currentVote) {
            $http.post('/voteComment',
                {
                    commentId: comment.id,
                    isUpvote: voteDelta > 0
                })
                .success(function (data) {
                    if (currentVote) {
                        // User must be reversing their vote, so there's a two point swing.
                        comment.score += voteDelta * 2;
                    } else {
                        comment.score += voteDelta;
                    }
                    setUserVote(voteDelta);
                })
                .error(function(err) {
                    ToastService.error(err.message);
                });
        }

        function revokeVote(currentVote) {
            $http.post('/unvoteComment',
                {
                    commentId: comment.id
                })
                .success(function (data) {
                    comment.score -= currentVote;
                    setUserVote(null);
                })
                .error(function(err) {
                    ToastService.error(err.message);
                });
        }

    }

})();