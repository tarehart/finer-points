// Webpack uses this as an entry point to build vendor.bundle.js and vendor.bundle.css.
// Third party libraries which we do not modify and which can alternatively be served from a CDN should be placed here.

require('expose?$!expose?jQuery!jquery');
require('angular');
require('expose?showdown!showdown');
require('angular-route');
require('angular-sanitize');
require('angular-animate');
require('angular-aria');
require('angular-cookies');
require('angular-material');
require('../../../../../node_modules/angular-material/angular-material.css');
require('font-awesome-webpack');
require('particles.js');
require('../../../../../node_modules/bootstrap-markdown/js/bootstrap-markdown');
require('../../../../../node_modules/bootstrap-markdown/css/bootstrap-markdown.min.css');
require('expose?toastr!toastr');
require('../../../../../node_modules/toastr/build/toastr.min.css');