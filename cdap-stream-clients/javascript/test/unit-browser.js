var StreamClient = CDAPStreamClient.StreamClient;
/*
sinon.log = function (data) {
    console.log(data);
};
*/
describe('CDAP ingest tests', function () {
    beforeEach(function () {
        this.server = sinon.fakeServer.create();
    });

    afterEach(function () {
        this.server.restore();
    });

    describe('StreamClient object creation', function () {
        it('Constructor creates an object', function () {
            var streamClient = new StreamClient({
                host: 'localhost',
                port: 10000
            });

            expect(streamClient).to.be.an('object');
        });

        it('Constructor creates a valid object', function () {
            var streamClient = new StreamClient({
                host: 'localhost',
                port: 10000
            });

            expect(streamClient).to.have.property('create');
            expect(streamClient).to.have.property('setTTL');
            expect(streamClient).to.have.property('getTTL');
            expect(streamClient).to.have.property('truncate');
            expect(streamClient).to.have.property('createWriter');
        });

        it('Constructor could not be called without "config" object', function () {
            expect(function () {
                var streamClient = new StreamClient();
            }).to.throwError();
        });

        it('"config" object for the constructor should contain "host" field', function () {
            expect(function () {
                var streamClient = new StreamClient({port: 10000});
            }).to.throwError();
        });

        it('"config" object for the constructor should contain "port" field', function () {
            expect(function () {
                var streamClient = new StreamClient({host: 'localhost'});
            }).to.throwError();
        });
    });

    describe('Functionality', function () {
        it('"create" request url is valid', function () {
            var host = 'localhost',
                port = 10000,
                streamName = 'newStream',
                requestUrl = '/v2/streams/' + streamName,
                streamClient = new StreamClient({
                    host: host,
                    port: port
                });

            this.server.respondWith('PUT', requestUrl, [200, {}, 'OK']);

            streamClient.create(streamName);
        });

        it('"setTTL" for a valid stream', function () {
            var host = 'localhost',
                port = 10000,
                streamName = 'newStream',
                createUrl = '/v2/streams/' + streamName,
                configUrl = '/v2/streams/' + streamName + '/config',
                ttl = 86400,
                streamClient = new StreamClient({
                    host: host,
                    port: port
                });

            this.server.respondWith(new RegExp(createUrl), [200, {}, 'OK']);
            this.server.respondWith(new RegExp(configUrl), [200, {}, 'OK']);

            streamClient.create(streamName);
            expect(function () {
                streamClient.setTTL(streamName, ttl);
            }).not.to.throwError();

        });

        it('"setTTL" for an invalid stream', function () {
            var host = 'localhost',
                port = 10000,
                streamName = 'invalidStream',
                configUrl = '/v2/streams/' + streamName + '/config',
                ttl = 86400,
                streamClient = new StreamClient({
                    host: host,
                    port: port
                });

            this.server.respondWith(new RegExp(configUrl), [404, {}, 'OK']);

            expect(function () {
                streamClient.setTTL(streamName, ttl);
            }).to.throwError();
        });

        it('"getTTL" for a valid stream and a valid TTL value', function () {
            var host = 'localhost',
                port = 10000,
                streamName = 'newStream',
                createUrl = '/v2/streams/' + streamName,
                configUrl = '/v2/streams/' + streamName + '/info',
                ttl = 86400,
                streamClient = new StreamClient({
                    host: host,
                    port: port
                });

            this.server.respondWith(new RegExp(createUrl), [200, {}, 'OK']);
            this.server.respondWith(new RegExp(configUrl), [200, { "Content-Type": "application/json" },
                JSON.stringify({ttl: ttl})]);

            streamClient.create(streamName);
            var respTTL = streamClient.getTTL(streamName);

            expect(respTTL).to.be.equal(ttl);
        });

        it('"getTTL" for a valid stream and a wrong TTL value', function () {
            var host = 'localhost',
                port = 10000,
                streamName = 'newStream',
                createUrl = '/v2/streams/' + streamName,
                configUrl = '/v2/streams/' + streamName + '/info',
                validTTL = 86400,
                invalidTTL = -1,
                streamClient = new StreamClient({
                    host: host,
                    port: port
                });

            this.server.respondWith(new RegExp(createUrl), [200, {}, 'OK']);
            this.server.respondWith(new RegExp(configUrl), [200, { "Content-Type": "application/json" },
                JSON.stringify({ttl: invalidTTL})]);

            streamClient.create(streamName);
            var respTTL = streamClient.getTTL(streamName);

            expect(respTTL).not.to.be.equal(validTTL);
        });

        it('"getTTL" for an invalid stream', function () {
            var host = 'localhost',
                port = 10000,
                streamName = 'invalidStream',
                configUrl = '/v2/streams/' + streamName + '/info',
                streamClient = new StreamClient({
                    host: host,
                    port: port
                });

            this.server.respondWith(new RegExp(configUrl), [404, {}, '']);

            expect(function () {
                streamClient.getTTL(streamName);
            }).to.throwError();
        });

        it('"truncate" for a valid stream', function () {
            var host = 'localhost',
                port = 10000,
                streamName = 'newStream',
                createUrl = '/v2/streams/' + streamName,
                truncateUrl = '/v2/streams/' + streamName + '/truncate',
                streamClient = new StreamClient({
                    host: host,
                    port: port
                });

            this.server.respondWith(new RegExp(createUrl), [200, {}, 'OK']);
            this.server.respondWith(new RegExp(truncateUrl), [200, {}, 'OK']);

            streamClient.create(streamName);
            expect(function () {
                streamClient.truncate(streamName);
            }).not.to.throwError();
        });

        it('"truncate" for an invalid stream', function () {
            var host = 'localhost',
                port = 10000,
                streamName = 'invalidStream',
                truncateUrl = '/v2/streams/' + streamName + '/truncate',
                streamClient = new StreamClient({
                    host: host,
                    port: port
                });

            this.server.respondWith(new RegExp(truncateUrl), [404, {}, '']);

            expect(function () {
                streamClient.truncate(streamName);
            }).to.throwError();
        });

        it('"createWriter" creates a valid object', function () {
            var host = 'localhost',
                port = 10000,
                streamName = 'newStream',
                configUrl = '/v2/streams/' + streamName + '/info',
                streamClient = new StreamClient({
                    host: host,
                    port: port
                });

            this.server.respondWith(new RegExp(configUrl), [200, { "Content-Type": "application/json" },
                JSON.stringify({ttl: 86400})]);

            var streamWriter = streamClient.createWriter(streamName);

            expect(streamWriter).to.have.property('write');
        });

        describe('StreamWriter', function () {
            it('"write" returns valid Promise object', function () {
                var host = 'localhost',
                    port = 10000,
                    streamName = 'newStream',
                    configUrl = '/v2/streams/' + streamName + '/info',
                    writeUrl = '/v2/streams/' + streamName,
                    textToSend = 'klasj ddkjas ldjas kljfasklj fklasfj a',
                    streamClient = new StreamClient({
                        host: host,
                        port: port
                    });

                this.server.respondWith(new RegExp(writeUrl), [200, {}, 'OK']);
                this.server.respondWith(new RegExp(configUrl), [200, { "Content-Type": "application/json" },
                    JSON.stringify({ttl: 86400})]);

                var streamWriter = streamClient.createWriter(streamName),
                    promise = streamWriter.write(textToSend);

                expect(promise).to.have.property('then');
                expect(promise).to.have.property('catch');
                expect(promise).to.have.property('notify');
                expect(promise).to.have.property('reject');
                expect(promise).to.have.property('resolve');
            });

            describe('Promise states', function () {
                it('"resolve" fires a handler', function () {
                    var host = 'localhost',
                        port = 10000,
                        streamName = 'newStream',
                        configUrl = '/v2/streams/' + streamName + '/info',
                        writeUrl = '/v2/streams/' + streamName,
                        textToSend = 'klasj ddkjas ldjas kljfasklj fklasfj a',
                        streamClient = new StreamClient({
                            host: host,
                            port: port
                        });

                    this.server.respondWith(new RegExp(writeUrl), [200, {}, 'OK']);
                    this.server.respondWith(new RegExp(configUrl), [200, { "Content-Type": "application/json" },
                        JSON.stringify({ttl: 86400})]);

                    var streamWriter = streamClient.createWriter(streamName),
                        promise = streamWriter.write(textToSend),
                        resolved = false,
                        promiseHandler = function (status) {
                            resolved = true;
                        },
                        promiseChecker = function () {
                            expect(resolved).to.be.ok();
                        };

                    this.server.respond();

                    promise.then(promiseHandler).then(promiseChecker, promiseChecker);
                });

                it('"catch" fires a handler', function () {
                    var host = 'localhost',
                        port = 10000,
                        streamName = 'newStream',
                        configUrl = '/v2/streams/' + streamName + '/info',
                        writeUrl = '/v2/streams/' + streamName,
                        textToSend = 'klasj ddkjas ldjas kljfasklj fklasfj a',
                        streamClient = new StreamClient({
                            host: host,
                            port: port
                        });

                    this.server.respondWith(new RegExp(writeUrl), [404, {}, '']);
                    this.server.respondWith(new RegExp(configUrl), [200, { "Content-Type": "application/json" },
                        JSON.stringify({ttl: 86400})]);

                    var streamWriter = streamClient.createWriter(streamName),
                        promise = streamWriter.write(textToSend),
                        resolved = false,
                        promiseHandler = function (status) {
                            resolved = true;
                        },
                        promiseChecker = function () {
                            expect(resolved).to.be.ok();
                        };

                    this.server.respond();

                    promise.catch(promiseHandler).then(promiseChecker, promiseChecker);
                });

                it('"notify" fires a handler', function () {
                    var host = 'localhost',
                        port = 10000,
                        streamName = 'newStream',
                        configUrl = '/v2/streams/' + streamName + '/info',
                        writeUrl = '/v2/streams/' + streamName,
                        textToSend = 'klasj ddkjas ldjas kljfasklj fklasfj a',
                        streamClient = new StreamClient({
                            host: host,
                            port: port
                        });

                    this.server.respondWith(new RegExp(writeUrl), [404, {}, '']);
                    this.server.respondWith(new RegExp(configUrl), [200, { "Content-Type": "application/json" },
                        JSON.stringify({ttl: 86400})]);

                    var streamWriter = streamClient.createWriter(streamName),
                        promise = streamWriter.write(textToSend),
                        resolved = false,
                        promiseHandler = function (status) {
                            resolved = true;
                        },
                        promiseChecker = function () {
                            expect(resolved).to.be.ok();
                        };

                    this.server.respond();

                    promise.then(null, null, promiseHandler).then(promiseChecker, promiseChecker);
                });
            });
        });
    });
});