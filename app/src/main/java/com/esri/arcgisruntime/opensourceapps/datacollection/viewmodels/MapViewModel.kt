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

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.esri.arcgisruntime.geometry.GeometryType
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap

/**
 * The MapViewModel to display and manipulate the [ArcGISMap]
 */
class MapViewModel : ViewModel() {

    val map: MutableLiveData<ArcGISMap> = MutableLiveData()

    /**
     * Defines the feature layer Data Collection wants to perform identify operation on.
     * Filters all the operational layers in the map based on if they are
     * a. instances of feature layer
     * b. have Point type geometry
     * c. is visible
     * d. has popup enabled and contains popup definition,
     *
     * and returns the first value in the resulting LayerList
     */
    val identifiableLayer: FeatureLayer?
        get() {
            return map.value?.operationalLayers?.filterIsInstance<FeatureLayer>()?.filter {
                (it.featureTable?.geometryType == GeometryType.POINT)
                    .and(it.isVisible)
                    .and(it.isPopupEnabled && it.popupDefinition != null)
            }?.get(0)
        }

}
