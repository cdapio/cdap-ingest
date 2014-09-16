describe('CDAPTracker tests', function() {
    var host = 'dummy.host',
        port = '65000',
        ssl = false,
        tracker = null;

    describe('Checking if tracker is correct object', function() {
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
});
