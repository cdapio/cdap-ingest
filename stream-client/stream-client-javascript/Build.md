# Cask JavaScript build process

## Build process is implemented with [Grunt](http://gruntjs.com/)

To build library next steps should be performed from command line:

```
#> npm install -g grunt-cli
#> npm install -g bower
$> cd project_root
$> npm install
$> bower install
$> grunt
```

## Build process commands

1. *grunt test*

Will make library testing.

2. *grunt build*

Will build library for both browser and NodeJS.
Build artifactes will be located at *dist* directory.

3. *grunt*

*grunt test* + *grunt build*
