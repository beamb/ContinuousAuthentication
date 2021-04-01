package dk.itu.continuousauthentication.controller

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import com.google.mlkit.vision.face.Face
import dk.itu.continuousauthentication.model.Person
import dk.itu.continuousauthentication.model.PersonsDB

object FaceMovement {

    private const val TAG = "MovementDetector"

    private const val Down = "0"
    private const val Left = "1"
    private const val Right = "2"
    private const val Up = "3"
    private const val Smile = "4"
    private const val LeftWink = "5"
    private const val RightWink = "6"
    private const val Closed = "7"
    private const val LeftTilt = "8"
    private const val RightTilt = "9"

    private var isFaceDown: Boolean = false
    private var isFaceLeft: Boolean = false
    private var isFaceRight: Boolean = false
    private var isFaceUp: Boolean = false
    private var isSmiling: Boolean = false
    private var isTiltLeft: Boolean = false
    private var isTiltRight: Boolean = false
    private var bothEyesCounter: Int = 0
    private var rightEyeCounter: Int = 0
    private var leftEyeCounter: Int = 0

    private var addMode: Boolean = true
    private var authMode: Boolean = false

    fun addMovement(face: Face, context: Context, name: String, classifier: MovementClassifier) {
        val faceDetector = FaceDetector[context]
        val personsDB = PersonsDB[context]

        val rotX = face.headEulerAngleX // Head is facing upwards rotX degrees
        val rotY =
            face.headEulerAngleY // Head is rotated to the right rotY degrees
        val rotZ = face.headEulerAngleZ // Head is tilted sideways rotZ degrees
        val smile = face.smilingProbability
        val rightEye = face.rightEyeOpenProbability
        val leftEye = face.leftEyeOpenProbability

        val person = personsDB.getPerson(name)

        if (smile!! > 0.9) {
            if (!isSmiling) {
                isSmiling = true
                faceDetector.setMovementTrigger(true)
                if (addMode) {
                    person.addMovement(Smile)
                    personsDB.add(person)
                } else if (authMode) classifier.addInput(Smile)
                val msg = "What a great smile ${person.name}!"
                Log.i(TAG, msg)
                displayToast(context, msg)
            }
        } else {
            isSmiling = false
            faceDetector.setMovementTrigger(false)
        }
        when {
            rightEye!! < 0.1 && leftEye!! < 0.1 -> {
                bothEyesCounter++
                rightEyeCounter = 0
                leftEyeCounter = 0
                if (bothEyesCounter == 6) {
                    faceDetector.setMovementTrigger(true)
                    if (addMode) {
                        person.addMovement(Closed)
                        personsDB.add(person)
                    } else if (authMode) classifier.addInput(Closed)
                    val msg = "Your eyes are closed ${person.name}!"
                    Log.i(TAG, msg)
                    displayToast(context, msg)
                }
            }
            rightEye > 0.1 && leftEye!! > 0.1 -> {
                bothEyesCounter = 0
                rightEyeCounter = 0
                leftEyeCounter = 0
                faceDetector.setMovementTrigger(false)
            }
            rightEye > 0.5 && leftEye!! < 0.1 -> {
                rightEyeCounter++
                bothEyesCounter = 0
                leftEyeCounter = 0
                if (rightEyeCounter == 3) {
                    faceDetector.setMovementTrigger(true)
                    if (addMode) {
                        person.addMovement(RightWink)
                        personsDB.add(person)
                    } else if (authMode) classifier.addInput(RightWink)
                    val msg = "Right Wink ${person.name}!"
                    Log.i(TAG, msg)
                    displayToast(context, msg)
                }
            }
            rightEye < 0.1 && leftEye!! > 0.5 -> {
                leftEyeCounter++
                bothEyesCounter = 0
                rightEyeCounter = 0
                if (leftEyeCounter == 3) {
                    faceDetector.setMovementTrigger(true)
                    if (addMode) {
                        person.addMovement(LeftWink)
                        personsDB.add(person)
                    } else if (authMode) classifier.addInput(LeftWink)
                    val msg = "Left Wink ${person.name}!"
                    Log.i(TAG, msg)
                    displayToast(context, msg)

                }
            }
        }

        when {
            rotY > 20.toFloat() -> {
                if (!isFaceLeft) {
                    faceDetector.setMovementTrigger(true)
                    if (addMode) {
                        person.addMovement(Left)
                        personsDB.add(person)
                    } else if (authMode) classifier.addInput(Left)
                    isFaceLeft = true
                    val msg = "Facing Left ${person.name}!"
                    Log.i(TAG, msg)
                    displayToast(context, msg)
                }
            }
            rotY < (-20).toFloat() -> {
                if (!isFaceRight) {
                    faceDetector.setMovementTrigger(true)
                    if (addMode) {
                        person.addMovement(Right)
                        personsDB.add(person)
                    } else if (authMode) classifier.addInput(Right)
                    isFaceRight = true
                    val msg = "Facing Right ${person.name}!"
                    Log.i(TAG, msg)
                    displayToast(context, msg)
                }
            }
            rotX > 20.toFloat() -> {
                if (!isFaceUp) {
                    faceDetector.setMovementTrigger(true)
                    if (addMode) {
                        person.addMovement(Up)
                        personsDB.add(person)
                    } else if (authMode) classifier.addInput(Up)
                    isFaceUp = true
                    val msg = "Facing Up ${person.name}!"
                    Log.i(TAG, msg)
                    displayToast(context, msg)
                }
            }
            rotX < (-5).toFloat() -> {
                if (!isFaceDown) {
                    faceDetector.setMovementTrigger(true)
                    if (addMode) {
                        person.addMovement(Down)
                        personsDB.add(person)
                    } else if (authMode) classifier.addInput(Down)
                    isFaceDown = true
                    val msg = "Facing Down ${person.name}!"
                    Log.i(TAG, msg)
                    displayToast(context, msg)
                }
            }
            rotZ > 15.toFloat() -> {
                if (!isTiltRight) {
                    faceDetector.setMovementTrigger(true)
                    if (addMode) {
                        person.addMovement(RightTilt)
                        personsDB.add(person)
                    } else if (authMode) classifier.addInput(RightTilt)
                    isTiltRight = true
                    val msg = "Right Tilt ${person.name}!"
                    Log.i(TAG, msg)
                    displayToast(context, msg)
                }
            }
            rotZ < (-15).toFloat() -> {
                if (!isTiltLeft) {
                    faceDetector.setMovementTrigger(true)
                    if (addMode) {
                        person.addMovement(LeftTilt)
                        personsDB.add(person)
                    } else if (authMode) classifier.addInput(LeftTilt)
                    isTiltLeft = true
                    val msg = "Left Tilt ${person.name}!"
                    Log.i(TAG, msg)
                    displayToast(context, msg)
                }
            }
            rotX < 20.toFloat() && rotX > (-5).toFloat() && rotY < 20.toFloat() && rotY > (-20).toFloat() && rotZ < 15.toFloat() && rotZ > (-15).toFloat() -> {
                isFaceDown = false
                isFaceUp = false
                isFaceLeft = false
                isFaceRight = false
                isTiltLeft = false
                isTiltRight = false
                faceDetector.setMovementTrigger(false)
            }
        }
    }

    private fun displayToast(context: Context, msg: String) {
        val toast = Toast.makeText(
            context,
            msg,
            Toast.LENGTH_SHORT
        )
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }

    fun setMode(mode: String) {
        when (mode) {
            "add" -> {
                addMode = true
                authMode = false
            }
            "auth" -> {
                addMode = false
                authMode = true
            }
            else -> {
                addMode = false
                authMode = false
            }
        }
    }
}