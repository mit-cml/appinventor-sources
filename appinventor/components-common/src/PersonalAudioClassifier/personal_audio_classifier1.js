"use strict";

console.log("PersonalAudioClassifier: Using TensorFlow.js version " + tf.version.tfjs);

const TRANSFER_MODEL_PREFIX = "appinventor://personal-audio-classifier/transfer/";
const TRANSFER_MODEL_SUFFIX = "_model.json";

const PERSONAL_MODEL_PREFIX = "appinventor://personal-audio-classifier/personal/";
const PERSONAL_MODEL_JSON_SUFFIX = "model.json";
const PERSONAL_MODEL_WEIGHTS_SUFFIX = "model.weights.bin";
const PERSONAL_MODEL_LABELS_SUFFIX = "model_labels.json";
const TRANSFER_MODEL_INFO_SUFFIX = "transfer_model.json";

const IMAGE_SIZE = 200;

// make sure error codes are consistent with those defined in PersonalImageClassifier.java
const ERROR_CLASSIFICATION_NOT_SUPPORTED = -1;
const ERROR_CLASSIFICATION_FAILED = -2;
const ERROR_CANNOT_CREATE_SPECTROGRAM = -10;

// Inputs are passed through an activation of the transfer before being fed into
// the user provided model
let transferModel;
let model;

// Data required to use the model
let modelLabels;
let transferModelInfo;

let topk_predictions;


//RECORDER.JS VARS

//webkitURL is deprecated but nevertheless
URL = window.URL || window.webkitURL;

var gumStream;            //stream from getUserMedia()
var rec;              //Recorder.js object
var input;              //MediaStreamAudioSourceNode we'll be recording

// shim for AudioContext when it's not avb.
var AudioContext = window.AudioContext || window.webkitAudioContext;
var audioContext //audio context to help us record

var recordButton = document.getElementById("recordButton");

//add events to those 2 buttons
recordButton.addEventListener("click", startRecording);

function startRecording() {
  console.log("recordButton clicked");
  recordButton.className = "recording";
  var constraints = {audio: true, video: false}

  /*
      Disable the record button until we get a success or fail from getUserMedia()
  */

  recordButton.disabled = true;

  /*
      We're using the standard promise based getUserMedia()
      https://developer.mozilla.org/en-US/docs/Web/API/MediaDevices/getUserMedia
  */


  navigator.mediaDevices.getUserMedia(constraints).then(function (stream) {
    console.log("getUserMedia() success, stream created, initializing Recorder.js ...");

    /*
      create an audio context after getUserMedia is called
      sampleRate might change after getUserMedia is called, like it does on macOS when recording through AirPods
      the sampleRate defaults to the one set in your OS for your playback device
    */
    audioContext = new AudioContext();

    //update the format
    // document.getElementById("formats").innerHTML="Format: 1 channel pcm @ "+audioContext.sampleRate/1000+"kHz"

    /*  assign to gumStream for later use  */
    gumStream = stream;

    /* use the stream */
    input = audioContext.createMediaStreamSource(stream);

    /*
      Create the Recorder object and configure to record mono sound (1 channel)
      Recording 2 channels  will double the file size
    */
    rec = new Recorder(input, {numChannels: 1})

    //start the recording process
    rec.record()

    console.log("Recording started");

    setTimeout(function () {
      stopRecording()
    }, 1500)

  }).catch(function (err) {
    //enable the record button if getUserMedia() fails
    console.log(err)
    console.log("getUserMedia() failed...")
    recordButton.disabled = false;
  });
}

function stopRecording() {
  console.log("stopButton clicked");
  recordButton.className = "not_recording"

  //disable the stop button, enable the record too allow for new recordings
  recordButton.disabled = false;

  //tell the recorder to stop the recording
  rec.stop();

  //stop microphone access
  gumStream.getAudioTracks()[0].stop();

  //create the wav blob and pass it on to createDownloadLink
  rec.exportWAV(createDownloadLink);
}

function createDownloadLink(blob) {
  var url = URL.createObjectURL(blob);
  var au = document.createElement('audio');
  au.controls = true;
  au.src = url;

  while (recordingsList.firstChild) {
    recordingsList.removeChild(recordingsList.firstChild);
  }
  recordingsList.appendChild(au);

  getSpectrogram(blob).then(reader => {
    reader.onload = async () => {
      let result = reader.result;
      console.log("Got spectrogram from backend!")
      console.log(result)

      await classifyImageData(result)
    }
  }).catch(error => {
    PersonalAudioClassifier.error(ERROR_CANNOT_CREATE_SPECTROGRAM,
        "Cannot create spectrogram: " + error);
  });
}


async function loadTransferModel(modelName, modelActivation) {
  const transferModel = await tf.loadLayersModel(TRANSFER_MODEL_PREFIX + modelName + TRANSFER_MODEL_SUFFIX);

  // Return an internal activation of the transfer model.
  const layer = transferModel.getLayer(modelActivation);
  return tf.model({inputs: transferModel.inputs, outputs: layer.output});
}

async function loadModelFile(url, json) {
  const modelFileResponse = await fetch(url);
  console.log(modelFileResponse)
  console.log("Done fetching file");

  if (json) {
    return await modelFileResponse.json();
  }
  return await modelFileResponse.blob();
}

// From https://stackoverflow.com/questions/27159179/how-to-convert-blob-to-file-in-javascript
function blobToFile(blob, fileName) {
  // A Blob() is almost a File() - it's just missing the two properties below which we will add
  blob.lastModifiedDate = new Date();
  blob.name = fileName;
  return blob;
}

async function getSpectrogram(blob) {
  console.log("PersonalAudioClassifier: " + "Audio Blob:")
  console.log(blob)

  const response = await fetch('https://c1.appinventor.mit.edu/spectrogram', {
    method: 'POST',
    body: blob
  });
  const resultBlob = await response.blob()
  let reader = new FileReader();
  reader.readAsDataURL(resultBlob);
  return reader;
}

const loadModel = async () => {
  try {
    // Loads the transfer model
    transferModelInfo = await loadModelFile(PERSONAL_MODEL_PREFIX + TRANSFER_MODEL_INFO_SUFFIX, true);

    transferModel = await loadTransferModel(transferModelInfo['name'], transferModelInfo['lastLayer']);
    console.log("transfer model fetch successful")
    console.log(transferModel)

    // Loads the user's personal model
    model = await tf.loadLayersModel(PERSONAL_MODEL_PREFIX + PERSONAL_MODEL_JSON_SUFFIX, true);
    console.log("personal model fetch successful")
    console.log(model)

    // Loads the model labels mapping
    modelLabels = await loadModelFile(PERSONAL_MODEL_PREFIX + PERSONAL_MODEL_LABELS_SUFFIX, true);
    console.log(modelLabels)

    console.log("PersonalAudioClassifier: transfer model activation and personal model are ready");
    PersonalAudioClassifier.ready(JSON.stringify(Object.values(modelLabels)))
  } catch (error) {
    console.log("PersonalAudioClassifier: " + error);
    PersonalAudioClassifier.error(ERROR_CLASSIFICATION_NOT_SUPPORTED,
        "Classification not supported: " + error);
  }
};

async function predict(pixels) {
  try {
    const logits = tf.tidy(() => {
      const img = tf.image.resizeBilinear(tf.fromPixels(pixels).toFloat(), [IMAGE_SIZE, IMAGE_SIZE]);
      const offset = tf.scalar(127.5);
      const normalized = img.sub(offset).div(offset);
      const batched = normalized.reshape([1, IMAGE_SIZE, IMAGE_SIZE, 3]);

      // Make a prediction, first using the transfer model activation and then
      // feeding that into the user provided model
      const activation = transferModel.predict(batched);
      const predictions = model.predict(activation);
      return predictions.as1D();
    });

    const topPredictions = await logits.topk(topk_predictions);

    const predictionIndices = await topPredictions.indices.data();
    const predictionValues = await topPredictions.values.data();

    var result = [];
    logits.dispose();

    for (let i = 0; i < topk_predictions; i++) {
      const currentIndex = predictionIndices[i];
      const currentValue = predictionValues[i];

      const labelName = modelLabels[currentIndex];

      result.push([labelName, currentValue.toFixed(5)]);
    }

    console.log("PersonalAudioClassifier: prediction is " + JSON.stringify(result));
    PersonalAudioClassifier.reportResult(JSON.stringify(result));
  } catch (error) {
    console.log("PersonalAudioClassifier: " + error);
    PersonalAudioClassifier.error(ERROR_CLASSIFICATION_NOT_SUPPORTED,
        "Classification not supported: " + error);
  }
}

async function classifyImageData(imageURL) {
  console.log("PersonalAudioClassifier: " + "(Javascript) in classifyImageData()");
  var convertedImg = await convertImg(imageURL)
  console.log(convertedImg)

  var activation = transferModel.predict(
    tf.stack([convertedImg])
  )
  console.log(activation)

  var prediction = model.predict(activation)
  var top = await prediction.topk(Object.keys(modelLabels).length)
  let confidences = await top.values.data()
  let ranks = await top.indices.data()
  console.log(confidences)
  console.log(ranks)

  var result = []
  for (var place = 0; place < ranks.length; place++) {
    var who = ranks[place]
    var label = modelLabels[who]
    var conf = confidences[place]
    result.push([label, conf])
  }

  console.log(result)
  PersonalAudioClassifier.reportResult(JSON.stringify(result));
}

async function convertImg(imgUrl) {
  const load = () => new Promise((resolve, reject) => {
    var img = new Image()
    img.onload = () => {
      resolve({img})
    }
    img.onerror = reject;
    img.src = imgUrl
    img.width = 200;
    img.height = 200;
  });

  const {img} = await load()

  const trainImage = tf.browser.fromPixels(img).resizeNearestNeighbor([224, 224]);
  return trainImage.toFloat().div(tf.scalar(127)).sub(tf.scalar(1));
}

loadModel();
