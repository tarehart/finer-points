var path = require('path');
var webpack = require('webpack');
var ROOT = path.resolve(__dirname, 'src/main/resources/static');
var DEST = path.resolve(__dirname, 'src/main/resources/static/dist');

var ExtractTextPlugin = require("extract-text-webpack-plugin");

module.exports = {
    entry: {
        "app": ROOT + '/js/local/app.js'
    },
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
                include: ROOT + '/js',
                loader: 'ng-annotate!babel-loader',
                query: {
                    presets: ["es2015"]
                }
            },
            {
                test: /\.scss$/,
                loader: ExtractTextPlugin.extract("style-loader", "css-loader?sourceMap!sass-loader?sourceMap")
            }
        ]
    },
    plugins: [
        new ExtractTextPlugin("[name].bundle.css")
    ],
    debug: true
};