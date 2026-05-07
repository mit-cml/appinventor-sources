(function(root) {
  'use strict';
  var slide = 0;
  var resolveFunction = undefined;

  function _isFunction(v) {
    return typeof v === 'function';
  }

  function _result(v) {
    return _isFunction(v) ? v() : v;
  }

  var toString = Object.prototype.toString;

  function Spectrogram(canvas, options) {
    if (!(this instanceof Spectrogram)) {
      return new Spectrogram(canvas, options);
    }

    var baseCanvasOptions = options.canvas || {};
    this._audioEnded = null;
    this._paused = null;
    this._pausedAt = null;
    this._startedAt = null;
    this._sources = {
      audioBufferStream: null,
      userMediaStream: null
    };
    this._baseCanvas = canvas;
    this._baseCanvasContext = this._baseCanvas.getContext('2d');

    this._baseCanvas.width = _result(baseCanvasOptions.width) || this._baseCanvas.width;
    this._baseCanvas.height = _result(baseCanvasOptions.height) || this._baseCanvas.height;

    window.onresize = function() {
      this._baseCanvas.width = _result(baseCanvasOptions.width) || this._baseCanvas.width;
      this._baseCanvas.height = _result(baseCanvasOptions.height) || this._baseCanvas.height;
    }.bind(this);

    var audioOptions = options.audio || {};
    this.audio = audioOptions;

    var colors = [];

    if (typeof options.colors === 'function') {
      colors = options.colors(275);
    } else {
      colors = this._generateDefaultColors(275);
    }

    this._colors = colors;

    this._baseCanvasContext.fillStyle = this._getColor(0);
    this._baseCanvasContext.fillRect(0, 0, this._baseCanvas.width, this._baseCanvas.height);
  }

  Spectrogram.prototype._init = function() {
    var source = this._sources.audioBufferStream;
    source.scriptNode = source.audioContext.createScriptProcessor(2048, 1, 1);
    source.scriptNode.connect(source.audioContext.destination);
    source.scriptNode.onaudioprocess = function(event) {
      var array = new Uint8Array(source.analyser.frequencyBinCount);
      source.analyser.getByteFrequencyData(array);

      this._draw(array, source.canvasContext);
    }.bind(this);

    source.sourceNode.onended = function() {
      this.clear();
    }.bind(this);

    source.analyser = source.audioContext.createAnalyser();
    source.analyser.smoothingTimeConstant = 0;
    source.analyser.fftSize = 4096;

    source.analyser.connect(source.scriptNode);
    source.sourceNode.connect(source.analyser);
    if (this.audio.enable) {
      source.sourceNode.connect(source.audioContext.destination);
    }
  };

  Spectrogram.prototype._draw = function(array, canvasContext) {
      console.log(slide)
      if (this._paused) {
        return false;
      }

      var canvas = canvasContext.canvas;
      var width = canvas.width;
      var height = canvas.height;
      var tempCanvasContext = canvasContext._tempContext;
      var tempCanvas = tempCanvasContext.canvas;
      tempCanvasContext.drawImage(canvas, 0, 0, width, height);

      for (var i = 0; i < array.length; i++) {
        var value = array[i];
        canvasContext.fillStyle = this._getColor(value);
        if (this._audioEnded) {
          canvasContext.fillStyle = this._getColor(0);
        }
        canvasContext.fillRect(slide, height - i, 3, 1);
      }

      // canvasContext.translate(-1, 0);

      // draw prev canvas before translation
      canvasContext.drawImage(tempCanvas, 0, 0, width, height, 0, 0, width, height);
      canvasContext.drawImage(tempCanvas, 0, 0, width, height, 0, 0, width, height);
      // reset transformation matrix
      canvasContext.setTransform(1, 0, 0, 1, 0, 0);

      this._baseCanvasContext.drawImage(canvas, 0, 0, width, height);

      if (!this._audioEnded) {slide += 3;}

  };

  Spectrogram.prototype._startMediaStreamDraw = function(analyser, canvasContext) {
    window.requestAnimationFrame(this._startMediaStreamDraw.bind(this, analyser, canvasContext));
    var audioData = new Uint8Array(analyser.frequencyBinCount);
    analyser.getByteFrequencyData(audioData);
    this._draw(audioData, canvasContext);
  };

  Spectrogram.prototype.connectSource = function(audioBuffer, audioContext) {
    var source = this._sources.audioBufferStream || {};

    // clear current audio process
    if (toString.call(source.scriptNode) === '[object ScriptProcessorNode]') {
      source.scriptNode.onaudioprocess = null;
    }

    if (toString.call(audioBuffer) === '[object AudioBuffer]') {
      audioContext = (!audioContext && source.audioBuffer.context) || (!audioContext && source.audioContext) || audioContext;

      var sourceNode = audioContext.createBufferSource();
      sourceNode.buffer = audioBuffer;

      var canvasContext = source.canvasContext;

      if (!source.canvasContext) {
        var canvas = document.createElement('canvas');
        canvas.width = this._baseCanvas.width;
        canvas.height = this._baseCanvas.height;
        canvasContext = canvas.getContext('2d');

        var tempCanvas = document.createElement('canvas');
        tempCanvas.width = canvas.width;
        tempCanvas.height = canvas.height;

        canvasContext._tempContext = tempCanvas.getContext('2d');
      }

      source = {
        audioBuffer: audioBuffer,
        audioContext: audioContext,
        sourceNode: sourceNode,
        analyser: null,
        scriptNode: null,
        canvasContext: canvasContext
      };

      this._sources.audioBufferStream = source;
      this._init();
    }

    if (toString.call(audioBuffer) === '[object AnalyserNode]') {
      source = this._sources.userMediaStream || {};
      source.analyser = audioBuffer;
      this._sources.userMediaStream = source;
    }
  };

  Spectrogram.prototype.start = function(offset, resolve) {
    //set global resolveFunction to resolve that resolves canvasPromise (resolution in stop)
    resolveFunction = resolve;

    var source = this._sources.audioBufferStream;
    var sourceMedia = this._sources.userMediaStream;

    if (source && source.sourceNode) {
      source.sourceNode.start(0, offset||0);
      this._audioEnded = false;
      this._paused = false;
      this._startedAt = Date.now();
    }

    // media stream uses an analyser for audio data
    if (sourceMedia && sourceMedia.analyser) {
      source = sourceMedia;
      var canvas = document.createElement('canvas');
      canvas.width = this._baseCanvas.width;
      canvas.height = this._baseCanvas.height;
      var canvasContext = canvas.getContext('2d');

      var tempCanvas = document.createElement('canvas');
      tempCanvas.width = canvas.width;
      tempCanvas.height = canvas.height;

      canvasContext._tempContext = tempCanvas.getContext('2d');

      this._startMediaStreamDraw(source.analyser, canvasContext);
    }
  };

  Spectrogram.prototype.stop = function() {

    var source = this._sources[Object.keys(this._sources)[0]];
    if (source && source.sourceNode) {
      source.sourceNode.stop();
    }
    slide = 0;
    this._audioEnded = true;

    resolveFunction() //resolve canvasPromise

  };

  Spectrogram.prototype.pause = function() {
    this.stop();
    this._paused = true;
    this._pausedAt += Date.now() - this._startedAt;
  };

  Spectrogram.prototype.resume = function(offset) {
    var source = this._sources[Object.keys(this._sources)[0]];
    this._paused = false;
    if (this._pausedAt) {
      this.connectSource(source.audioBuffer, source.audioContext);
      this.start(offset || (this._pausedAt / 1000));
    }
  };

  Spectrogram.prototype.clear = function(canvasContext) {
    var source = this._sources[Object.keys(this._sources)[0]];

    this.stop();

    if (toString.call(source.scriptNode) === '[object ScriptProcessorNode]') {
      source.scriptNode.onaudioprocess = null;
    }

    // canvasContext = canvasContext || source.canvasContext;
    // var canvas = canvasContext.canvas;
    // canvasContext.clearRect(0, 0, canvas.width, canvas.height);
    // canvasContext._tempContext.clearRect(0, 0, canvas.width, canvas.height);
    // this._baseCanvasContext.clearRect(0, 0, canvas.width, canvas.height);
  };

  Spectrogram.prototype._generateDefaultColors = function(steps) {
    var frequency = Math.PI / steps;
    var amplitude = 127;
    var center = 128;
    var slice = (Math.PI / 2) * 3.1;
    var colors = [];

    function toRGBString(v) {
      return 'rgba(' + [v,v,v,1].toString() + ')';
    }

    for (var i = 0; i < steps; i++) {
      var v = (Math.sin((frequency * i) + slice) * amplitude + center) >> 0;

      colors.push(toRGBString(v));
    }

    return colors;
  };

  Spectrogram.prototype._getColor = function(index) {
    var color = this._colors[index>>0];

    if (typeof color === 'undefined') {
      color = this._colors[0];
    }

    return color;
  };

  if (typeof exports !== 'undefined') {
    if (typeof module !== 'undefined' && module.exports) {
      exports = module.exports = Spectrogram;
    }
    exports.Spectrogram = Spectrogram;
  } else {
    root.Spectrogram = Spectrogram;
  }

})(this);
