var path = require('path');
var webpack = require('webpack');
var ROOT = path.resolve(__dirname, 'src/main/resources/static');
var DEST = path.resolve(__dirname, 'src/main/resources/static/dist');

var ExtractTextPlugin = require("extract-text-webpack-plugin");

module.exports = {
    entry: {
        "app": ROOT + '/js/local/app.js',
        "vendor": ['jquery', 'angular', 'angular-route', 'angular-sanitize', 'angular-animate', 'angular-aria',
            'angular-material', 'font-awesome-webpack', 'showdown', 'particles.js',
            './node_modules/bootstrap-markdown/js/bootstrap-markdown.js',
            './node_modules/bootstrap-markdown/css/bootstrap-markdown.min.css']
    },
    // externals: {
    //     'angular': 'angular',
    //     'particles.js': 'particlesJS',
    //     'jquery': 'jQuery'
    // },
    output: {
        path: DEST,
        filename: '[name].bundle.js',
        publicPath: '/dist/'
    },
    devtool: 'source-map',
    module: {
        loaders: [
            {
                test: /\.js$/,
                exclude: /node_modules/,
                loader: 'ng-annotate!babel-loader?presets[]=es2015'
            },
            {
                test: /\.scss$/,
                loader: ExtractTextPlugin.extract("style-loader", "css-loader?sourceMap!sass-loader?sourceMap")
            },
            {
                test: /\.css$/,
                loader: ExtractTextPlugin.extract("style-loader", "css-loader")
            },
            { test: /\.svg(\?v=\d+\.\d+\.\d+)?$/, loader: 'url?limit=10000&mimetype=image/svg+xml&name=fonts/[name].[ext]' },
            { test: /\.woff(\?v=\d+\.\d+\.\d+)?$/, loader: 'url?limit=10000&mimetype=application/font-woff&name=fonts/[name].[ext]' },
            { test: /\.woff2(\?v=\d+\.\d+\.\d+)?$/, loader: 'url?limit=10000&mimetype=application/font-woff2&name=fonts/[name].[ext]' },
            { test: /\.[ot]tf(\?v=\d+\.\d+\.\d+)?$/, loader: 'url?limit=10000&mimetype=application/octet-stream&name=fonts/[name].[ext]' },
            { test: /\.eot(\?v=\d+\.\d+\.\d+)?$/, loader: 'url?limit=10000&mimetype=application/vnd.ms-fontobject&name=fonts/[name].[ext]' }
        ]
    },
    plugins: [
        new ExtractTextPlugin("[name].bundle.css"),
        new webpack.ProvidePlugin({
            $: "jquery",
            jQuery: "jquery",
            angular: "angular"
        })
    ],
    debug: true
};