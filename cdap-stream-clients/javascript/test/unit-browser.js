var StreamClient = CDAPStreamClient.StreamClient;

sinon.log = function (data) {
    console.log(data);
};

describe('CDAP ingest tests', function () {
    beforeEach(function () {
        this.server = sinon.fakeServer.create();
        this.server.autoRespond = true;
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
        it('"setTTL" for a valid stream', function (done) {
            var host = 'localhost',
                port = 10000,
                streamName = 'newStream',
                configUrl = '/v2/streams/' + streamName + '/config',
                ttl = 86400,
                streamClient = new StreamClient({
                    host: host,
                    port: port
                }),
                resolved = false,
                checker = function () {
                    expect(resolved).to.be.ok();
                    done();
                };

            this.server.respondWith('PUT', new RegExp(configUrl), [200, {}, 'OK']);

            var configPromise = streamClient.setTTL(streamName, ttl);

            configPromise.then(function () {
                resolved = true;
            }).then(checker, checker);

        });

        it('"setTTL" for an invalid stream', function (done) {
            var host = 'localhost',
                port = 10000,
                streamName = 'invalidStream',
                configUrl = '/v2/streams/' + streamName + '/config',
                ttl = 86400,
                streamClient = new StreamClient({
                    host: host,
                    port: port
                }),
                rejected = false,
                checker = function () {
                    expect(rejected).to.be.ok();
                    done();
                };

            this.server.respondWith(new RegExp(configUrl), [404, {}, 'OK']);

            var promise = streamClient.setTTL(streamName, ttl);
            promise.catch(function () {
                rejected = true;
            }).then(checker, checker);
        });

        it('"getTTL" for a valid stream and a valid TTL value', function (done) {
            var host = 'localhost',
                port = 10000,
                streamName = 'newStream',
                configUrl = '/v2/streams/' + streamName + '/info',
                ttl = 86400,
                streamClient = new StreamClient({
                    host: host,
                    port: port
                }),
                respTTL = -1,
                checker = function () {
                    expect(respTTL).to.be.equal(ttl);
                    done();
                };

            this.server.respondWith(new RegExp(configUrl), [200, { "Content-Type": "application/json" },
                JSON.stringify({ttl: ttl})]);

            var configPromise = streamClient.getTTL(streamName);

            configPromise.then(function (configTTL) {
                respTTL = configTTL;
            }).then(checker, checker);
        });

        it('"getTTL" for a valid stream and a wrong TTL value', function (done) {
            var host = 'localhost',
                port = 10000,
                streamName = 'newStream',
                configUrl = '/v2/streams/' + streamName + '/info',
                validTTL = 86400,
                invalidTTL = -1,
                streamClient = new StreamClient({
                    host: host,
                    port: port
                }),
                respTTL = -1,
                checker = function () {
                    expect(respTTL).not.to.be.equal(validTTL);
                    done();
                };

            this.server.respondWith(new RegExp(configUrl), [200, { "Content-Type": "application/json" },
                JSON.stringify({ttl: invalidTTL})]);

            var configPromise = streamClient.getTTL(streamName);

            configPromise.then(function (configTTL) {
                respTTL = configTTL;
            }).then(checker, checker);
        });

        it('"getTTL" for an invalid stream', function (done) {
            var host = 'localhost',
                port = 10000,
                streamName = 'invalidStream',
                configUrl = '/v2/streams/' + streamName + '/info',
                streamClient = new StreamClient({
                    host: host,
                    port: port
                }),
                rejected = false,
                checker = function () {
                    expect(rejected).to.be.ok();
                    done();
                };

            this.server.respondWith(new RegExp(configUrl), [404, {}, '']);

            var configPromise = streamClient.getTTL(streamName);
            configPromise.catch(function () {
                rejected = true;
            }).then(checker, checker);
        });

        it('"truncate" for a valid stream', function (done) {
            var host = 'localhost',
                port = 10000,
                streamName = 'newStream',
                truncateUrl = '/v2/streams/' + streamName + '/truncate',
                streamClient = new StreamClient({
                    host: host,
                    port: port
                }),
                resolved = false,
                checker = function () {
                    expect(resolved).to.be.ok();
                    done();
                };

            this.server.respondWith(new RegExp(truncateUrl), [200, {}, 'OK']);

            var truncatePromise = streamClient.truncate(streamName);
            truncatePromise.then(function () {
                resolved = true;
            }).then(checker, checker);
        });

        it('"truncate" for an invalid stream', function (done) {
            var host = 'localhost',
                port = 10000,
                streamName = 'invalidStream',
                truncateUrl = '/v2/streams/' + streamName + '/truncate',
                streamClient = new StreamClient({
                    host: host,
                    port: port
                }),
                rejected = false,
                checker = function () {
                    expect(rejected).to.be.ok();
                    done();
                };

            this.server.respondWith(new RegExp(truncateUrl), [404, {}, '']);

            var truncatePromise = streamClient.truncate(streamName);
            truncatePromise.catch(function () {
                rejected = true;
            }).then(checker, checker);
        });

        it('"createWriter" creates a valid object', function (done) {
            var host = 'localhost',
                port = 10000,
                streamName = 'newStream',
                configUrl = '/v2/streams/' + streamName + '/info',
                streamClient = new StreamClient({
                    host: host,
                    port: port
                }),
                streamWriter = null,
                checker = function () {
                    expect(streamWriter).to.be.an('object');
                    expect(streamWriter).to.have.property('write');
                    done();
                };

            this.server.respondWith(new RegExp(configUrl), [200, { "Content-Type": "application/json" },
                JSON.stringify({ttl: 86400})]);

            var writerPromise = streamClient.createWriter(streamName);
            writerPromise.then(function (writer) {
                streamWriter = writer;
            }).then(checker, checker);
        });

        describe('StreamWriter', function () {
            it('"write" returns valid Promise object', function (done) {
                var self = this,
                    host = 'localhost',
                    port = 10000,
                    streamName = 'newStream',
                    configUrl = '/v2/streams/' + streamName + '/info',
                    writeUrl = '/v2/streams/' + streamName,
                    textToSend = 'klasj ddkjas ldjas kljfasklj fklasfj a',
                    streamClient = new StreamClient({
                        host: host,
                        port: port
                    });

                this.server.respondWith(new RegExp(writeUrl), [200, {}, '']);
                this.server.respondWith(new RegExp(configUrl), [200, { "Content-Type": "application/json" },
                    JSON.stringify({ttl: 86400})]);

                var writerPromise = streamClient.createWriter(streamName);

                writerPromise.then(function (newWriter) {
                    var promise = newWriter.write(textToSend);

                    expect(promise).to.have.property('then');
                    expect(promise).to.have.property('catch');
                    expect(promise).to.have.property('notify');
                    expect(promise).to.have.property('reject');
                    expect(promise).to.have.property('resolve');

                    done();
                }, function () {
                    expect().fail();
                });

                this.server.respond();
            });

            describe('Promise states', function () {
                it('"resolve" fires a handler', function (done) {
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

                    var writerPromise = streamClient.createWriter(streamName),
                        resolved = false,
                        checker = function () {
                            expect(resolved).to.be.ok();
                            done();
                        };

                    writerPromise.then(function (newWriter) {
                        var writePromise = newWriter.write(textToSend);

                        writePromise.then(function () {
                            resolved = true;
                        }).then(checker, checker);
                    }, function () {
                        expect().fail();
                    });

                    this.server.respond();
                });

                it('"catch" fires a handler', function (done) {
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

                    var writerPromise = streamClient.createWriter(streamName),
                        rejected = false,
                        checker = function () {
                            expect(rejected).to.be.ok();
                            done();
                        };

                    writerPromise.then(function (newWriter) {
                        var writePromise = newWriter.write(textToSend);

                        writePromise.catch(function () {
                            rejected = true;
                        }).then(checker, checker);
                    }, function () {
                        expect().fail();
                    });

                    this.server.respond();
                });

                it('"notify" fires a handler', function (done) {
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

                    var writerPromise = streamClient.createWriter(streamName),
                        rejected = false,
                        checker = function () {
                            expect(rejected).to.be.ok();
                            done();
                        };

                    writerPromise.then(function (newWriter) {
                        var writePromise = newWriter.write(textToSend);

                        writePromise.then(null, null, function () {
                            rejected = true;
                        }).then(checker, checker);
                    }, function () {
                        expect().fail();
                    });

                    this.server.respond();
                });
            });
        });
    });
});