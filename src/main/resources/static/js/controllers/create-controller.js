(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .controller('CreateController', CreateController);

    function CreateController($scope, $routeParams, $http) {

        var self = this;

        self.type = 'assertion'; // Default selection

        self.createOptions = [
            { value: 'assertion', label: 'Opinion' },
            { value: 'interpretation', label: 'Interpretation' },
            { value: 'source', label: 'Source' },
            { value: 'subject', label: 'Subject' }
        ];
    }

})();