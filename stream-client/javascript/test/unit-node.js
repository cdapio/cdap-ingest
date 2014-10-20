var expect = require('expect.js'),
    httpsync = require('http-sync'),
    sinon = require('sinon'),
    nock = require('nock'),
    StreamClient = require('../src/streamclient');

describe('CDAP ingest tests', function () {
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
                createArgs = {
                    host: host,
                    port: port,
                    path: requestUrl,
                    ssl: false,
                    headers: { Authorization: '' },
                    data: '{}',
                    method: 'PUT',
                    async: false
                },

                mock = sinon.mock(httpsync),
                streamClient = new StreamClient({
                    host: host,
                    port: port
                });

            mock.expects('request').withArgs(createArgs).returns({
                write: function (data) {
                },
                end: function () {
                    return {
                        statusCode: 200,
                        body: ''
                    };
                }
            });

            streamClient.create(streamName);

            mock.verify();
            mock.restore();
        });

        it('"setTTL" for a valid stream', function () {
            var host = 'localhost',
                port = 10000,
                streamName = 'newStream',
                createUrl = '/v2/streams/' + streamName,
                configUrl = '/v2/streams/' + streamName + '/config',
                ttl = 86400,
                createArgs = {
                    host: host,
                    port: port,
                    path: createUrl,
                    ssl: false,
                    headers: { Authorization: '' },
                    data: '{}',
                    method: 'PUT',
                    async: false
                },
                ttlArgs = {
                    host: host,
                    port: port,
                    path: configUrl,
                    ssl: false,
                    headers: { Authorization: '' },
                    data: JSON.stringify({
                        ttl: ttl
                    }),
                    method: 'PUT',
                    async: false
                },

                mock = sinon.mock(httpsync),
                streamClient = new StreamClient({
                    host: host,
                    port: port
                });

            mock.expects('request').withArgs(createArgs).returns({
                write: function (data) {
                },
                end: function () {
                    return {
                        statusCode: 200,
                        body: ''
                    };
                }
            });
            mock.expects('request').withArgs(ttlArgs).returns({
                write: function (data) {
                },
                end: function () {
                    return {
                        statusCode: 200,
                        body: ''
                    };
                }
            });

            streamClient.create(streamName);
            streamClient.setTTL(streamName, ttl);

            mock.verify();
            mock.restore();
        });

        it('"setTTL" for an invalid stream', function () {
            var host = 'localhost',
                port = 10000,
                streamName = 'invalidStream',
                configUrl = '/v2/streams/' + streamName + '/config',
                ttl = 86400,
                ttlArgs = {
                    host: host,
                    port: port,
                    path: configUrl,
                    ssl: false,
                    headers: { Authorization: '' },
                    data: JSON.stringify({
                        ttl: ttl
                    }),
                    method: 'PUT',
                    async: false
                },

                mock = sinon.mock(httpsync),
                streamClient = new StreamClient({
                    host: host,
                    port: port
                });

            mock.expects('request').withArgs(ttlArgs).returns({
                write: function (data) {
                },
                end: function () {
                    return {
                        statusCode: 404,
                        body: ''
                    };
                }
            });

            expect(function () {
                streamClient.setTTL(streamName, ttl);
            }).to.throwError();

            mock.restore();
        });

        it('"getTTL" for a valid stream and a valid TTL value', function () {
            var host = 'localhost',
                port = 10000,
                streamName = 'newStream',
                createUrl = '/v2/streams/' + streamName,
                configUrl = '/v2/streams/' + streamName + '/info',
                ttl = 86400,
                createArgs = {
                    host: host,
                    port: port,
                    path: createUrl,
                    ssl: false,
                    headers: { Authorization: '' },
                    data: '{}',
                    method: 'PUT',
                    async: false
                },
                ttlArgs = {
                    host: host,
                    port: port,
                    path: configUrl,
                    ssl: false,
                    headers: { Authorization: '' },
                    data: '{}',
                    method: 'GET',
                    async: false
                },

                mock = sinon.mock(httpsync),
                streamClient = new StreamClient({
                    host: host,
                    port: port
                });

            mock.expects('request').withArgs(createArgs).returns({
                write: function (data) {
                },
                end: function () {
                    return {
                        statusCode: 200,
                        body: ''
                    };
                }
            });
            mock.expects('request').withArgs(ttlArgs).returns({
                write: function (data) {
                },
                end: function () {
                    return {
                        statusCode: 200,
                        body: JSON.stringify({
                            ttl: ttl
                        })
                    };
                }
            });

            streamClient.create(streamName);
            var respTTL = streamClient.getTTL(streamName);

            expect(respTTL).to.be.equal(ttl);

            mock.restore();
        });

        it('"getTTL" for a valid stream and a wrong TTL value', function () {
            var host = 'localhost',
                port = 10000,
                streamName = 'newStream',
                createUrl = '/v2/streams/' + streamName,
                configUrl = '/v2/streams/' + streamName + '/info',
                validTTL = 86400,
                invalidTTL = -1,
                createArgs = {
                    host: host,
                    port: port,
                    path: createUrl,
                    ssl: false,
                    headers: { Authorization: '' },
                    data: '{}',
                    method: 'PUT',
                    async: false
                },
                ttlArgs = {
                    host: host,
                    port: port,
                    path: configUrl,
                    ssl: false,
                    headers: { Authorization: '' },
                    data: '{}',
                    method: 'GET',
                    async: false
                },

                mock = sinon.mock(httpsync),
                streamClient = new StreamClient({
                    host: host,
                    port: port
                });

            mock.expects('request').withArgs(createArgs).returns({
                write: function (data) {
                },
                end: function () {
                    return {
                        statusCode: 200,
                        body: ''
                    };
                }
            });
            mock.expects('request').withArgs(ttlArgs).returns({
                write: function (data) {
                },
                end: function () {
                    return {
                        statusCode: 200,
                        body: JSON.stringify({
                            ttl: invalidTTL
                        })
                    };
                }
            });

            streamClient.create(streamName);
            var respTTL = streamClient.getTTL(streamName);

            expect(respTTL).not.to.be.equal(validTTL);

            mock.restore();
        });

        it('"getTTL" for an invalid stream', function () {
            var host = 'localhost',
                port = 10000,
                streamName = 'invalidStream',
                configUrl = '/v2/streams/' + streamName + '/info',
                ttl = 86400,
                ttlArgs = {
                    host: host,
                    port: port,
                    path: configUrl,
                    ssl: false,
                    headers: { Authorization: '' },
                    data: '{}',
                    method: 'GET',
                    async: false
                },

                mock = sinon.mock(httpsync),
                streamClient = new StreamClient({
                    host: host,
                    port: port
                });

            mock.expects('request').withArgs(ttlArgs).returns({
                write: function (data) {
                },
                end: function () {
                    return {
                        statusCode: 404,
                        body: ''
                    };
                }
            });

            expect(function () {
                streamClient.getTTL(streamName);
            }).to.throwError();

            mock.restore();
        });

        it('"truncate" for a valid stream', function () {
            var host = 'localhost',
                port = 10000,
                streamName = 'newStream',
                createUrl = '/v2/streams/' + streamName,
                truncateUrl = '/v2/streams/' + streamName + '/truncate',
                createArgs = {
                    host: host,
                    port: port,
                    path: createUrl,
                    ssl: false,
                    headers: { Authorization: '' },
                    data: '{}',
                    method: 'PUT',
                    async: false
                },
                truncateArgs = {
                    host: host,
                    port: port,
                    path: truncateUrl,
                    ssl: false,
                    headers: { Authorization: '' },
                    data: '{}',
                    method: 'POST',
                    async: false
                },

                mock = sinon.mock(httpsync),
                streamClient = new StreamClient({
                    host: host,
                    port: port
                });

            mock.expects('request').withArgs(createArgs).returns({
                write: function (data) {
                },
                end: function () {
                    return {
                        statusCode: 200,
                        body: ''
                    };
                }
            });
            mock.expects('request').withArgs(truncateArgs).returns({
                write: function (data) {
                },
                end: function () {
                    return {
                        statusCode: 200,
                        body: ''
                    };
                }
            });

            streamClient.create(streamName);
            streamClient.truncate(streamName);

            mock.verify();
            mock.restore();
        });

        it('"truncate" for an invalid stream', function () {
            var host = 'localhost',
                port = 10000,
                streamName = 'invalidStream',
                truncateUrl = '/v2/streams/' + streamName + '/truncate',
                truncateArgs = {
                    host: host,
                    port: port,
                    path: truncateUrl,
                    ssl: false,
                    headers: { Authorization: '' },
                    data: '{}',
                    method: 'POST',
                    async: false
                },

                mock = sinon.mock(httpsync),
                streamClient = new StreamClient({
                    host: host,
                    port: port
                });

            mock.expects('request').withArgs(truncateArgs).returns({
                write: function (data) {
                },
                end: function () {
                    return {
                        statusCode: 404,
                        body: ''
                    };
                }
            });

            expect(function () {
                streamClient.truncate(streamName);
            }).to.throwError();

            mock.restore();
        });

        it('"createWriter" creates a valid object', function () {
            var host = 'localhost',
                port = 10000,
                streamName = 'newStream',
                configUrl = '/v2/streams/' + streamName + '/info',
                ttlArgs = {
                    host: host,
                    port: port,
                    path: configUrl,
                    ssl: false,
                    headers: { Authorization: '' },
                    data: '{}',
                    method: 'GET',
                    async: false
                },

                mock = sinon.mock(httpsync),
                streamClient = new StreamClient({
                    host: host,
                    port: port
                });

            mock.expects('request').withArgs(ttlArgs).returns({
                write: function (data) {
                },
                end: function () {
                    return {
                        statusCode: 200,
                        body: JSON.stringify({
                            ttl: 86400
                        })
                    };
                }
            });

            var streamWriter = streamClient.createWriter(streamName);

            expect(streamWriter).to.have.property('write');

            mock.restore();
        });

        describe('StreamWriter', function () {
            it('"write" returns valid Promise object', function () {
                var host = 'localhost',
                    port = 10000,
                    streamName = 'newStream',
                    configUrl = '/v2/streams/' + streamName + '/info',
                    writeUrl = '/v2/streams/' + streamName,
                    textToSend = 'klasj ddkjas ldjas kljfasklj fklasfj a',
                    ttlArgs = {
                        host: host,
                        port: port,
                        path: configUrl,
                        ssl: false,
                        headers: { Authorization: '' },
                        data: '{}',
                        method: 'GET',
                        async: false
                    },

                    mock = sinon.mock(httpsync),
                    r = nock('http://' + host + ':' + port)
                        .defaultReplyHeaders({
                            'Content-Type': 'application/json'
                        })
                        .post(writeUrl)
                        .reply(200, "{}"),
                    streamClient = new StreamClient({
                        host: host,
                        port: port
                    });

                mock.expects('request').withArgs(ttlArgs).returns({
                    write: function (data) {
                    },
                    end: function () {
                        return {
                            statusCode: 200,
                            body: JSON.stringify({
                                ttl: 86400
                            })
                        };
                    }
                });

                var streamWriter = streamClient.createWriter(streamName),
                    promise = streamWriter.write(textToSend);

                expect(promise).to.have.property('then');
                expect(promise).to.have.property('catch');
                expect(promise).to.have.property('notify');
                expect(promise).to.have.property('reject');
                expect(promise).to.have.property('resolve');

                mock.restore();
            });

            describe('Promise states', function () {
                it('"resolve" fires a handler', function () {
                    var host = 'localhost',
                        port = 10000,
                        streamName = 'newStream',
                        configUrl = '/v2/streams/' + streamName + '/info',
                        writeUrl = '/v2/streams/' + streamName,
                        textToSend = 'klasj ddkjas ldjas kljfasklj fklasfj a',
                        ttlArgs = {
                            host: host,
                            port: port,
                            path: configUrl,
                            ssl: false,
                            headers: { Authorization: '' },
                            data: '{}',
                            method: 'GET',
                            async: false
                        },

                        mock = sinon.mock(httpsync),
                        r = nock('http://' + host + ':' + port)
                            .defaultReplyHeaders({
                                'Content-Type': 'application/json'
                            })
                            .post(writeUrl)
                            .reply(200, "{}"),

                        streamClient = new StreamClient({
                            host: host,
                            port: port
                        });

                    mock.expects('request').withArgs(ttlArgs).returns({
                        write: function (data) {
                        },
                        end: function () {
                            return {
                                statusCode: 200,
                                body: JSON.stringify({
                                    ttl: 86400
                                })
                            };
                        }
                    });

                    var streamWriter = streamClient.createWriter(streamName),
                        promise = streamWriter.write(textToSend),
                        resolved = false,
                        promiseHandler = function (status) {
                            resolved = true;
                        },
                        promiseChecker = function () {
                            expect(resolved).to.be.ok();
                        };

                    promise.then(promiseHandler).then(promiseChecker, promiseChecker);

                    mock.restore();
                });

                it('"catch" fires a handler', function () {
                    var host = 'localhost',
                        port = 10000,
                        streamName = 'newStream',
                        configUrl = '/v2/streams/' + streamName + '/info',
                        writeUrl = '/v2/streams/' + streamName,
                        textToSend = 'klasj ddkjas ldjas kljfasklj fklasfj a',
                        ttlArgs = {
                            host: host,
                            port: port,
                            path: configUrl,
                            ssl: false,
                            headers: { Authorization: '' },
                            data: '{}',
                            method: 'GET',
                            async: false
                        },

                        mock = sinon.mock(httpsync),
                        r = nock('http://' + host + ':' + port)
                            .defaultReplyHeaders({
                                'Content-Type': 'application/json'
                            })
                            .post(writeUrl)
                            .reply(404, "{}"),

                        streamClient = new StreamClient({
                            host: host,
                            port: port
                        });

                    mock.expects('request').withArgs(ttlArgs).returns({
                        write: function (data) {
                        },
                        end: function () {
                            return {
                                statusCode: 200,
                                body: JSON.stringify({
                                    ttl: 86400
                                })
                            };
                        }
                    });

                    var streamWriter = streamClient.createWriter(streamName),
                        promise = streamWriter.write(textToSend),
                        resolved = false,
                        promiseHandler = function (status) {
                            resolved = true;
                        },
                        promiseChecker = function () {
                            expect(resolved).to.be.ok();
                        };

                    promise.catch(promiseHandler).then(promiseChecker, promiseChecker);

                    mock.restore();
                });

                it('"notify" fires a handler', function () {
                    var host = 'localhost',
                        port = 10000,
                        streamName = 'newStream',
                        configUrl = '/v2/streams/' + streamName + '/info',
                        writeUrl = '/v2/streams/' + streamName,
                        textToSend = 'klasj ddkjas ldjas kljfasklj fklasfj a',
                        ttlArgs = {
                            host: host,
                            port: port,
                            path: configUrl,
                            ssl: false,
                            headers: { Authorization: '' },
                            data: '{}',
                            method: 'GET',
                            async: false
                        },

                        mock = sinon.mock(httpsync),
                        r = nock('http://' + host + ':' + port)
                            .defaultReplyHeaders({
                                'Content-Type': 'application/json'
                            })
                            .post(writeUrl)
                            .reply(404, "{}"),

                        streamClient = new StreamClient({
                            host: host,
                            port: port
                        });

                    mock.expects('request').withArgs(ttlArgs).returns({
                        write: function (data) {
                        },
                        end: function () {
                            return {
                                statusCode: 200,
                                body: JSON.stringify({
                                    ttl: 86400
                                })
                            };
                        }
                    });

                    var streamWriter = streamClient.createWriter(streamName),
                        promise = streamWriter.write(textToSend),
                        resolved = false,
                        promiseHandler = function (status) {
                            resolved = true;
                        },
                        promiseChecker = function () {
                            expect(resolved).to.be.ok();
                        };

                    promise.then(null, null, promiseHandler).then(promiseChecker, promiseChecker);

                    mock.restore();
                });
            });
        });
    });
});