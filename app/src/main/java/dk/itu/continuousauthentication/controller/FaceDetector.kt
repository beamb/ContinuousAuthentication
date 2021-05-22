package dk.itu.continuousauthentication.controller

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import dk.itu.continuousauthentication.controller.FaceMovement.addMovement
import dk.itu.continuousauthentication.controller.FaceMovement.setMode
import dk.itu.continuousauthentication.model.Person
import dk.itu.continuousauthentication.model.PersonsDB
import dk.itu.continuousauthentication.utils.BitmapUtils
import dk.itu.continuousauthentication.utils.Frame
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.HashMap
import kotlin.math.abs


class FaceDetector : Observable() {

    private val mlkitFaceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(MIN_FACE_SIZE)
            .enableTracking()
            .build()
    )

    /** Listener that gets notified when a face detection result is ready. */
    private var onFaceDetectionResultListener: OnFaceDetectionResultListener? = null

    /** [Executor] used to run the face detection on a background thread.  */
    private var faceDetectionExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private lateinit var faceBitmap: Bitmap
    private var screenHeight: Int = 0
    private var screenWidth: Int = 0

    private val facesHashMap = HashMap<Int, String>()
    private var counter = 0
    private var startAuthentication = false
    private var unknownFaceStatus = false
    private var finishedEnrollmentStatus = false

    private var isEnrolling = false
    private var isAuthenticating = false

    private lateinit var identifiedPerson: Person
    private lateinit var classifier: MovementClassifier

    // Model: Database of persons
    private lateinit var personsDB: PersonsDB

    fun setIsEnrolling(boolean: Boolean) {
        isEnrolling = boolean
    }

    fun setIsAuthenticating(boolean: Boolean) {
        isAuthenticating = boolean
    }

    fun setStartAuthentication(boolean: Boolean) {
        startAuthentication = boolean
    }

    fun setUnknownFaceStatus(boolean: Boolean) {
        unknownFaceStatus = boolean
        setChanged()
        notifyObservers()
    }

    fun getUnknownFaceStatus(): Boolean {
        return unknownFaceStatus
    }

    fun getFinishedEnrollmentStatus(): Boolean {
        return finishedEnrollmentStatus
    }

    fun getIdentifiedPerson(): Person {
        return identifiedPerson
    }

    fun close() {
        mlkitFaceDetector.close()
    }

    /**
     * Kick-starts a face detection operation on a camera frame. If a previous face detection
     * operation is still ongoing, the frame is dropped until the face detector is no longer busy.
     */
    fun process(
        frame: Frame,
        context: Context, name: String
    ) {

        screenWidth = context.resources.displayMetrics.widthPixels
        screenHeight = context.resources.displayMetrics.heightPixels
        personsDB = PersonsDB[context]
        faceDetectionExecutor.execute {
            frame.detectFaces(
                context, name
            )
        }
    }


    private fun Frame.detectFaces(
        context: Context, name: String
    ) {
        val data = data ?: return
        val inputImage = InputImage.fromByteArray(data, size.width, size.height, rotation, format)
        val faceClassifier = FaceClassifier[context]
        when {
            isEnrolling -> {
                mlkitFaceDetector.process(inputImage)
                    .addOnSuccessListener { faces ->
                        if (faces.size == 1) {
                            counter++
                            faces.forEach {
                                if (!::classifier.isInitialized) classifier =
                                    MovementClassifier[context, personsDB.getPerson(name)]
                                if (personsDB.getPerson(name).movements.length <= 5) {
                                    finishedEnrollmentStatus = false
                                    setMode("add")
                                    if (counter == 1
                                    ) {
                                        Log.i(
                                            "Recognize",
                                            "Capturing you, $name when counter is at $counter and you have ${personsDB.getPerson(
                                                name
                                            ).movements.length} movements!"
                                        )
                                        makePersonFromBitmap(it, this, data, faceClassifier, name)
                                    }
                                    addMovement(
                                        it,
                                        context,
                                        name,
                                        classifier
                                    )
                                } else {
                                    finishedEnrollmentStatus = true
                                    counter = 0
                                }
                            }
                        } else {
                            Log.i("Recognize", "Faces size = ${faces.size}")
                            setUnknownFaceStatus(true)
                        }
                    }
                    .addOnFailureListener { exception ->
                        onError(exception)
                    }
            }
            isAuthenticating -> {
                mlkitFaceDetector.process(inputImage)
                    .addOnSuccessListener { faces ->
                        if (faces.size == 1) {
                            counter++
                            faces.forEach {
                                if (facesHashMap.containsKey(it.trackingId!!) && counter != 1
                                ) {
                                    Log.i("FaceRecognition", "Counter: $counter")
                                    if (counter > 20) {
                                        counter = 0
                                    }
                                    if (::classifier.isInitialized) {
                                        if (classifier.person.name != identifiedPerson.name) classifier =
                                            MovementClassifier[context, personsDB.getPerson(
                                                identifiedPerson.name
                                            )]
                                        setMode("auth")
                                        addMovement(
                                            it,
                                            context,
                                            identifiedPerson.name,
                                            classifier
                                        )
                                    } else {
                                        classifier =
                                            MovementClassifier[context, personsDB.getPerson(
                                                identifiedPerson.name
                                            )]
                                        setMode("auth")
                                        addMovement(
                                            it,
                                            context,
                                            identifiedPerson.name,
                                            classifier
                                        )
                                    }
                                } else {
                                    identifyFromBitmap(it, this, data, faceClassifier)
                                    if (::classifier.isInitialized) {
                                        if (classifier.person.name != identifiedPerson.name) classifier =
                                            MovementClassifier[context, personsDB.getPerson(
                                                identifiedPerson.name
                                            )]
                                        setMode("auth")
                                        addMovement(
                                            it,
                                            context,
                                            identifiedPerson.name,
                                            classifier
                                        )
                                    } else {
                                        classifier =
                                            MovementClassifier[context, personsDB.getPerson(
                                                identifiedPerson.name
                                            )]
                                        setMode("auth")
                                        addMovement(
                                            it,
                                            context,
                                            identifiedPerson.name,
                                            classifier
                                        )
                                    }
                                }
                            }
                        } else {
                            Log.i("Recognize", "Faces size = ${faces.size}")
                            setUnknownFaceStatus(true)
                        }
                    }
                    .addOnFailureListener { exception ->
                        onError(exception)
                    }
            }
            else -> {
                mlkitFaceDetector.process(inputImage)
                    .addOnSuccessListener { faces ->
                        if (faces.size == 1) {
                            counter++
                            faces.forEach {
                                if (facesHashMap.containsKey(it.trackingId!!) && counter != 1
                                ) {
                                    Log.i(
                                        "FaceRecognition",
                                        "Recognition: ${it.trackingId} --> ${facesHashMap[it.trackingId!!]}"
                                    )
                                    if (counter > 10) {
                                        counter = 0
                                    }
                                } else {
                                    identifyFromBitmap(it, this, data, faceClassifier)
                                }
                            }
                        } else {
                            Log.i("Recognize", "Faces size = ${faces.size}")
                            setUnknownFaceStatus(true)
                        }
                    }

                    .addOnFailureListener { exception ->
                        onError(exception)
                    }

            }
        }
    }

    private fun makePersonFromBitmap(
        face: Face,
        frame: Frame,
        data: ByteArray,
        faceClassifier: FaceClassifier,
        name: String
    ) {
        faceBitmap = getFaceBitmap(
            getScaledBoundingBox(face, frame), Bitmap.createBitmap(
                BitmapUtils.getBitmap(
                    ByteBuffer.wrap(data),
                    frame
                )!!
            )
        )
        val embedding = faceClassifier.getEmbedding(faceBitmap)
        val person = personsDB.getPerson(name)
        person.addEmbeddings(embedding)
        personsDB.add(person)
    }

    private fun identifyFromBitmap(
        face: Face,
        frame: Frame,
        data: ByteArray,
        faceClassifier: FaceClassifier
    ) {
        faceBitmap = getFaceBitmap(
            getScaledBoundingBox(face, frame), Bitmap.createBitmap(
                BitmapUtils.getBitmap(
                    ByteBuffer.wrap(data),
                    frame
                )!!
            )
        )
        identifiedPerson = faceClassifier.classify(faceBitmap)
        Log.i("FaceRecognition", "HashMap: $facesHashMap")
        if (identifiedPerson.name != "unknown") {
            facesHashMap[face.trackingId!!] = identifiedPerson.name
        } else {
            Log.i("Recognize", "Identified person = unknown")
            setUnknownFaceStatus(true)
        }
    }

    fun resetMovementClassifier() {
        classifier.resetInput()
    }

    fun checkInput(): Boolean {
        return classifier.checkInput()
    }

    fun displayInput(): String {
        return classifier.getInput()
    }

    private fun getFaceBitmap(boundingBox: RectF, frame: Bitmap): Bitmap {
        val frameWidth = frame.width
        val frameHeight = frame.height
        Log.i("crop", "NEW ROUND")
        Log.i("crop", "screenWidth, screenHeight: $screenWidth, $screenHeight")
        Log.i("crop", "frameWidth, frameHeight: $frameWidth, $frameHeight")
        val offsetX = abs((frame.width - screenWidth) / 2)
        val offsetY = abs((frame.height - screenHeight) * 2)
        Log.i("crop", "offsetX, offsetY: $offsetX, $offsetY")
        val boxCenterX: Float
        val boxCenterY: Float
        if (offsetX != 0 && offsetY != 0) {
            val centerX = (boundingBox.left + (boundingBox.right - boundingBox.left) / 2) + offsetX
            val centerY = (boundingBox.top + (boundingBox.bottom - boundingBox.top) / 2) + offsetY

            boxCenterX = centerX - boundingBox.width() / 2
            boxCenterY = centerY - boundingBox.width() / 2
            Log.i("crop", "took the if")
        } else {
            boxCenterX =
                (boundingBox.left + (boundingBox.right - boundingBox.left) / 2) - boundingBox.width() / 2
            boxCenterY =
                (boundingBox.top + (boundingBox.bottom - boundingBox.top) / 2) - boundingBox.height() / 2
            Log.i("crop", "took the else")
            Log.i("crop", "boxCenterX, boxCenterY: $boxCenterX, $boxCenterY")
        }

        val bbHeight = boundingBox.height()
        Log.i("crop", "$bbHeight")
        val faceX: Int =
            if ((boxCenterX + boundingBox.width()) > frame.width || ((boxCenterX + boundingBox.width()) + (boxCenterX - boundingBox.width()) < 0)) 0 else boxCenterX.toInt()
        val faceY: Int =
            if ((boxCenterY + boundingBox.height()) > frame.height || ((boxCenterY + boundingBox.height()) + (boxCenterY - boundingBox.height()) < 0)) 0 else boxCenterY.toInt()
        Log.i("crop", "faceX,faceY: $faceX, $faceY")

        val faceWidth: Int =
            if ((faceX + boundingBox.width()) > frame.width) frame.width else boundingBox.width()
                .toInt()
        val faceHeight: Int =
            if ((faceY + boundingBox.height()) > frame.height) frame.width else boundingBox.width()
                .toInt()
        Log.i("crop", "faceWidth, faceHeight: $faceWidth, $faceHeight")

        return Bitmap.createBitmap(frame, faceX, faceY, faceWidth, faceHeight)
    }

    private fun getScaledBoundingBox(face: Face, frame: Frame): RectF {
        val width = frame.size.height
        val height = frame.size.width
        val flippedLeft = width - face.boundingBox.right
        val flippedRight = width - face.boundingBox.left
        val scaledLeft = width.toFloat() * flippedLeft
        val scaledTop = height.toFloat() * face.boundingBox.top
        val scaledRight = width.toFloat() * flippedRight
        val scaledBottom = height.toFloat() * face.boundingBox.bottom
        return RectF(scaledLeft, scaledTop, scaledRight, scaledBottom)
    }

    private fun onError(exception: Exception) {
        onFaceDetectionResultListener?.onFailure(exception)
        Log.e(TAG, "An error occurred while running a face detection", exception)
    }

    /**
     * Interface containing callbacks that are invoked when the face detection process succeeds or
     * fails.
     */
    interface OnFaceDetectionResultListener {

        /**
         * Invoked when an error is encountered while attempting to detect faces in a camera frame.
         *
         * @param exception Encountered [Exception] while attempting to detect faces in a camera
         * frame.
         */
        fun onFailure(exception: Exception) {}
    }

    companion object {
        private const val TAG = "FaceDetector"
        private const val MIN_FACE_SIZE = 0.15F
    }
}