var StreamClient = CDAPStreamClient.StreamClient,
    AuthManager = CDAPAuthManager,
    authManager = new AuthManager();

authManager.setConnectionInfo(config.host, config.port, config.ssl);
authManager.configure({
    username: config.user,
    password: config.pass
});

describe('CDAP ingest tests', function () {
    describe('StreamClient object creation', function () {
        it('Constructor creates an object', function () {
            var streamClient = new StreamClient({
                host: config.host,
                port: config.port
            });

            expect(streamClient).to.be.an('object');
        });

        it('Constructor creates a valid object', function () {
            var streamClient = new StreamClient({
                host: config.host,
                port: config.port
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
                var streamClient = new StreamClient({port: config.port});
            }).to.throwError();
        });

        it('"config" object for the constructor should contain "port" field', function () {
            expect(function () {
                var streamClient = new StreamClient({host: config.host});
            }).to.throwError();
        });
    });

    describe('Functionality', function () {
        it('"setTTL" for a valid stream', function () {
            var streamName = 'newStream',
                ttl = 86400,
                streamClient = new StreamClient({
                    host: config.host,
                    port: config.port,
                    ssl: config.ssl,
                    authManager: authManager
                });

            streamClient.create(streamName);
            expect(function () {
                streamClient.setTTL(streamName, ttl);
            }).not.to.throwError();
        });

        it('"setTTL" for an invalid stream', function () {
            var streamName = 'invalidStream',
                ttl = 86400,
                streamClient = new StreamClient({
                    host: config.host,
                    port: config.port,
                    ssl: config.ssl,
                    authManager: authManager
                });

            expect(function () {
                streamClient.setTTL(streamName, ttl);
            }).to.throwError();
        });

        it('"getTTL" for a valid stream and a valid TTL value', function () {
            var streamName = 'newStream',
                ttl = 86400,
                streamClient = new StreamClient({
                    host: config.host,
                    port: config.port,
                    ssl: config.ssl,
                    authManager: authManager
                });

            streamClient.create(streamName);
            var respTTL = streamClient.getTTL(streamName);

            expect(respTTL).to.be.equal(ttl);
        });

        it('"getTTL" for a valid stream and a wrong TTL value', function () {
            var streamName = 'newStream',
                invalidTTL = -1,
                streamClient = new StreamClient({
                    host: config.host,
                    port: config.port,
                    ssl: config.ssl,
                    authManager: authManager
                });

            streamClient.create(streamName);
            var respTTL = streamClient.getTTL(streamName);

            expect(respTTL).not.to.be.equal(invalidTTL);
        });

        it('"getTTL" for an invalid stream', function () {
            var streamName = 'invalidStream',
                streamClient = new StreamClient({
                    host: config.host,
                    port: config.port,
                    ssl: config.ssl,
                    authManager: authManager
                });

            expect(function () {
                streamClient.getTTL(streamName);
            }).to.throwError();
        });

        it('"truncate" for a valid stream', function () {
            var streamName = 'newStream',
                streamClient = new StreamClient({
                    host: config.host,
                    port: config.port,
                    ssl: config.ssl,
                    authManager: authManager
                });

            streamClient.create(streamName);
            expect(function () {
                streamClient.truncate(streamName);
            }).not.to.throwError();
        });

        it('"truncate" for an invalid stream', function () {
            var streamName = 'invalidStream',
                streamClient = new StreamClient({
                    host: config.host,
                    port: config.port,
                    ssl: config.ssl,
                    authManager: authManager
                });

            expect(function () {
                streamClient.truncate(streamName);
            }).to.throwError();
        });

        it('"createWriter" creates a valid object', function () {
            var streamName = 'newStream',
                streamClient = new StreamClient({
                    host: config.host,
                    port: config.port,
                    ssl: config.ssl,
                    authManager: authManager
                });

            var streamWriter = streamClient.createWriter(streamName);

            expect(streamWriter).to.have.property('write');
        });

        describe('StreamWriter', function () {
            it('"write" returns valid Promise object', function () {
                var streamName = 'newStream',
                    textToSend = 'klasj ddkjas ldjas kljfasklj fklasfj a',
                    streamClient = new StreamClient({
                        host: config.host,
                        port: config.port,
                        ssl: config.ssl,
                        authManager: authManager
                    });

                var streamWriter = streamClient.createWriter(streamName),
                    promise = streamWriter.write(textToSend);

                expect(promise).to.have.property('then');
                expect(promise).to.have.property('catch');
                expect(promise).to.have.property('notify');
                expect(promise).to.have.property('reject');
                expect(promise).to.have.property('resolve');
            });

            it('Event data validation', function (done) {
                /**
                 * We are testing http request. So we need to wait.
                 */
                this.timeout(10000);

                var streamName = 'newStream',
                    textToSend = 'data sent to CDAP',
                    streamClient = new StreamClient({
                        host: config.host,
                        port: config.port,
                        ssl: config.ssl,
                        authManager: authManager
                    });

                var streamWriter = streamClient.createWriter(streamName),
                    promise = streamWriter.write(textToSend);

                promise.then(function () {
                    var authToken = authManager.getToken(),
                        isDataConsistent = false,
                        httpRequest = new XMLHttpRequest();

                    httpRequest.open('GET', [
                        config.ssl ? 'https' : 'http', "://",
                        config.host, ':', config.port,
                        '/v2/streams/', streamName, '/events'
                    ].join(''), false);
                    httpRequest.setRequestHeader('Authorization', [authToken.type, ' ', authToken.token].join(''));
                    httpRequest.send();

                    if (XMLHttpRequest.DONE === httpRequest.readyState && 200 === httpRequest.status) {
                        var events = JSON.parse(httpRequest.responseText);

                        while (events.length) {
                            if (isDataConsistent) {
                                break;
                            }

                            var event = events.shift();
                            isDataConsistent = (event.body == JSON.stringify(textToSend));
                        }
                    }

                    expect(isDataConsistent).to.be.ok();
                    done();
                });
            });

            describe('Promise states', function () {
                it('"resolve" fires a handler', function (done) {
                    var streamName = 'newStream',
                        textToSend = 'klasj ddkjas ldjas kljfasklj fklasfj a',
                        streamClient = new StreamClient({
                            host: config.host,
                            port: config.port,
                            ssl: config.ssl,
                            authManager: authManager
                        });

                    var streamWriter = streamClient.createWriter(streamName),
                        promise = streamWriter.write(textToSend),
                        resolved = false,
                        promiseHandler = function (status) {
                            resolved = true;
                        },
                        promiseChecker = function () {
                            expect(resolved).to.be.ok();
                            done();
                        };

                    promise.then(promiseHandler).then(promiseChecker, promiseChecker);
                });

                it('"notify" fires a handler', function (done) {
                    var streamName = 'newStream',
                        textToSend = 'klasj ddkjas ldjas kljfasklj fklasfj a',
                        streamClient = new StreamClient({
                            host: config.host,
                            port: config.port,
                            ssl: config.ssl,
                            authManager: authManager
                        });

                    var streamWriter = streamClient.createWriter(streamName),
                        promise = streamWriter.write(textToSend),
                        resolved = false,
                        promiseHandler = function (status) {
                            resolved = true;
                        },
                        promiseChecker = function () {
                            expect(resolved).to.be.ok();
                            done();
                        };

                    promise.then(null, null, promiseHandler).then(promiseChecker, promiseChecker);
                });
            });
        });
    });
});