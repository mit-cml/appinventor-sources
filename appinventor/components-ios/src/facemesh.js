// -*- mode: javascript; js-indent-level: 2; -*-
// Copyright © 2019 MIT, All rights reserved.

"use strict";

console.log("FaceExtension using tfjs-core version " + tf.version_core);
console.log("FaceExtension using tfjs-converter version " + tf.version_converter);

const ERROR_WEBVIEW_NO_MEDIA = 400;
const ERROR_MODEL_LOAD = 401;
var videoWidth = 300;
var videoHeight = 250;
var showMesh = "0";

const ERRORS = {
  400: "WebView does not support navigator.mediaDevices",
  401: "Unable to load model"
};

let forwardCamera = true;
let running = false;

async function setupCamera() {
  if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
    PosenetExtension.error(ERROR_WEBVIEW_NO_MEDIA,
      ERRORS[ERROR_WEBVIEW_NO_MEDIA]);
    return;
  }

  const video = document.getElementById('video');
  video.width = 0;
  video.height = 0;

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

let stop = false;

function runClassifier(video, net) {
  const canvas = document.getElementById('output');
  const ctx = canvas.getContext('2d');

  canvas.width = videoWidth;
  canvas.height = videoHeight;

  async function classifyFrame() {
    const predictions = await net.estimateFaces(video, false, true);

    ctx.clearRect(0, 0, videoWidth, videoHeight);

    ctx.save();
    ctx.scale(forwardCamera ? -1 : 1, 1);
    ctx.translate(forwardCamera ? -videoWidth : 0, 0);
    ctx.drawImage(video, 0, 0, videoWidth, videoHeight);
    ctx.restore();

    if (predictions.length > 0) {
      const rightCheek = predictions[0].scaledMesh[234];
      const leftCheek = predictions[0].scaledMesh[454];
      const forehead = predictions[0].scaledMesh[10];
      const chin = predictions[0].scaledMesh[152];
      const rightEyeInnerCorner = predictions[0].scaledMesh[133];
      const leftEyeInnerCorner = predictions[0].scaledMesh[362];
      const mouthTop = predictions[0].scaledMesh[0];
      const mouthBottom = predictions[0].scaledMesh[17];
      const rightEyeTop = predictions[0].scaledMesh[159];
      const rightEyeBottom = predictions[0].scaledMesh[145];
      const leftEyeTop = predictions[0].scaledMesh[386];
      const leftEyeBottom = predictions[0].scaledMesh[374];
      const rightEarStart = predictions[0].scaledMesh[67];
      const leftEarStart = predictions[0].scaledMesh[297];
      const noseBottom = predictions[0].scaledMesh[164];
      const rightNoseTop = predictions[0].scaledMesh[188];
      const leftNoseTop = predictions[0].scaledMesh[412];
      const leftEyebrow = predictions[0].scaledMesh[443];
      const rightEyebrow = predictions[0].scaledMesh[223];

      let allPoints = {};
      for (let i = 0; i < predictions[0].scaledMesh.length; i++) {
        allPoints[i.toString()] = {x : predictions[0].scaledMesh[i][0] + 480, y: predictions[0].scaledMesh[i][1] - 20};
      }

      const newObj = {"leftCheek" : {x: leftCheek[0] + 480, y: leftCheek[1]-20, z: leftCheek[2]},
                      "rightCheek" : {x : rightCheek[0] + 480, y: rightCheek[1]-20, z: rightCheek[2]},
                      "leftEyebrow" : {x: leftEyebrow[0] + 480, y: leftEyebrow[1]-20, z: leftEyebrow[2]},
                      "rightEyebrow" : {x : rightEyebrow[0] + 480, y: rightEyebrow[1]-20, z: rightEyebrow[2]},
                      "forehead": {x : forehead[0] + 480, y: forehead[1]-20, z: forehead[2]},
                      "chin": {x : chin[0] + 480, y: chin[1]-20, z: chin[2]},
                      "leftEyeInnerCorner": {x : leftEyeInnerCorner[0]+ 480, y: leftEyeInnerCorner[1]-20, z: leftEyeInnerCorner[2]},
                      "rightEyeInnerCorner": {x : rightEyeInnerCorner[0]+ 480, y: rightEyeInnerCorner[1]-20, z: rightEyeInnerCorner[2]},
                      "mouthTop": {x : mouthTop[0]+ 480, y: mouthTop[1]-20, z: mouthTop[2]},
                      "mouthBottom": {x : mouthBottom[0]+ 480, y: mouthBottom[1]-20, z: mouthBottom[2]},
                      "leftEyeTop": {x : leftEyeTop[0]+ 480, y: leftEyeTop[1]-20, z: leftEyeTop[2]},
                      "leftEyeBottom": {x : leftEyeBottom[0]+ 480, y: leftEyeBottom[1]-20, z: leftEyeBottom[2]},
                      "rightEyeTop": {x : rightEyeTop[0]+ 480, y: rightEyeTop[1]-20, z: rightEyeTop[2]},
                      "rightEyeBottom": {x : rightEyeBottom[0]+ 480, y: rightEyeBottom[1]-20, z: rightEyeBottom[2]},
                      "rightEarStart": {x : rightEarStart[0]+ 480, y: rightEarStart[1]-20, z: rightEarStart[2]},
                      "leftEarStart": {x : leftEarStart[0]+ 480, y: leftEarStart[1]-20, z: leftEarStart[2]},
                      "noseBottom": {x : noseBottom[0]+ 480, y: noseBottom[1]-20, z: noseBottom[2]},
                      "rightNoseTop": {x : rightNoseTop[0]+ 480, y: rightNoseTop[1]-20, z: rightNoseTop[2]},
                      "leftNoseTop": {x : leftNoseTop[0]+ 480, y: leftNoseTop[1]-20, z: leftNoseTop[2]},
                      "allPoints": allPoints
                      };

      FaceExtension.reportResult(JSON.stringify(newObj));

      if (showMesh == "1") {
        for (let i = 0; i < predictions[0].scaledMesh.length; i++) {
          ctx.font = "3pt Calibri";
          var currX = (predictions[0].scaledMesh[i][0] + 470.0) / 480.0 * videoWidth;
          var currY = (predictions[0].scaledMesh[i][1] - 7.0) / 620.0 * videoHeight;
          ctx.fillText(i.toString(), currX, currY);
          ctx.save();
        }
      }
      
    }

    const dataURL = canvas.toDataURL();
    FaceExtension.reportImage(dataURL);


    if (!stop) requestAnimationFrame(classifyFrame);
  }

  return classifyFrame();
}

async function loadModel() {
  try {
    return facemesh.load({
      maxFaces: 1
    });
  } catch (e) {
    FaceExtension.error(ERROR_MODEL_LOAD,
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
    FaceExtension.error(ERROR_WEBVIEW_NO_MEDIA,
      ERRORS[ERROR_WEBVIEW_NO_MEDIA]);
    throw e;
  }

  running = true;
  return runClassifier(video, net);
}

function turnMeshOn() {
  showMesh = "1";
}

function turnMeshOff() {
  showMesh = "0";
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
  FaceExtension.ready();
});

