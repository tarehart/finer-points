require('../directives/node-list-directive');
require('../../sass/profile.scss');

(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .controller('ProfileController', ProfileController);

    function ProfileController($scope, $routeParams, $http, UserService, NodeCache) {

        var self = this;
        
        self.subject = {};
        self.stableId = $routeParams.authorStableId;
        self.draftNodes = [];
        self.publishedNodes = [];
        self.scoreLog = [];
        self.chart = {};

        self.friendlyVoteName = function(voteType) {
            if (voteType === 'GREAT') return 'Green';
            if (voteType === 'WEAK') return 'Yellow';
            if (voteType === 'TOUCHE') return 'Purple';
            if (voteType === 'TRASH') return 'Red';
        };

        self.formatDate = function(millis) {
            return moment(millis).format("MMM DD, h:mm a");
        };

        $http.get('/getProfile', {params: {stableId: self.stableId}}).success(function (data) {
            self.subject = data;

            $http.get('/getScoreLog', {params: {stableId: self.stableId}}).success(function (data) {
                self.scoreLog = data;

                self.chart.datasetOverride = [{
                    steppedLine: true
                }];

                self.chart.data = [ calculateScoreOverTime(data, self.subject.nodePoints) ];

                self.chart.options = {
                    maintainAspectRatio: false,
                    legend: {
                        display: false
                    },
                    scales: {
                        xAxes: [{
                            type: 'time',
                            time: {
                                unit: 'day',
                                max: new Date(),
                                tooltipFormat: 'MMM DD, h:mm a',
                                displayFormats: {
                                    day: 'MMM DD',
                                    hour: 'MMM DD, h a',
                                    minute: 'MMM DD, h:mm a',
                                    second: 'MMM DD, h:mm a'
                                }
                            }
                        }]
                    }
                };
            });

        });

        if (UserService.userControlsAlias(self.stableId)) {
            fetchDrafts();
        } else {
            UserService.subscribeSuccessfulLogin($scope, function() {
                if (UserService.userControlsAlias(self.stableId)) {
                    fetchDrafts();
                }
            });
        }

        function fetchDrafts() {
            $http.get('/draftNodes', {params: {authorStableId: self.stableId}}).success(function (data) {
                self.draftNodes.push.apply(self.draftNodes, data); // Push data to nodes
            });
        }

        $http.get('/nodesPublishedByUser', {params: {stableId: self.stableId}}).success(function (data) {
            self.publishedNodes.push.apply(self.publishedNodes, data); // Push data to nodes
        });

        self.discardDraft = function(node) {
            NodeCache.discardDraft(node, function() {
                var idx = self.draftNodes.indexOf(node);
                self.draftNodes.splice(idx, 1);
            });
        };
    }

    function calculateScoreOverTime(scoreLog, finalScore) {

        var scoreSeries = [];
        var runningScore = finalScore;

        for (var i = scoreLog.length - 1; i >= 0; i--) {
            var entry = scoreLog[i];
            scoreSeries[i] = {x: entry.timestamp, y: runningScore, logEntry: entry};
            runningScore -= entry.points;
        }

        scoreSeries[scoreLog.length] = {x: new Date().getTime(), y:finalScore};

        return scoreSeries;
    }

})();