
(function () {
    'use strict';

    angular
        .module('nodeStandControllers')
        .service('ToastService', ToastService);

    function ToastService() {
        var self = this;

        toastr.options = {
            closeButton: true,
            positionClass: 'toast-bottom-right',
            timeOut: 4000
        };

        self.success = function(message) {
            toastr.success(message);
        };

        self.info = function(message) {
            toastr.info(message);
        };

        self.error = function (message) {
            toastr.error(message);
        };
    }

})();