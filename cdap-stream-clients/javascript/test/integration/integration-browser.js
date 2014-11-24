var CDAPAuthManager = CDAPAuthManager || null,
    StreamClient = CDAPStreamClient.StreamClient,
    AuthManager = CDAPAuthManager,
    authManager = null;

if (AuthManager) {
    authManager = new AuthManager();
    authManager.setConnectionInfo(config.host, config.port, config.ssl);
    authManager.configure({
        username: config.user,
        password: config.pass
    });
}

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

    describe('Functionality', function (done) {
        it('"setTTL" for a valid stream', function () {
            var streamName = 'newStream',
                ttl = 86400,
                streamClient = new StreamClient({
                    host: config.host,
                    port: config.port,
                    ssl: config.ssl,
                    authManager: authManager
                }),
                resolved = false,
                checker = function () {
                    expect(resolved).to.be.ok();
                    done();
                };

            var createPromise = streamClient.create(streamName);
            createPromise.then(function () {
                var ttlPromise = streamClient.setTTL(streamName, ttl);

                ttlPromise.then(function () {
                    resolved = true;
                }).then(checker, checker);
            }, function () {
                expect().fail();
            });
        });

        it('"setTTL" for an invalid stream', function (done) {
            var streamName = 'invalidStream',
                ttl = 86400,
                streamClient = new StreamClient({
                    host: config.host,
                    port: config.port,
                    ssl: config.ssl,
                    authManager: authManager
                }),
                resolved = true,
                checker = function () {
                    expect(resolved).not.to.be.ok();
                    done();
                };

            var ttlPromise = streamClient.setTTL(streamName, ttl);
            ttlPromise.catch(function () {
                resolved = false;
            }).then(checker, checker);
        });

        it('"getTTL" for a valid stream and a valid TTL value', function (done) {
            var streamName = 'newStream',
                ttl = 86400,
                streamClient = new StreamClient({
                    host: config.host,
                    port: config.port,
                    ssl: config.ssl,
                    authManager: authManager
                }),
                respTTL = -1,
                checker = function () {
                    expect(respTTL).to.be.equal(ttl);
                    done();
                };

            var createPromise = streamClient.create(streamName);
            createPromise.then(function () {
                var configPromise = streamClient.getTTL(streamName);

                configPromise.then(function (configTTL) {
                    respTTL = configTTL;
                }).then(checker, checker);
            }).catch(function () {
                except().fail();
            });
        });

        it('"getTTL" for a valid stream and a wrong TTL value', function (done) {
            var streamName = 'newStream',
                invalidTTL = -1,
                streamClient = new StreamClient({
                    host: config.host,
                    port: config.port,
                    ssl: config.ssl,
                    authManager: authManager
                }),
                respTTL = 0,
                checker = function () {
                    expect(respTTL).not.to.be.equal(invalidTTL);
                    done();
                };

            var createPromise = streamClient.create(streamName);
            createPromise.then(function () {
                var configPromise = streamClient.getTTL(streamName);

                configPromise.then(function (configTTL) {
                    respTTL = configTTL;
                }).then(checker, checker);
            }).catch(function () {
                except().fail();
            });
        });

        it('"getTTL" for an invalid stream', function (done) {
            var streamName = 'invalidStream',
                streamClient = new StreamClient({
                    host: config.host,
                    port: config.port,
                    ssl: config.ssl,
                    authManager: authManager
                }),
                rejected = false,
                checker = function () {
                    expect(rejected).not.to.be.ok();
                    done();
                };

            var ttlPromise = streamClient.getTTL(streamName);
            ttlPromise.catch(function () {
                rejected = true;
            }).then(checker, checker);
        });

        it('"truncate" for a valid stream', function (done) {
            var streamName = 'newStream',
                streamClient = new StreamClient({
                    host: config.host,
                    port: config.port,
                    ssl: config.ssl,
                    authManager: authManager
                });

            var createPromise = streamClient.create(streamName);

            createPromise.then(function () {
                var truncatePromise = streamClient.truncate(streamName);

                truncatePromise.then(function () {
                    var authToken = {
                        type: '',
                        token: ''
                    };

                    if (authManager) {
                        authToken = authManager.getToken();
                    }

                    var isDataConsistent = false,
                        response = http.request({
                            method: 'GET',
                            host: config.host,
                            port: config.port,
                            protocol: config.ssl ? 'https' : 'http',
                            headers: {
                                Authorization: [authToken.type, ' ', authToken.token].join('')
                            },
                            body: '',

                            path: '/v2/streams/' + streamName + '/events'
                        }, function () {
                            if (200 === response.statusCode) {
                                var events = JSON.parse(response.body.toString());
                                isDataConsistent = (0 == events.length);
                                /**
                                 * HTTP status = 204 means there is NO CONTENT
                                 */
                            } else if (204 == response.statusCode) {
                                isDataConsistent = true;
                            }

                            expect(isDataConsistent).to.be.ok();
                            done();
                        }).end();
                }, function () {
                    expect().fail();
                });
            }, function () {
                expect().fail();
            });
        });

        it('"truncate" for an invalid stream', function (done) {
            var streamName = 'invalidStream',
                streamClient = new StreamClient({
                    host: config.host,
                    port: config.port,
                    ssl: config.ssl,
                    authManager: authManager
                }),
                rejected = false,
                checker = function () {
                    expect(rejected).to.be.ok();
                    done();
                };

            var truncatePromise = streamClient.truncate(streamName);
            truncatePromise.catch(function () {
                rejected = true;
            }).then(checker, checker);
        });

        it('"createWriter" creates a valid object', function (done) {
            var streamName = 'newStream',
                streamClient = new StreamClient({
                    host: config.host,
                    port: config.port,
                    ssl: config.ssl,
                    authManager: authManager
                }),
                streamWriter = null,
                checker = function () {
                    expect(streamWriter).to.be.an('object');
                    expect(streamWriter).to.have.property('write');
                    done();
                };

            var writerPromise = streamClient.createWriter(streamName);
            writerPromise.then(function (writer) {
                streamWriter = writer;
            }).then(checker, checker);
        });

        describe('StreamWriter', function () {
            it('"write" returns valid Promise object', function (done) {
                var streamName = 'newStream',
                    textToSend = 'klasj ddkjas ldjas kljfasklj fklasfj a',
                    streamClient = new StreamClient({
                        host: config.host,
                        port: config.port,
                        ssl: config.ssl,
                        authManager: authManager
                    });

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
                    startTime = Date.now(),
                    promise = streamWriter.write(textToSend);

                promise.then(function () {
                    var authToken = {
                        type: '',
                        token: ''
                    };

                    if (authManager) {
                        authToken = authManager.getToken();
                    }

                    var isDataConsistent = false,
                        httpRequest = new XMLHttpRequest();

                    httpRequest.open('GET', [
                        config.ssl ? 'https' : 'http', "://",
                        config.host, ':', config.port,
                        '/v2/streams/', streamName, '/events?start=',
                        startTime, '&end=', Date.now()
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