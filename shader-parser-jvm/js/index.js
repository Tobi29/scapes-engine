(function (root, factory) {
  if (typeof define === 'function' && define.amd)
    define('ShaderParser_main', ['exports', 'antlr4'], factory);
  else if (typeof exports === 'object')
    factory(module.exports, require('antlr4'));
  else {
    if (typeof antlr4 === 'undefined') {
      throw new Error("Error loading module 'ShaderParser_main'. Its dependency 'antlr4' was not found. Please, check whether 'antlr4' is loaded prior to 'ShaderParser_main'.");
    }
    root.ShaderParser_main = factory(typeof ShaderParser_main === 'undefined' ? {} : ShaderParser_main, antlr4);
  }
}(this, function (_, antlr4) {
  'use strict';
  _.ScapesShaderLexer = require('./lib/org/tobi29/scapes/engine/utils/shader/ScapesShaderLexer.js').ScapesShaderLexer;
  _.ScapesShaderListener = require('./lib/org/tobi29/scapes/engine/utils/shader/ScapesShaderListener.js').ScapesShaderListener;
  _.ScapesShaderParser = require('./lib/org/tobi29/scapes/engine/utils/shader/ScapesShaderParser.js').ScapesShaderParser;
  return _;
}));
