/*
 * Copyright 2020 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.esri.arcgisruntime.opensourceapps.datacollection.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.layers.LayerContent
import com.esri.arcgisruntime.mapping.GeoElement
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult
import com.esri.arcgisruntime.toolkit.util.Event
import com.esri.arcgisruntime.toolkit.util.raiseEvent
import com.esri.arcgisruntime.toolkit.popup.PopupViewModel

/**
 * The view model for IdentifyResultFragment, that is responsible for processing the result of
 * identify layer operation on MapView, highlighting the selected feature and displaying the values
 * of the display fields of result Popup in the bottom sheet.
 */
class IdentifyResultViewModel(val popupViewModel: PopupViewModel) : ViewModel() {

    private val _showIdentifyResultEvent = MutableLiveData<Event<Unit>>()
    val showIdentifyResultEvent: LiveData<Event<Unit>> = _showIdentifyResultEvent

    private val _dismissIdentifyResultEvent = MutableLiveData<Event<Unit>>()
    val dismissIdentifyResultEvent: LiveData<Event<Unit>> = _dismissIdentifyResultEvent

    private val _identifyLayerResult = MutableLiveData<IdentifyLayerResult>()
    val identifyLayerResult: LiveData<IdentifyLayerResult> = _identifyLayerResult

    private val _showPopupEvent = MutableLiveData<Event<Unit>>()
    val showPopupEvent: LiveData<Event<Unit>> = _showPopupEvent

    /**
     * Factory class to help create an instance of [IdentifyResultViewModel] with a [PopupViewModel].
     */
    class Factory(private val popupViewModel: PopupViewModel) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return IdentifyResultViewModel(popupViewModel) as T
        }
    }

    /**
     * <ul>
     * <li>Sets the identifyLayerResult instance on the identifyResultViewModel
     * <li>Sets the first popup returned by identifyLayerResult on PopupViewModel
     * <li>Raises an event to show the identify result
     * <li>Highlights the result popup in the GeoView
     * </ul>
     *
     * @param identifyLayerResult
     */
    fun processIdentifyLayerResult(
        identifyLayerResult: IdentifyLayerResult
    ) {
        if (identifyLayerResult.popups.size > 0) {
            _identifyLayerResult.value = identifyLayerResult
            popupViewModel.setPopup(identifyLayerResult.popups[0])
            _showIdentifyResultEvent.raiseEvent()
            highlightFeatureInFeatureLayer(
                identifyLayerResult.layerContent,
                identifyLayerResult.popups[0].geoElement
            )
        }
    }

    /**
     * <ul>
     * <li>Resets the result of the identify operation
     * <li>Clears the popup set on PopupViewModel
     * </ul>
     */
    fun resetIdentifyLayerResult() {
        _identifyLayerResult.value = null
        popupViewModel.clearPopup()
    }

    /**
     * Raises an event to dismiss the identify results
     */
    fun dismissIdentifyLayerResult() {
        _dismissIdentifyResultEvent.raiseEvent()
    }

    /**
     * Raises an event to show the Popup
     */
    fun showPopup() {
        _showPopupEvent.raiseEvent()
    }

    /**
     * Highlights the GeoElement in the GeoView.
     *
     * @param layerContent
     * @param geoelement
     */
    fun highlightFeatureInFeatureLayer(layerContent: LayerContent, geoelement: GeoElement) {
        val featureLayer: FeatureLayer? = layerContent as? FeatureLayer
        featureLayer?.selectFeature(geoelement as Feature)
    }
}
