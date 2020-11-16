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
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.layers.LayerContent
import com.esri.arcgisruntime.mapping.GeoElement
import com.esri.arcgisruntime.mapping.popup.Popup
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult

/**
 * The view model for IdentifyResultFragment, that is responsible for processing the result of
 * identify layer operation on MapView and displaying the values of the display fields of result
 * Popup in the bottom sheet.
 */
class IdentifyResultViewModel : ViewModel() {

    private val _identifiedPopup = MutableLiveData<Popup>()
    val identifiedPopup: LiveData<Popup> = _identifiedPopup

    private val _isShowPopupAttributeList = MutableLiveData<Boolean>()
    val isShowPopupAttributeList: LiveData<Boolean> = _isShowPopupAttributeList

    /**
     * Highlights the result popup in the GeoView.
     * Updates identifiedPopup property to set the popup field values being displayed in
     * the bottom sheet.
     *
     * @param identifyLayerResult
     */
    fun processIdentifyLayerResult(
        identifyLayerResult: IdentifyLayerResult
    ) {
        if (identifyLayerResult.popups.size > 0) {
            _identifiedPopup.value = identifyLayerResult.popups[0]
            highlightFeatureInFeatureLayer(
                identifyLayerResult.layerContent,
                identifyLayerResult.popups[0].geoElement
            )
        }
    }

    /**
     * Called when user taps on the identify result bottom sheet. Kicks off PopupAttributeListFragment
     */
    fun showPopupAttributeList() {
        _isShowPopupAttributeList.value = true
    }

    /**
     * Highlights the GeoElement in the GeoView.
     *
     * @param layerContent
     * @param geoelement
     */
    private fun highlightFeatureInFeatureLayer(layerContent: LayerContent, geoelement: GeoElement) {
        val featureLayer: FeatureLayer? = layerContent as? FeatureLayer
        featureLayer?.selectFeature(geoelement as Feature)
    }

}
