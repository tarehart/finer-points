// http://stackoverflow.com/questions/29421409/how-to-load-all-files-in-a-subdirectories-using-webpack-without-require-statemen

function requireAll(r) {
    r.keys().forEach(r);
}

requireAll(require.context('../../cdn/', true, /\.js$/));
requireAll(require.context('../../cdn/', true, /\.css$/));