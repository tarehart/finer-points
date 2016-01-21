var gulp = require('gulp'),
    concat = require('gulp-concat'),
    uglify = require('gulp-uglify'),
    sourcemaps = require('gulp-sourcemaps');

var root = './src/main/resources/static/';

gulp.task('css-local', function() {
    return gulp.src(root + 'css/local/**/*.css')
        .pipe(concat('combined.css'))
        .pipe(gulp.dest(root + 'dist/'));
});

// This provides a file useful for offline development, e.g. on an airplane
gulp.task('css-cdn', function() {
    return gulp.src(root + 'css/cdn/**/*.css')
        .pipe(concat('combined-cdn.css'))
        .pipe(gulp.dest(root + 'dist/'));
});

gulp.task('js-local', function() {
    return gulp.src([
        root + 'js/local/app.js',
        root + 'js/local/showdown-node-stand.js',
        root + 'js/local/**/*.js'])
        .pipe(sourcemaps.init())
            .pipe(concat('combined.js'))
            .pipe(uglify())
        .pipe(sourcemaps.write('./'))
        .pipe(gulp.dest(root + 'dist/'));
});

gulp.task('js-cdn', function() {
    return gulp.src([
        root + 'js/cdn/jquery*',
        root + 'js/cdn/angular.min.js',
        root + 'js/cdn/angular-route.min.js',
        root + 'js/cdn/angular-sanitize.min.js',
        root + 'js/cdn/bootstrap.min.js',
        root + 'js/cdn/ui-bootstrap-tpls-0.14.3.min.js',
        root + 'js/cdn/showdown.js',
        root + 'js/cdn/**/*.js'])
        .pipe(concat('combined-cdn.js'))
        .pipe(gulp.dest(root + 'dist/'));
});

gulp.task('build', ['css-local', 'css-cdn', 'js-local', 'js-cdn']);

gulp.task('watch', ['build'], function () {
    gulp.watch(root + 'css/local/**/*.css', ['css-local']);
    gulp.watch(root + 'css/cdn/**/*.js', ['css-cdn']);
    gulp.watch(root + 'js/local/**/*.js', ['js-local']);
    gulp.watch(root + 'js/cdn/**/*.js', ['js-cdn']);
});