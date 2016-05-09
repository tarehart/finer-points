var path = require('path');
var webpack = require('webpack');
var ROOT = path.resolve(__dirname, 'src/main/resources/static');
var SRC = path.resolve(ROOT, 'js');
var DEST = path.resolve(__dirname, 'src/main/resources/static/dist');

var ExtractTextPlugin = require("extract-text-webpack-plugin");

module.exports = {
    entry: [
        'babel-polyfill',
        ROOT + '/sass/main.scss',
        ROOT + '/js/local/app.js'
    ],
    output: {
        path: DEST,
        filename: 'bundle.js',
        publicPath: '/dist/'
    },
    devtool: 'source-map',
    module: {
        loaders: [
            {
                test: /\.js$/,
                include: ROOT + '/js',
                loader: 'babel-loader',
                query: {
                    presets: ["es2015"]
                }
            },
            {
                test: /\.scss$/, loader: ExtractTextPlugin.extract("style-loader", "css-loader?sourceMap!sass-loader?sourceMap")
            }
        ]
    },
    plugins: [
        new ExtractTextPlugin("bundle.css")
    ],
    debug: true
};