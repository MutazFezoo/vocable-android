package com.willowtree.vocable.facetracking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.ar.core.AugmentedFace
import com.google.ar.sceneform.math.Vector3
import com.willowtree.vocable.utils.VocableSharedPreferences
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject

class FaceTrackingViewModel : ViewModel(), KoinComponent {

    private val viewModelJob = SupervisorJob()
    private val backgroundScope = CoroutineScope(viewModelJob + Dispatchers.IO)

    private val sharedPrefs: VocableSharedPreferences by inject()

    private var faceTrackingJob: Job? = null

    private val liveAdjustedVector = MutableLiveData<Vector3>()
    val adjustedVector: LiveData<Vector3> = liveAdjustedVector

    private val livePointerLocation = MutableLiveData<Vector3>()
    val pointerLocation: LiveData<Vector3> = livePointerLocation

    private val liveShowError = MutableLiveData<Boolean>()
    val showError: LiveData<Boolean> = liveShowError


    private var oldVector: Vector3? = null

    fun onFaceDetected(augmentedFaces: Collection<AugmentedFace>?) {
        if (!sharedPrefs.getHeadTrackingEnabled()) {
            liveShowError.postValue(false)
            return
        }
        if (augmentedFaces?.firstOrNull() == null) {
            liveShowError.postValue(true)
            return
        }
        if (liveShowError.value == true) {
            liveShowError.postValue(false)
        }

        if (faceTrackingJob != null && faceTrackingJob?.isActive == true) {
            return
        }
        augmentedFaces.firstOrNull()?.let { augmentedFace ->
            faceTrackingJob = backgroundScope.launch {
                val pose = augmentedFace.getRegionPose(AugmentedFace.RegionType.NOSE_TIP)
                val zAxis = pose.zAxis
                val x = zAxis[0]
                val y = zAxis[1]
                val z = -zAxis[2]

                when (oldVector) {
                    null -> {
                        oldVector = Vector3(x, y, z)
                        liveAdjustedVector.postValue(oldVector)
                        Vector3(x, y, z)
                    }
                    else -> {
                        val adjustedVector = Vector3.lerp(oldVector, Vector3(x, y, z), 0.15F)
                        liveAdjustedVector.postValue(adjustedVector)
                        oldVector = adjustedVector
                    }
                }
            }
        }
    }

    fun onScreenPointAvailable(screenPoint: Vector3) {
        livePointerLocation.postValue(screenPoint)
    }

    override fun onCleared() {
        viewModelJob.cancel()
    }
}