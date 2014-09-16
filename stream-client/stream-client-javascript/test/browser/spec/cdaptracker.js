describe('CDAPTracker tests', function() {
    var tracker = null;

    describe('Checking if Tracker returns correct object', function() {
        it('Could not be constructed without "url" parameter', function() {
            expect(function() {
                tracker = new CDAPTracker.ServiceConnector();
            }).toThrowError('"url" parameter have to be of type "string"');
        });

        it('Constructor return an object', function() {
            tracker = new CDAPTracker.ServiceConnector('/fake/url');
            expect(tracker).toBeDefined();
        });

        it('Object has necessary methods', function() {
            tracker = new CDAPTracker.ServiceConnector('/fake/url');
            expect(tracker.track).toBeDefined();
        });
    });

    describe('Checking Tracker functionality', function() {
        beforeEach(function() {
            jasmine.Ajax.install();
        });

        afterEach(function() {
            jasmine.Ajax.uninstall();
        });

        it('"track" method returns promise object', function() {
            tracker = CDAPTracker.ServiceConnector('/fake/url');

            var promise = tracker.track({});
            expect(promise).toBeDefined();
            expect(promise.then).toBeDefined();
            expect(promise.catch).toBeDefined();
            expect(promise.notify).toBeDefined();
            expect(promise.resolve).toBeDefined();
            expect(promise.reject).toBeDefined();
        });

        it('Checking promise "then" method', function() {
            var tracker = CDAPTracker.ServiceConnector(
                '/some/cool/url', 'localhost', 10000, false),
            promise = tracker.track({}),
            promiseFired = false;

            promise.then(function() {
                promiseFired = true;
            });

            expect(jasmine.Ajax.requests.mostRecent().url)
                .toBe('http://localhost:10000/some/cool/url');

            jasmine.Ajax.requests.mostRecent().response({
                "status": 200,
                "contentType": 'text/json',
                "responseText": '{}'
            });
            expect(promiseFired).toBe(true);
        });

        it('Checking promise "catch" method', function() {
            var tracker = CDAPTracker.ServiceConnector(
                '/some/cool/url', 'localhost', 10000, false),
            promise = tracker.track({}),
            promiseFired = false;

            promise.catch(function() {
                promiseFired = true;
            });

            expect(jasmine.Ajax.requests.mostRecent().url)
                .toBe('http://localhost:10000/some/cool/url');

            jasmine.Ajax.requests.mostRecent().response({
                "status": 404,
                "contentType": 'text/json',
                "responseText": '{}'
            });
            expect(promiseFired).toBe(true);
        });

        it('Checking promise notification handler( then(null, null, handler) )', function() {
            var tracker = CDAPTracker.ServiceConnector(
                '/some/cool/url', 'localhost', 10000, false),
            promise = tracker.track({}),
            promiseFired = false;

            promise.then(null, null, function() {
                promiseFired = true;
            });

            expect(jasmine.Ajax.requests.mostRecent().url)
                .toBe('http://localhost:10000/some/cool/url');

            jasmine.Ajax.requests.mostRecent().response({
                "status": 200,
                "contentType": 'text/json',
                "responseText": '{}'
            });
            expect(promiseFired).toBe(true);
        });
    });
});
