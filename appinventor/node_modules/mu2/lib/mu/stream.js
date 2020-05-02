var util = require('util')
  , Stream = require('stream').Stream;

function MuStream() {
  Stream.call(this);
  this.paused = false;
  this.readable = true;
}

util.inherits(MuStream, Stream);
module.exports = MuStream;


MuStream.prototype.pause = function () {
  this.paused = true;
}

MuStream.prototype.resume = function () {
  this.paused = false;
  this.emit('resumed');
};
