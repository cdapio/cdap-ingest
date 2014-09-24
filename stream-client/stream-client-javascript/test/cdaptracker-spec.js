var CDAPTracker = require('../src/serviceconnector'),
    expect = require('expect.js'),
    nock = require('nock');

describe('CDAPTracker tests', function () {
    var tracker = null;

    describe('Checking if Tracker returns correct object', function () {
        it('Constructor return an object', function () {
            tracker = new CDAPTracker.ServiceConnector();
            expect(tracker).to.be.an('object');
        });

        it('Object has necessary methods', function () {
            tracker = new CDAPTracker.ServiceConnector();
            expect(tracker).to.have.property('track');
        });
    });

    describe('Checking Tracker functionality', function () {
        it('"track" method returns promise object', function () {
            tracker = CDAPTracker.ServiceConnector();

            var r = nock('http://localhost:10000')
                .post('/some/cool/url')
                .reply(200, "Ok");

            var promise = tracker.track('/some/cool/url', {});

            expect(promise).to.be.an('object');
            expect(promise).to.have.property('then');
            expect(promise).to.have.property('catch');
        });

        it('Checking promise "then" method', function () {
            var r = nock('http://localhost:10000')
                    .defaultReplyHeaders({
                        'Content-Type': 'application/json'
                    })
                    .post('/some/cool/url')
                    .reply(200, "{}"),

                tracker = CDAPTracker.ServiceConnector(
                    'localhost', 10000, false),
                promise = tracker.track('/some/cool/url', {}),
                promiseFired = false,

                checkResult = function () {
                    expect(promiseFired).to.be.ok();
                };

            promise.then(function (data) {
                promiseFired = true;
            });

            promise.then(checkResult, checkResult);
        });

        it('Checking promise "catch" method', function () {
            var r = nock('http://localhost:10000')
                    .defaultReplyHeaders({
                        'Content-Type': 'application/json'
                    })
                    .post('/some/cool/url')
                    .reply(404, "{}"),

                tracker = CDAPTracker.ServiceConnector(
                    'localhost', 10000, false),
                promise = tracker.track('/some/cool/url', {}),
                promiseFired = false,

                checkResult = function () {
                    expect(promiseFired).to.be.ok();
                };

            promise.catch(function () {
                promiseFired = true;
            });

            promise.then(checkResult, checkResult);
        });

        it('Checking promise notification handler( then(null, null, handler) )', function () {
            var r = nock('http://localhost:10000')
                    .defaultReplyHeaders({
                        'Content-Type': 'application/json'
                    })
                    .post('/some/cool/url')
                    .reply(200, "{}"),

                tracker = CDAPTracker.ServiceConnector(
                    'localhost', 10000, false),
                promise = tracker.track('/some/cool/url', {}),
                promiseFired = false,

                checkResult = function () {
                    expect(promiseFired).to.be.ok();
                };

            promise.then(null, null, function (data) {
                promiseFired = true;
            });

            promise.then(checkResult, checkResult);
        });
    });
});
