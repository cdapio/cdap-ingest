var expect = require('expect.js'),
    sinon = require('sinon'),
    nock = require('nock'),
    StreamClient = require('../src/streamclient');

describe('CDAP ingest tests', function () {
    describe('StreamClient object creation', function () {
        it('Constructor creates an object', function () {
            var streamClient = new StreamClient({
                host: 'localhost',
                port: 11015
            });

            expect(streamClient).to.be.an('object');
        });

        it('Constructor creates a valid object', function () {
            var streamClient = new StreamClient({
                host: 'localhost',
                port: 11015
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
                var streamClient = new StreamClient({port: 11015});
            }).to.throwError();
        });

        it('"config" object for the constructor should contain "port" field', function () {
            expect(function () {
                var streamClient = new StreamClient({host: 'localhost'});
            }).to.throwError();
        });
    });

    describe('Functionality', function () {

        it('"setTTL" for a valid stream', function () {
            var host = 'localhost',
                port = 11015,
                streamName = 'newStream',
                createUrl = '/v2/streams/' + streamName,
                configUrl = '/v2/streams/' + streamName + '/config',
                ttl = 86400,

                mockCreate = nock('http://' + host + ':' + port)
                    .put(createUrl)
                    .reply(200),
                mockConfig = nock('http://' + host + ':' + port)
                    .put(configUrl)
                    .reply(200),
                streamClient = new StreamClient({
                    host: host,
                    port: port
                }),
                resolved = false,
                checker = function () {
                    expect(resolved).to.be.ok();
                };

            var createPromise = streamClient.create(streamName);

            createPromise.then(function () {
                var configPromise = streamClient.setTTL(streamName, ttl);

                configPromise.then(function () {
                    resolved = true;
                }).then(checker, checker);
            }).catch(function () {
                expect().fail();
            });
        });

        it('"setTTL" for an invalid stream', function () {
            var host = 'localhost',
                port = 11015,
                streamName = 'invalidStream',
                configUrl = '/v2/streams/' + streamName + '/config',
                ttl = 86400,

                mock = nock('http://' + host + ':' + port)
                    .put(configUrl)
                    .reply(404),
                streamClient = new StreamClient({
                    host: host,
                    port: port
                }),
                rejected = false,
                checker = function () {
                    expect(rejected).to.be.ok();
                };

            var promise = streamClient.setTTL(streamName, ttl);
            promise.catch(function () {
                rejected = true;
            }).then(checker, checker);
        });

        it('"getTTL" for a valid stream and a valid TTL value', function () {
            var host = 'localhost',
                port = 11015,
                streamName = 'newStream',
                createUrl = '/v2/streams/' + streamName,
                configUrl = '/v2/streams/' + streamName + '/info',
                ttl = 86400,
                respTTL = -1,

                mockCreate = nock('http://' + host + ':' + port)
                    .put(createUrl)
                    .reply(200),
                mockConfig = nock('http://' + host + ':' + port)
                    .get(configUrl)
                    .reply(200, JSON.stringify({
                        ttl: ttl
                    })),
                streamClient = new StreamClient({
                    host: host,
                    port: port
                }),
                checker = function () {
                    expect(respTTL).to.be.equal(ttl);
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

        it('"getTTL" for a valid stream and a wrong TTL value', function () {
            var host = 'localhost',
                port = 11015,
                streamName = 'newStream',
                createUrl = '/v2/streams/' + streamName,
                configUrl = '/v2/streams/' + streamName + '/info',
                validTTL = 86400,
                invalidTTL = -1,
                respTTL = 0,

                mockCreate = nock('http://' + host + ':' + port)
                    .put(createUrl)
                    .reply(200),
                mockConfig = nock('http://' + host + ':' + port)
                    .get(configUrl)
                    .reply(200, JSON.stringify({
                        ttl: invalidTTL
                    })),
                streamClient = new StreamClient({
                    host: host,
                    port: port
                }),
                checker = function () {
                    expect(respTTL).not.to.be.equal(validTTL);
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

        it('"getTTL" for an invalid stream', function () {
            var host = 'localhost',
                port = 11015,
                streamName = 'invalidStream',
                configUrl = '/v2/streams/' + streamName + '/info',
                ttlArgs = {
                    host: host,
                    port: port,
                    path: configUrl,
                    ssl: false,
                    headers: { Authorization: '' },
                    data: '',
                    method: 'GET',
                    async: false
                },

                mock = nock('http://' + host + ':' + port)
                    .get(configUrl)
                    .reply(404),
                streamClient = new StreamClient({
                    host: host,
                    port: port
                }),
                rejected = false,
                checker = function () {
                    expect(rejected).to.be.ok();
                };

            var configPromise = streamClient.getTTL(streamName);
            configPromise.catch(function () {
                rejected = true;
            }).then(checker, checker);
        });

        it('"truncate" for a valid stream', function () {
            var host = 'localhost',
                port = 11015,
                streamName = 'newStream',
                createUrl = '/v2/streams/' + streamName,
                truncateUrl = '/v2/streams/' + streamName + '/truncate',

                mockCreate = nock('http://' + host + ':' + port)
                    .put(createUrl)
                    .reply(200),
                mockTruncate = nock('http://' + host + ':' + port)
                    .post(truncateUrl)
                    .reply(200),
                streamClient = new StreamClient({
                    host: host,
                    port: port
                }),
                resolved = false,
                checker = function () {
                    expect(resolved).to.be.ok();
                };

            var createPromise = streamClient.create(streamName);
            createPromise.then(function () {
                var truncatePromise = streamClient.truncate(streamName);
                truncatePromise.then(function () {
                    resolved = true;
                }).then(checker, checker);
            }).catch(function () {
                expect().fail();
            });
        });

        it('"truncate" for an invalid stream', function () {
            var host = 'localhost',
                port = 11015,
                streamName = 'invalidStream',
                truncateUrl = '/v2/streams/' + streamName + '/truncate',

                mockTruncate = nock('http://' + host + ':' + port)
                    .post(truncateUrl)
                    .reply(404),
                streamClient = new StreamClient({
                    host: host,
                    port: port
                }),
                rejected = false,
                checker = function () {
                    expect(rejected).to.be.ok();
                };

            var truncatePromise = streamClient.truncate(streamName);
            truncatePromise.catch(function () {
                rejected = true;
            }).then(checker, checker);
        });

        it('"createWriter" creates a valid object', function () {
            var host = 'localhost',
                port = 11015,
                streamName = 'newStream',
                configUrl = '/v2/streams/' + streamName + '/info',

                mockConfig = nock('http://' + host + ':' + port)
                    .get(configUrl)
                    .reply(200, JSON.stringify({
                        ttl: 86400
                    })),
                streamClient = new StreamClient({
                    host: host,
                    port: port
                }),
                streamWriter = null,
                checker = function () {
                    expect(streamWriter).to.be.an('object');
                    expect(streamWriter).to.have.property('write');
                };

            var writerPromise = streamClient.createWriter(streamName);
            writerPromise.then(function (writer) {
                streamWriter = writer;
            }).then(checker, checker);
        });

        describe('StreamWriter', function () {
            it('"write" returns valid Promise object', function () {
                var host = 'localhost',
                    port = 11015,
                    streamName = 'newStream',
                    configUrl = '/v2/streams/' + streamName + '/info',
                    writeUrl = '/v2/streams/' + streamName,
                    textToSend = 'klasj ddkjas ldjas kljfasklj fklasfj a',

                    mockConfig = nock('http://' + host + ':' + port)
                        .get(configUrl)
                        .reply(200),
                    mockWrite = nock('http://' + host + ':' + port)
                        .post(writeUrl)
                        .reply(200),
                    streamClient = new StreamClient({
                        host: host,
                        port: port
                    });

                var writerPromise = streamClient.createWriter(streamName);

                writerPromise.then(function (newWriter) {
                    var promise = newWriter.write(textToSend);

                    expect(promise).to.have.property('then');
                    expect(promise).to.have.property('catch');
                    expect(promise).to.have.property('notify');
                    expect(promise).to.have.property('reject');
                    expect(promise).to.have.property('resolve');
                }, function () {
                    expect().fail();
                });
            });

            describe('Promise states', function () {
                it('"resolve" fires a handler', function () {
                    var host = 'localhost',
                        port = 11015,
                        streamName = 'newStream',
                        configUrl = '/v2/streams/' + streamName + '/info',
                        writeUrl = '/v2/streams/' + streamName,
                        textToSend = 'klasj ddkjas ldjas kljfasklj fklasfj a',

                        mockConfig = nock('http://' + host + ':' + port)
                            .get(configUrl)
                            .reply(200),
                        mockWrite = nock('http://' + host + ':' + port)
                            .post(writeUrl)
                            .reply(200, "{}"),

                        streamClient = new StreamClient({
                            host: host,
                            port: port
                        });

                    var writerPromise = streamClient.createWriter(streamName),
                        resolved = false,
                        checker = function () {
                            expect(resolved).to.be.ok();
                        };

                    writerPromise.then(function (newWriter) {
                        var writePromise = newWriter.write(textToSend);

                        writePromise.then(function () {
                            resolved = true;
                        }).then(checker, checker);
                    }, function () {
                        expect().fail();
                    });
                });

                it('"catch" fires a handler', function () {
                    var host = 'localhost',
                        port = 11015,
                        streamName = 'newStream',
                        configUrl = '/v2/streams/' + streamName + '/info',
                        writeUrl = '/v2/streams/' + streamName,
                        textToSend = 'klasj ddkjas ldjas kljfasklj fklasfj a',

                        mockConfig = nock('http://' + host + ':' + port)
                            .get(configUrl)
                            .reply(200),
                        mockWrite = nock('http://' + host + ':' + port)
                            .post(writeUrl)
                            .reply(404),

                        streamClient = new StreamClient({
                            host: host,
                            port: port
                        });

                    var writerPromise = streamClient.createWriter(streamName),
                        rejected = false,
                        checker = function () {
                            expect(rejected).to.be.ok();
                        };

                    writerPromise.then(function (newWriter) {
                        var writePromise = newWriter.write(textToSend);

                        writePromise.catch(function () {
                            rejected = true;
                        }).then(checker, checker);
                    }, function () {
                        expect().fail();
                    });
                });

                it('"notify" fires a handler', function () {
                    var host = 'localhost',
                        port = 11015,
                        streamName = 'newStream',
                        configUrl = '/v2/streams/' + streamName + '/info',
                        writeUrl = '/v2/streams/' + streamName,
                        textToSend = 'klasj ddkjas ldjas kljfasklj fklasfj a',

                        mockConfig = nock('http://' + host + ':' + port)
                            .get(configUrl)
                            .reply(200),
                        mockWrite = nock('http://' + host + ':' + port)
                            .post(writeUrl)
                            .reply(404),

                        streamClient = new StreamClient({
                            host: host,
                            port: port
                        });


                    var writerPromise = streamClient.createWriter(streamName),
                        rejected = false,
                        checker = function () {
                            expect(rejected).to.be.ok();
                        };

                    writerPromise.then(function (newWriter) {
                        var writePromise = newWriter.write(textToSend);

                        writePromise.then(null, null, function () {
                            rejected = true;
                        }).then(checker, checker);
                    }, function () {
                        expect().fail();
                    });
                });
            });
        });
    });
});
