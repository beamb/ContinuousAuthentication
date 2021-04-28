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
    private var moreMode: Boolean = false

    fun addMovement(
        face: Face,
        context: Context,
        name: String,
        classifier: MovementClassifier
    ): Boolean {
        val personsDB = PersonsDB[context]

        val rotX = face.headEulerAngleX // Head is facing upwards rotX degrees
        val rotY =
            face.headEulerAngleY // Head is rotated to the right rotY degrees
        val rotZ = face.headEulerAngleZ // Head is tilted sideways rotZ degrees
        val smile = face.smilingProbability
        val rightEye = face.rightEyeOpenProbability
        val leftEye = face.leftEyeOpenProbability

        val person = personsDB.getPerson(name)

        when {
            rotY > 20.toFloat() -> {
                if (!isFaceLeft) {
                    isFaceLeft = true
                    val msg = "Facing Left ${person.name}!"
                    manageMovement(person, personsDB, classifier, msg, context, Left)
                    return true
                }
                return false
            }
            rotY < (-20).toFloat() -> {
                if (!isFaceRight) {
                    isFaceRight = true
                    val msg = "Facing Right ${person.name}!"
                    manageMovement(person, personsDB, classifier, msg, context, Right)
                    return true
                }
                return false
            }
            rotX > 20.toFloat() -> {
                if (!isFaceUp) {
                    isFaceUp = true
                    val msg = "Facing Up ${person.name}!"
                    manageMovement(person, personsDB, classifier, msg, context, Up)
                    return true
                }
                return false
            }
            rotX < (-5).toFloat() -> {
                if (!isFaceDown) {
                    isFaceDown = true
                    val msg = "Facing Down ${person.name}!"
                    manageMovement(person, personsDB, classifier, msg, context, Down)
                    return true
                }
                return false
            }
            rotZ > 15.toFloat() -> {
                if (!isTiltRight) {
                    isTiltRight = true
                    val msg = "Right Tilt ${person.name}!"
                    manageMovement(
                        person,
                        personsDB,
                        classifier,
                        msg,
                        context,
                        RightTilt
                    )
                    return true
                }
                return false
            }
            rotZ < (-15).toFloat() -> {
                if (!isTiltLeft) {
                    isTiltLeft = true
                    val msg = "Left Tilt ${person.name}!"
                    manageMovement(
                        person,
                        personsDB,
                        classifier,
                        msg,
                        context,
                        LeftTilt
                    )
                    return true
                }
                return false
            }
            rightEye!! < 0.1 && leftEye!! < 0.1 -> {
                bothEyesCounter++
                rightEyeCounter = 0
                leftEyeCounter = 0
                if (bothEyesCounter == 6) {
                    val msg = "Your eyes are closed ${person.name}!"
                    manageMovement(
                        person,
                        personsDB,
                        classifier,
                        msg,
                        context,
                        Closed
                    )
                    return true
                }
                return false
            }
            rightEye > 0.5 && leftEye!! < 0.1 -> {
                rightEyeCounter++
                bothEyesCounter = 0
                leftEyeCounter = 0
                if (rightEyeCounter == 3) {
                    val msg = "Right Wink ${person.name}!"
                    manageMovement(
                        person,
                        personsDB,
                        classifier,
                        msg,
                        context,
                        RightWink
                    )
                    return true
                }
                return false
            }
            rightEye < 0.1 && leftEye!! > 0.5 -> {
                leftEyeCounter++
                bothEyesCounter = 0
                rightEyeCounter = 0
                if (leftEyeCounter == 3) {
                    val msg = "Left Wink ${person.name}!"
                    manageMovement(
                        person,
                        personsDB,
                        classifier,
                        msg,
                        context,
                        LeftWink
                    )
                    return true
                }
                return false
            }
            smile!! > 0.9 -> {
                if (!isSmiling) {
                    isSmiling = true
                    val msg = "What a great smile ${person.name}!"
                    manageMovement(person, personsDB, classifier, msg, context, Smile)
                    return true
                }
                return false
            }
        }
        isFaceDown = false
        isFaceUp = false
        isFaceLeft = false
        isFaceRight = false
        isTiltLeft = false
        isTiltRight = false
        isSmiling = false
        bothEyesCounter = 0
        rightEyeCounter = 0
        leftEyeCounter = 0
        return false
    }

    private fun manageMovement(
        person: Person,
        personsDB: PersonsDB,
        classifier: MovementClassifier,
        msg: String,
        context: Context,
        movement: String
    ) {
        if (addMode) {
            person.addMovement(movement)
            personsDB.add(person)
            displayToast(context, msg)
        } else if (authMode) {
            classifier.addInput(movement)
            displayToast(context, msg)
        } else if (moreMode && person.movements.contains(movement)) {
            displayToast(context, msg)
        }
        Log.i(TAG, msg)
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
                moreMode = false
            }
            "auth" -> {
                addMode = false
                authMode = true
                moreMode = false
            }
            "more" -> {
                addMode = false
                authMode = false
                moreMode = true
            }
            else -> {
                addMode = false
                authMode = false
                moreMode = false
            }
        }
    }
}