var testApp = angular.module('sampleApp', []);

testApp.controller('SampleController', function ($scope) {
    $scope.fruits = [
        {'name': 'Apple',
            'snippet': 'Red and juicy'},
        {'name': 'Orange',
            'snippet': 'Full of citric acid'},
        {'name': 'Grape',
            'snippet': 'Bite-sized'}
    ];
});