describe('CDAPTracker tests', function () {
    beforeEach(function () {
        this.server = sinon.fakeServer.create();
    });

    afterEach(function () {
        this.server.restore();
    });

    var tracker = null;

    describe('Checking if Tracker returns correct object', function () {
        it('Constructor returns an object', function () {
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

            this.server.respondWith("POST", "/some/article/comments.json",
                                    [200, { "Content-Type": "application/json" },
                                     '[{}]']);

            var promise = tracker.track('/some/cool/url', {});

            expect(promise).to.be.an('object');
            expect(promise).to.have.property('then');
            expect(promise).to.have.property('catch');
        });

        it('Checking promise "then" method', function () {
            this.server.respondWith("POST", "/some/article/comments.json",
                                    [200, { "Content-Type": "application/json" },
                                     '[{}]']);

            var tracker = CDAPTracker.ServiceConnector(
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
            this.server.respondWith("POST", "/some/article/comments.json",
                                    [404, { "Content-Type": "application/json" },
                                     '[{}]']);

            var tracker = CDAPTracker.ServiceConnector(
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
            this.server.respondWith("POST", "/some/article/comments.json",
                                    [200, { "Content-Type": "application/json" },
                                     '[{}]']);

            var tracker = CDAPTracker.ServiceConnector(
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
