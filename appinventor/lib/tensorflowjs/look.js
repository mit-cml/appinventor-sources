'use strict';

console.log('Look: Using Tensorflow.js version ' + tf.version.tfjs);

//const MOBILENET_MODEL_PATH = 'https://storage.googleapis.com/tfjs-models/tfjs/mobilenet_v1_0.25_224/model.json';
const MOBILENET_MODEL_PATH = 'model.json';

const IMAGE_SIZE = 224;
const TOPK_PREDICTIONS = 10;

let mobilenet;
const mobilenetDemo = async () => {
  mobilenet = await tf.loadModel(MOBILENET_MODEL_PATH);
  const zeros = tf.zeros([1, IMAGE_SIZE, IMAGE_SIZE, 3]);
  mobilenet.predict(zeros).dispose();
  zeros.dispose();
  console.log('Look: Mobilenet ready');
  Look.ready();
};

async function predict(imgElement) {
  const logits = tf.tidy(() => {
    const img = tf.image.resizeBilinear(tf.fromPixels(imgElement).toFloat(), [IMAGE_SIZE, IMAGE_SIZE]);
    const offset = tf.scalar(127.5);
    const normalized = img.sub(offset).div(offset);
    const batched = normalized.reshape([1, IMAGE_SIZE, IMAGE_SIZE, 3]);
    return mobilenet.predict(batched);
  });
  const classes = await getTopKClasses(logits, TOPK_PREDICTIONS);
  logits.dispose();
  var result = [];
  for (let i = 0; i < classes.length; i++) {
    result.push([classes[i].className, classes[i].probability.toFixed(5)]);
  }
  console.log('Look: prediction is ' + JSON.stringify(result));
  Look.reportResult(JSON.stringify(result));
}

async function getTopKClasses(logits, topK) {
  const values = await logits.data();
  const valuesAndIndices = [];
  for (let i = 0; i < values.length; i++) {
    valuesAndIndices.push({value: values[i], index: i});
  }
  valuesAndIndices.sort((a, b) => {
    return b.value - a.value;
  });
  const topkValues = new Float32Array(topK);
  const topkIndices = new Int32Array(topK);
  for (let i = 0; i < topK; i++) {
    topkValues[i] = valuesAndIndices[i].value;
    topkIndices[i] = valuesAndIndices[i].index;
  }
  const topClassesAndProbs = [];
  for (let i = 0; i < topkIndices.length; i++) {
    topClassesAndProbs.push({
      className: IMAGENET_CLASSES[topkIndices[i]],
      probability: topkValues[i]
    });
  }
  return topClassesAndProbs;
}

var video = document.createElement('video');
video.setAttribute('autoplay', '');
video.setAttribute('playsinline', '');
video.width = 500;
video.style.display = 'none';

var frontFacing = true;
var isPlaying = false;
var isVideoMode = false;

var img = new Image();
img.width = 500;

var isImageShowing = true;
img.style.display = 'block';

document.body.appendChild(video);
document.body.appendChild(img);

video.addEventListener('loadedmetadata', function () {
    video.height = this.videoHeight * video.width / this.videoWidth;
}, false);

function startVideo() {
  if (!isPlaying && isVideoMode) {
    navigator.mediaDevices.getUserMedia({video: {facingMode: frontFacing ? 'user' : 'environment'}, audio: false})
    .then(stream => (video.srcObject = stream))
    .catch(e => log(e));
    isPlaying = true;
    video.style.display = 'block';
  }
}

function stopVideo() {
  if (isPlaying && isVideoMode && video.srcObject) {
    video.srcObject.getTracks().forEach(t => t.stop());
    isPlaying = false;
    video.style.display = 'none';
  }
}

function toggleCameraFacingMode() {
  frontFacing = !frontFacing;
  stopVideo();
  startVideo();
}

function classifyVideoData() {
  if (isPlaying && isVideoMode) {
    predict(video);
  }
}

function classifyImageData(imageData) {
  if (!isVideoMode) {
    img.onload = function() {
      predict(img);
    }
    img.src = 'data:image/png;base64,' + imageData;
  }
}

function showImage() {
  if (!isImageShowing && !isVideoMode) {
    img.style.display = 'block';
    isImageShowing = true;
  }
}

function hideImage() {
  if (isImageShowing) {
    img.style.display = 'none';
    isImageShowing = false;
  }
}

function setInputMode(inputMode) {
  if (inputMode == 'image' && isVideoMode) {
    stopVideo();
    isVideoMode = false;
    showImage();
  } else if (inputMode == 'video' && !isVideoMode) {
    hideImage();
    isVideoMode = true;
    startVideo();
  }
}

function setInputWidth(width) {
  video.width = width;
  video.height = video.videoHeight * width / video.videoWidth;
  img.width = width;
}

mobilenetDemo();
