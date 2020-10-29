package com.esri.arcgisruntime.opensourceapps.datacollection.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.esri.arcgisruntime.ArcGISRuntimeException
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.FeatureEditResult
import com.esri.arcgisruntime.data.FeatureTable
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.mapping.popup.Popup
import com.esri.arcgisruntime.mapping.popup.PopupManager
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult
import com.esri.arcgisruntime.opensourceapps.datacollection.util.Event
import com.esri.arcgisruntime.opensourceapps.datacollection.util.raiseEvent

/**
 * The view model for PopupView, that is responsible for maintaining the PopupView
 * editMode state for orientation changes and for performing async operations.
 */
class PopupViewModel(application: Application) : AndroidViewModel(application) {

    private val _identifiedPopup = MutableLiveData<Popup>()
    val identifiedPopup: LiveData<Popup> = _identifiedPopup

    private val _popupManager = MutableLiveData<PopupManager>()
    val popupManager: LiveData<PopupManager> = _popupManager

    private val _isPopupInEditMode = MutableLiveData<Boolean>()
    val isPopupInEditMode: LiveData<Boolean> = _isPopupInEditMode

    private val _showSavePopupErrorEvent = MutableLiveData<Event<String>>()
    val showSavePopupErrorEvent: LiveData<Event<String>> = _showSavePopupErrorEvent

    private val _toggleSavingPopupProgressBarEvent = MutableLiveData<Event<Boolean>>()
    val toggleSavingPopupProgressBarEvent: LiveData<Event<Boolean>> = _toggleSavingPopupProgressBarEvent

    private val _confirmCancelPopupEditingEvent = MutableLiveData<Event<Unit>>()
    val confirmCancelPopupEditingEvent: LiveData<Event<Unit>> = _confirmCancelPopupEditingEvent

    /**
     * Updates identifiedPopup property to set the popup field values being displayed in
     * the bottom sheet. Creates PopupManager for PopupView to perform edit operations.
     *
     * @param identifyLayerResult
     */
    fun processIdentifyLayerResult(
        identifyLayerResult: IdentifyLayerResult
    ) {
        if (identifyLayerResult.popups.size > 0) {
            _identifiedPopup.value = identifyLayerResult.popups[0]
            _popupManager.value = PopupManager(getApplication(), _identifiedPopup.value)
        }
    }

    /**
     * Enables/disables edit mode on the PopupView
     */
    fun setEditMode(isEnable: Boolean) {
        _isPopupInEditMode.value = isEnable
    }

    /**
     * Resets the result popup of the identify operation
     */
    fun resetIdentifiedPopup() {
        _identifiedPopup.value = null
    }

    /**
     * Cancels the edit mode.
     */
    fun cancelEditing() {
        _popupManager.value?.cancelEditing()
        _isPopupInEditMode.value = false
    }

    /**
     * Raises ConfirmCancelPopupEditingEvent that can be observed and used for
     * prompting user with confirmation dialog to make sure the user wants to cancel edits.
     * To be followed by cancelEditing() if the user respond is positive.
     */
    fun confirmAndCancelEditing() {
        _confirmCancelPopupEditingEvent.raiseEvent()
    }

    /**
     * Saves the popup by applying changes to the feature service.
     */
    fun savePopup() {
        // show the Progress bar informing user that save operation is in progress
        _toggleSavingPopupProgressBarEvent.raiseEvent(true)
        _popupManager.value?.let { popupManager ->
            // Call finishEditingAsync() to apply edit changes locally and end the popup manager
            // editing session
            val finishEditingFuture: ListenableFuture<ArcGISRuntimeException> =
                popupManager.finishEditingAsync()
            finishEditingFuture.addDoneListener {
                try {
                    finishEditingFuture.get()

                    // The edits were applied successfully to the local geodatabase,
                    // push those changes to the ServiceFeatureTable by calling applyEditsAsync()
                    val feature: Feature = popupManager.popup.geoElement as Feature
                    val featureTable: FeatureTable = feature.featureTable
                    if (featureTable is ServiceFeatureTable) {
                        val applyEditsFuture: ListenableFuture<List<FeatureEditResult>> =
                            featureTable.applyEditsAsync()
                        applyEditsFuture.addDoneListener {
                            // dismiss the Progress bar
                            _toggleSavingPopupProgressBarEvent.raiseEvent(false)
                            // dismiss edit mode
                            _isPopupInEditMode.value = false
                            try {
                                val featureEditResults: List<FeatureEditResult> =
                                    applyEditsFuture.get()
                                // Check for errors in FeatureEditResults
                                if (featureEditResults.any { result -> result.hasCompletedWithErrors() }) {
                                    // an error was encountered when trying to apply edits
                                    val exception =
                                        featureEditResults.filter { featureEditResult -> featureEditResult.hasCompletedWithErrors() }[0].error
                                    // show the error message to the user
                                    exception.message?.let { exceptionMessage ->
                                        _showSavePopupErrorEvent.raiseEvent(exceptionMessage)
                                    }
                                }

                            } catch (exception: Exception) {
                                exception.message?.let { exceptionMessage ->
                                    _showSavePopupErrorEvent.raiseEvent(exceptionMessage)
                                }
                            }
                        }
                    }
                } catch (exception: Exception) {
                    exception.message?.let { exceptionMessage ->
                        _showSavePopupErrorEvent.raiseEvent(exceptionMessage)
                    }
                }
            }
        }
    }
}
