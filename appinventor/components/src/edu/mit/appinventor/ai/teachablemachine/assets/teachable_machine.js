"use strict";

console.log("PersonalImageClassifier: Using TensorFlow.js version " + tf.version.tfjs);

const TRANSFER_MODEL_PREFIX = "https://appinventor.mit.edu/personal-image-classifier/transfer/";
const TRANSFER_MODEL_SUFFIX = "_model.json";

const PERSONAL_MODEL_PREFIX = "https://appinventor.mit.edu/personal-image-classifier/personal/";
const PERSONAL_MODEL_JSON_SUFFIX = "model.json";
const PERSONAL_MODEL_WEIGHTS_SUFFIX = "model.weights.bin";
const PERSONAL_MODEL_LABELS_SUFFIX = "model_labels.json";
const TRANSFER_MODEL_INFO_SUFFIX = "transfer_model.json";

const IMAGE_SIZE = 224;

// make sure error codes are consistent with those defined in PersonalImageClassifier.java
const ERROR_CLASSIFICATION_NOT_SUPPORTED = -1;
const ERROR_CLASSIFICATION_FAILED = -2;
const ERROR_CANNOT_TOGGLE_CAMERA_IN_IMAGE_MODE = -3;
const ERROR_CANNOT_CLASSIFY_IMAGE_IN_VIDEO_MODE = -4;
const ERROR_CANNOT_CLASSIFY_VIDEO_IN_IMAGE_MODE = -5;
const ERROR_INVALID_INPUT_MODE = -6;
// Error -10 was failure to load labels
const ERROR_FAILED_TO_START_VIDEO = -11;

// Inputs are passed through an activation of the transfer before being fed into
// the user provided model
let transferModel;
let model, maxPredictions;

// Data required to use the model
let modelLabels;
let transferModelInfo;

let topk_predictions;

let img = document.createElement("img");
img.width = window.innerWidth;
img.style.display = "block";

let frontFacing = false;
let isVideoMode = false;
let isRunning = false;
let minClassTime = 0;
let lastClassification = new Date();
let webcamHolder = document.getElementById('webcam-box');
let video = /** @type {HTMLVideoElement} */ (document.getElementById('webcam'));
webcamHolder.style.display = 'none';
video.style.display = "none";

//testing

// Loading the model

const URL = "https://teachablemachine.withgoogle.com/models/uZCBCIj3D/";


const modelURL = URL + "model.json"
const metadataURL = URL + "metadata.json"

model = await tmImage.load(modelURL, metadataURL);

maxPredictions = model.getTotalClasses();

console.log("Model Loaded !!")



// Inputing image data
// let androidWebcam = new tmImage.webcamHolder(200,200, flip);
// await androidWebcam.setup()
// await androidWebcam.play()
// window.requestAnimationFrame(loop)


// async function loop() {
//   androidWebcam.update()
//   await predict(img);
//   window.requestAnimationFrame(loop)
// }


// async function loadTransferModel(modelName, modelActivation) {
//   const transferModel = await tf.loadModel(TRANSFER_MODEL_PREFIX + modelName + TRANSFER_MODEL_SUFFIX);

//   // Return an internal activation of the transfer model.
//   const layer = transferModel.getLayer(modelActivation);
//   return tf.model({inputs: transferModel.inputs, outputs: layer.output});
// }

// async function loadModelFile(url, json) {
//   const modelFileResponse = await fetch(url);

//   console.log("Done fetching file");

//   if (json) {
//     return await modelFileResponse.json();
//   }
//   return await modelFileResponse.blob();
// }

// From https://stackoverflow.com/questions/27159179/how-to-convert-blob-to-file-in-javascript
// function blobToFile(blob, fileName){
//     // A Blob() is almost a File() - it's just missing the two properties below which we will add
//     blob.lastModifiedDate = new Date();
//     blob.name = fileName;
//     return blob;
// }

// const loadModel = async () => {
//   try {
//     // Loads the transfer model
//     transferModelInfo = await loadModelFile(PERSONAL_MODEL_PREFIX + TRANSFER_MODEL_INFO_SUFFIX, true);
//     transferModel = await loadTransferModel(transferModelInfo['name'], transferModelInfo['lastLayer']);

//     // Loads the user's personal model
//     const modelTopologyBlob = await loadModelFile(PERSONAL_MODEL_PREFIX + PERSONAL_MODEL_JSON_SUFFIX, false);
//     const modelTopologyFile = blobToFile(modelTopologyBlob, PERSONAL_MODEL_JSON_SUFFIX);

//     const modelWeightsBlob = await loadModelFile(PERSONAL_MODEL_PREFIX + PERSONAL_MODEL_WEIGHTS_SUFFIX, false);
//     const modelWeightsFile = blobToFile(modelWeightsBlob, PERSONAL_MODEL_WEIGHTS_SUFFIX);

//     model = await tf.loadModel(tf.io.browserFiles([modelTopologyFile, modelWeightsFile]));

//     // Loads the model labels mapping
//     modelLabels = await loadModelFile(PERSONAL_MODEL_PREFIX + PERSONAL_MODEL_LABELS_SUFFIX, true);
//     topk_predictions = Math.min(3, Object.keys(modelLabels).length);

//     const zeros = tf.zeros([1, IMAGE_SIZE, IMAGE_SIZE, 3]);
//     transferModel.predict(zeros).dispose();
//     zeros.dispose();
//     console.log("PersonalImageClassifier: transfer model activation and personal model are ready");
//     PersonalImageClassifier.ready(JSON.stringify(Object.values(modelLabels)));
//   } catch (error) {
//     console.log("PersonalImageClassifier: " + error);
//     PersonalImageClassifier.error(ERROR_CLASSIFICATION_NOT_SUPPORTED);
//   }
// };


/**
 * Crops an image tensor so we get a square image with no white space.
 * @param {tf.tensor4d} img An input image Tensor to crop.
 */
function cropImage(img) {
  const size = Math.min(img.shape[0], img.shape[1]);
  const centerHeight = img.shape[0] / 2;
  const beginHeight = centerHeight - (size / 2);
  const centerWidth = img.shape[1] / 2;
  const beginWidth = centerWidth - (size / 2);
  const slice = img.slice([beginHeight, beginWidth, 0], [size, size, 3]);
  return size === IMAGE_SIZE ? slice : tf.image.resizeBilinear(slice, [IMAGE_SIZE, IMAGE_SIZE]);
}

/**
 * Predict the class of an image.
 *
 * @param {HTMLImageElement|HTMLVideoElement} pixels
 * @param {boolean=false} crop
 * @returns {Promise<void>}
 */
async function predict(pixels, crop) {
  let predictions;
  try {
    const logits = tf.tidy(() => {
      const img = crop ? cropImage(tf.fromPixels(pixels)) :
        tf.image.resizeBilinear(tf.fromPixels(pixels).toFloat(), [IMAGE_SIZE, IMAGE_SIZE]);
      const batchedImage = img.expandDims(0);
      const scaled = batchedImage.toFloat().div(tf.scalar(127)).sub(tf.scalar(1));

      // Make a prediction, first using the transfer model activation and then
      // feeding that into the user provided model
      // const activation = transferModel.predict(scaled);
      predictions = await model.predict(scaled);
      return predictions.as1D();
    });

    const topPredictions = await logits.topk(topk_predictions);

    const predictionIndices = await topPredictions.indices.data();
    const predictionValues = await topPredictions.values.data();

    let result = [];
    logits.dispose();

    // var prediction = 

    for (let i = 0; i < maxPredictions; i++) {
      // const currentIndex = predictionIndices[i];
      // const currentValue = predictionValues[i];
      const currentValue = predictions[i].probability;

      // const labelName = modelLabels[currentIndex];
      const labelName = predictions[i].className

      result.push([labelName, currentValue.toFixed(5)]);
    }

    console.log("TeachableMachine: prediction is " + JSON.stringify(result));
    TeachableMachine.reportResult(JSON.stringify(result));
  } catch (error) {
    console.log("TeachableMachine: " + error);
    TeachableMachine.error(ERROR_CLASSIFICATION_NOT_SUPPORTED);
  }
}

function updateVideoSize() {
  let windowWidth = document.body.offsetWidth;
  let windowHeight = document.body.offsetHeight;
  let size = Math.min(windowWidth, windowHeight);
  webcamHolder.style.width = size + 'px';
  webcamHolder.style.height = size + 'px';
  let width = video.videoWidth;
  let height = video.videoHeight;
  let aspectRatio = width / height;
  if (width >= height) {
    video.width = aspectRatio * size;
    video.height = size;
    video.style.left = (size - video.width) / 2.0 + 'px';
    video.style.top = '0px';
  } else {
    video.height = size / aspectRatio;
    video.width = size;
    video.style.left = '0px';
    video.style.top = (size - video.height) / 2.0 + 'px';
  }
}

video.addEventListener('loadeddata' , () => {
  updateVideoSize();
}, false);

document.body.appendChild(img);

function startVideo() {
  if (isVideoMode) {
    navigator.mediaDevices.getUserMedia({
      video: {facingMode: frontFacing ? "user" : "environment"},
      audio: false
    })
      .then(stream => (video.srcObject = stream))
      .catch(e => {
        TeachableMachine.error(ERROR_FAILED_TO_START_VIDEO);
        console.error(e);
      });
    webcamHolder.style.display = 'block';
    video.style.display = "block";
    if (frontFacing) {  // flip the front facing camera to make it 'natural'
      video.style.transform = 'scaleX(-1)';
    } else {
      video.style.transform = '';
    }
  }
}

function stopVideo() {
  if (isVideoMode && video.srcObject) {
    video.srcObject.getTracks().forEach(t => t.stop());
    webcamHolder.style.display = 'none';
    video.style.display = "none";
  }
}

// Called from TeachableMachine.java
// noinspection JSUnusedGlobalSymbols
function toggleCameraFacingMode() {
  if (isVideoMode) {
    stopVideo();
    frontFacing = !frontFacing;
    startVideo();
  } else {
    TeachableMachine.error(ERROR_CANNOT_TOGGLE_CAMERA_IN_IMAGE_MODE);
  }
}

// Called from TeachableMachine.java
// noinspection JSUnusedGlobalSymbols
function classifyImageData(imageData) {
  if (!isVideoMode) {
    img.onload = function() {
      predict(img).catch(() => TeachableMachine.error(ERROR_CLASSIFICATION_FAILED));
    }
    img.src = "data:image/png;base64," + imageData;
  } else {
    TeachableMachine.error(ERROR_CANNOT_CLASSIFY_IMAGE_IN_VIDEO_MODE);
  }
}

// Called from TeachableMachine.java
// noinspection JSUnusedGlobalSymbols
function classifyVideoData() {
  if (isVideoMode) {
    predict(video, true).catch(() => TeachableMachine.error(ERROR_CLASSIFICATION_FAILED));
  } else {
    TeachableMachine.error(ERROR_CANNOT_CLASSIFY_VIDEO_IN_IMAGE_MODE);
  }
}

function cvcHandler() {
  if (!isRunning || !isVideoMode) {
    return;
  }
  let now = new Date();
  if (now.getTime() - lastClassification.getTime() > minClassTime) {
    lastClassification = now;
    predict(video, true).then(() => requestAnimationFrame(cvcHandler));
  } else {
    requestAnimationFrame(cvcHandler);
  }
}

// Called from TeachableMachine.java
// noinspection JSUnusedGlobalSymbols
function startVideoClassification() {
  if (isRunning || !isVideoMode) {
    return;
  }
  isRunning = true;
  setTimeout(cvcHandler, 16);
}

// Called from TeachableMachine.java
// noinspection JSUnusedGlobalSymbols
function stopVideoClassification() {
  if (!isRunning || !isVideoMode) {
    return;
  }
  isRunning = false;
}

function setInputMode(inputMode) {
  if (inputMode === "image" && isVideoMode) {
    stopVideo();
    isVideoMode = false;
    img.style.display = "block";
  } else if (inputMode === "video" && !isVideoMode) {
    img.style.display = "none";
    isVideoMode = true;
    startVideo();
  } else if (inputMode !== "image" && inputMode !== "video") {
    TeachableMachine.error(ERROR_INVALID_INPUT_MODE);
  }
}

window.addEventListener("resize", function() {
  img.width = window.innerWidth;
  video.width = window.innerWidth;
  video.height = video.videoHeight * window.innerWidth / video.videoWidth;
});

// loadModel().catch(() => TeachableMachine.error(ERROR_CLASSIFICATION_NOT_SUPPORTED));

window.addEventListener('orientationchange', function() {
  if (isVideoMode) {
    // The event fires before the video actually rotates, so we delay updating the frame until
    // a later time.
    setTimeout(updateVideoSize, 500);
  }
});
