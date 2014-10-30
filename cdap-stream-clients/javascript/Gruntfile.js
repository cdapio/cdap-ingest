module.exports = function (grunt) {
    'use strict';
    // Project configuration
    grunt.initConfig({
        // Metadata
        pkg: grunt.file.readJSON('package.json'),
        // Task configuration
        // Browser side tests
        mocha: {
            browser: {
                src: ['test/unit-browser.html'],
                options: {
                    run: true,
                    reporter: 'Nyan',
                },
            },
        },
        // Node.JS side tests
        mochaTest: {
            nodejs: {
                src: ['test/unit-node.js']
            },
        },
        clean: [
            'test/integration/node_modules/<%= pkg.name %>',
            'dist/nodejs/<%= pkg.name %>',
            'dist/browser'
        ],
        concat: {
            browser_dist: {
                src: ['src/promise.js', 'src/utils.js', 'src/request-browser.js', 'src/serviceconnector.js', 'src/streamwriter.js', 'src/streamclient.js'],
                dest: 'tmp/browser/<%= pkg.name %>.js'
            },
        },
        copy: {
            integration_tests_node_src: {
                expand: true,
                cwd: 'src',
                src: ['promise.js', 'utils.js', 'request-node.js', 'serviceconnector.js', 'streamwriter.js', 'streamclient.js'],
                dest: 'test/integration/node_modules/<%= pkg.name %>/'
            },
            integration_tests_node_package: {
                expand: true,
                cwd: 'src/nodejs',
                src: ['*.json', '*.js'],
                dest: 'test/integration/node_modules/<%= pkg.name %>/'
            },
            integration_tests_browser: {
                expand: true,
                cwd: 'dist/browser',
                src: '<%= pkg.name %>.min.js',
                dest: 'test/integration'
            },
            nodejs_package: {
                expand: true,
                cwd: 'src/nodejs/',
                src: ['*.json', '*.js'],
                dest: 'dist/nodejs/<%= pkg.name %>/'
            },
            nodejs_src: {
                expand: true,
                cwd: 'src',
                src: ['promise.js', 'utils.js', 'request-node.js', 'serviceconnector.js', 'streamwriter.js', 'streamclient.js'],
                dest: 'dist/nodejs/<%= pkg.name %>/'
            },
        },
        uglify: {
            browser_dist: {
                src: '<%= concat.browser_dist.dest %>',
                dest: 'dist/browser/<%= pkg.name %>.min.js'
            }
        }
    });

    // Default task
    grunt.registerTask('default', [
    // Build
        'clean',
        'concat',
        'uglify',
        'copy:nodejs_src',
        'copy:nodejs_package',
    // Tests
        'copy:integration_tests_node_src',
        'copy:integration_tests_node_package',
        'copy:integration_tests_browser',
        'mocha',
        'mochaTest'
    ]);

    // These plugins provide necessary tasks
    grunt.loadNpmTasks('grunt-contrib-concat');
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-contrib-clean');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-mocha');
    grunt.loadNpmTasks('grunt-mocha-test');
};
