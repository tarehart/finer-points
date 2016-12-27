
(function () {
    'use strict';

    angular
        .module('nodeStandControllers')
        .service('BodyTextService', BodyTextService);

    function BodyTextService() {
        var self = this;

        self.buildLinkCode = function(id, text) {
            return "{{[" + id + "]" + text + "}}";
        };
    }

})();