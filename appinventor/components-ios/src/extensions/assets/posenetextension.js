// -*- mode: javascript; js-indent-level: 2; -*-
// Copyright © 2019 MIT, All rights reserved.

"use strict";

console.log("Posenet Extension using tfjs-core version " + tf.version_core);
console.log("Posenet Extension using tfjs-converter version " + tf.version_converter);

const ERROR_WEBVIEW_NO_MEDIA = 400;
const ERROR_MODEL_LOAD = 401;
const videoWidth = 300;
const videoHeight = 250;

const ERRORS = {
  ERROR_WEBVIEW_NO_MEDIA: "WebView does not support navigator.mediaDevices",
  ERROR_MODEL_LOAD: "Unable to load model"
};

let forwardCamera = true;
let running = false;

async function setupCamera() {
  if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
    PosenetExtension.error(ERROR_WEBVIEW_NO_MEDIA,
      ERRORS.ERROR_WEBVIEW_NO_MEDIA);
    return;
  }

  const video = document.getElementById('video');
  video.width = videoWidth;
  video.height = videoHeight;

  video.srcObject = await navigator.mediaDevices.getUserMedia({
    'audio': false,
    'video': {
      facingMode: forwardCamera ? 'user' : 'environment'
    }
  });

  return new Promise((resolve) => {
    video.onloadedmetadata = () => {
      resolve(video);
    }
  });
}

async function loadVideo() {
  const video = await setupCamera();
  video.play();
  return video;
}

const defaultQuantBytes = 2;
const defaultMobileNetMultiplier = 0.50;
const defaultMobileNetStride = 16;
const defaultMobileNetInputResolution = 257;

let minPoseConfidence = 0.1;
let stop = false;

function detectPoseInRealTime(video, net) {
  const canvas = document.getElementById('output');
  const ctx = canvas.getContext('2d');

  canvas.width = videoWidth;
  canvas.height = videoHeight;

  async function poseDetectionFrame() {
    let poses = [];
    const pose = await net.estimatePoses(video, {
      flipHorizontal: forwardCamera,
      decodingMethod: 'single-person'
    });
    poses = poses.concat(pose);

    ctx.clearRect(0, 0, videoWidth, videoHeight);

    ctx.save();
    ctx.scale(forwardCamera ? -1 : 1, 1);
    ctx.translate(forwardCamera ? -videoWidth : 0, 0);
    ctx.drawImage(video, 0, 0, videoWidth, videoHeight);
    ctx.restore();

    poses.forEach(({score, keypoints}) => {
      const dataURL = canvas.toDataURL();
      PosenetExtension.reportImage(dataURL);
      if (score >= minPoseConfidence) {
        PosenetExtension.reportResult(JSON.stringify(keypoints));
      }
    });

    if (!stop) requestAnimationFrame(poseDetectionFrame);
  }

  return poseDetectionFrame();
}

async function loadModel() {
  try {
    return posenet.load({
      architecture: 'MobileNetV1',
      outputStride: defaultMobileNetStride,
      inputResolution: defaultMobileNetInputResolution,
      multiplier: defaultMobileNetMultiplier,
      quantBytes: defaultQuantBytes
    });
  } catch (e) {
    PosenetExtension.error(ERROR_MODEL_LOAD,
      ERRORS[ERROR_MODEL_LOAD]);
    throw e;
  }
}

let net = null;

async function runModel() {
  let video;

  try {
    video = await loadVideo();
  } catch (e) {
    PosenetExtension.error(ERROR_WEBVIEW_NO_MEDIA,
      ERRORS.ERROR_WEBVIEW_NO_MEDIA);
    throw e;
  }

  running = true;
  return detectPoseInRealTime(video, net);
}

async function startVideo() {
  console.log('startVideo called');
  stop = false;
  return runModel();
}

// noinspection JSUnusedGlobalSymbols
function stopVideo() {
  console.log('stopVideo called');
  stop = true;
  running = false;
}

// noinspection JSUnusedGlobalSymbols
function setCameraFacingMode(useForward) {
  console.log('setCameraFacingMode(' + useForward + ')');
  forwardCamera = useForward;
  stop = true;
  requestAnimationFrame(() => {
    // noinspection JSIgnoredPromiseFromCall
    startVideo();
  })
}

// noinspection JSUnresolvedVariable
navigator.getUserMedia = navigator.getUserMedia ||
  navigator.webkitGetUserMedia || navigator.mozGetUserMedia;

loadModel().then(model => {
  net = model;
  PosenetExtension.ready();
});
