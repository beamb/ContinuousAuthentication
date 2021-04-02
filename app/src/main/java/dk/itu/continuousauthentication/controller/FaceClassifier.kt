package dk.itu.continuousauthentication.controller

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import dk.itu.continuousauthentication.utils.ByteBufferUtils
import dk.itu.continuousauthentication.model.Person
import dk.itu.continuousauthentication.model.PersonsDB
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap
import kotlin.math.sqrt

import org.tensorflow.lite.Interpreter


class FaceClassifier(context: Context) {

    private var interpreter: Interpreter? = null

    // Model: Database of persons
    private lateinit var personsDB: PersonsDB

    private lateinit var globalPerson: Person

    init {
        val inputStream: InputStream = context.assets.open("MobileFaceNet.tflite")
        val model = inputStream.readBytes()
        val buffer = ByteBuffer.allocateDirect(model.size).order(ByteOrder.nativeOrder())
        buffer.put(model)
        interpreter = Interpreter(buffer)
        personsDB = PersonsDB[context]
    }


    fun getEmbedding(face: Bitmap): FloatArray {
        val bitmap = Bitmap.createScaledBitmap(face, 112, 112, true)

        val input = ByteBufferUtils.getImageData(bitmap)

        val modelOutput = ByteBufferUtils.getModelOutput()

        Log.i("Enrollment", "Interpreter: $interpreter")
        interpreter?.run(input, modelOutput)

        modelOutput.rewind()

        val buffer = modelOutput.asFloatBuffer()
        val result = FloatArray(buffer.capacity()) { 0f }
        buffer.get(result)

        return result
    }

    fun classify(face: Bitmap) {
        val embeddings = getEmbedding(face)
        Log.i("Enrollment", "persons in FaceClassifier: ${personsDB.getPersonsDB()}")
        try {
            recognize(embeddings)

        } catch (e: IOException) {
            Log.e(
                "FaceClassifier",
                "Classification IOException : $e"
            )
            "error"
        }
    }

    private fun recognize(embedding: FloatArray) {
        if (personsDB.isNotEmpty()) {
            val similarities = LinkedHashMap<Float, String>()
            for (person in personsDB.getPersonsDB()) {
                for (personEmbedding in person.embeddings) {
                    similarities[cosineSimilarity(personEmbedding, embedding)] = person.name
                }
            }
            val maxVal = Collections.max(similarities.keys)
            if (!this::globalPerson.isInitialized || globalPerson.name != similarities[maxVal]) {
                Log.i("FaceRecognition", "MaxVal: $maxVal")
                globalPerson = if (maxVal > 0.85) {
                    similarities[maxVal]?.let { personsDB.getPerson(it) }!!
                } else Person("unknown")
            }
        }
    }

    private fun cosineSimilarity(A: FloatArray?, B: FloatArray?): Float {
        if (A == null || B == null || A.isEmpty() || B.isEmpty() || A.size != B.size) {
            return 2.0F
        }

        var sumProduct = 0.0
        var sumASq = 0.0
        var sumBSq = 0.0
        for (i in A.indices) {
            sumProduct += (A[i] * B[i]).toDouble()
            sumASq += (A[i] * A[i]).toDouble()
            sumBSq += (B[i] * B[i]).toDouble()
        }
        return if (sumASq == 0.0 && sumBSq == 0.0) {
            2.0F
        } else (sumProduct / (sqrt(sumASq) * sqrt(sumBSq))).toFloat()
    }

    fun getGlobalPersonName(): Person {
        return if (this::globalPerson.isInitialized) globalPerson else Person("unknown")
    }

    // TODO: Figure out best cut-off point for maxVal authentication
}