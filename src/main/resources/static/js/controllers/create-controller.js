require('../../sass/create.scss');

(function() {
    'use strict';

    angular
        .module('nodeStandControllers')
        .controller('CreateController', CreateController);

    function CreateController($scope, $routeParams, $http) {

        var self = this;

        self.createOptions = [
            { value: 'source', label: 'Source' },
            { value: 'subject', label: 'Subject' },
            { value: 'interpretation', label: 'Interpretation' },
            { value: 'assertion', label: 'Opinion' }
        ];
    }

})();