# Build procedure.

Build process is implemented with [Grunt](http://gruntjs.com/).

to set up all dependencies use next shell commands:

```
# npm install -g grunt-cli
# npm unstall -g bower
$ npm install
$ bower install
```

## Tests

```
$ grunt test
```

## Build

```
$ grunt build
```

## Default behaviour

```
$ grunt
```

Is equal to:
```
$ grunt test && grunt build
```

To run tests