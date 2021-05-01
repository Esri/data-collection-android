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

package com.esri.arcgisruntime.opensourceapps.datacollection.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.opensourceapps.datacollection.R
import com.esri.arcgisruntime.opensourceapps.datacollection.databinding.FragmentDataCollectionBinding
import com.esri.arcgisruntime.opensourceapps.datacollection.util.Logger
import com.esri.arcgisruntime.opensourceapps.datacollection.viewmodels.DataCollectionViewModel
import com.esri.arcgisruntime.opensourceapps.datacollection.viewmodels.IdentifyResultViewModel
import com.esri.arcgisruntime.opensourceapps.datacollection.viewmodels.MapViewModel
import com.esri.arcgisruntime.security.AuthenticationManager
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler
import com.esri.arcgisruntime.toolkit.popup.PopupViewModel
import com.esri.arcgisruntime.toolkit.util.observeEvent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HALF_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.google.android.material.bottomsheet.BottomSheetBehavior.from
import kotlinx.android.synthetic.main.fragment_data_collection.*
import java.security.InvalidParameterException
import kotlin.math.roundToInt

/**
 * Represents the main fragment of the Data Collection application.
 */
class DataCollectionFragment : Fragment() {

    // mapViewModel and dataCollectionViewModel are shared with NavigationDrawerFragment, thus we
    // use by activityViewModels()
    private val mapViewModel: MapViewModel by activityViewModels()

    private val dataCollectionViewModel: DataCollectionViewModel by activityViewModels {
        DataCollectionViewModel.Factory(
            requireActivity().application,
            mapViewModel
        )
    }

    // popupViewModel and identifyResultViewModel is shared with IdentifyResultFragment, thus we
    // use by activityViewModels()
    private val popupViewModel: PopupViewModel by activityViewModels()

    private val identifyResultViewModel: IdentifyResultViewModel by activityViewModels {
        IdentifyResultViewModel.Factory(popupViewModel)
    }

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private lateinit var bottomSheetNavController: NavController

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            when {
                // we handle the back button press here to navigate back in the bottomsheet.
                // when the user presses the back button when PopupAttributesListFragment is showing
                // from the bottomsheet_navigation graph we pop it from the BackStack to go to the
                // previous IdentifyResultFragment. When the backstack is at identifyResultFragment
                // popBackStack() will return a false and we will exit the DataCollectionActivity.
                !bottomSheetNavController.popBackStack(R.id.identifyResultFragment, false) ->
                    requireActivity().finish()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentDataCollectionBinding: FragmentDataCollectionBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_data_collection,
            container,
            false
        )

        val view = fragmentDataCollectionBinding.root

        fragmentDataCollectionBinding.dataCollectionViewModel = dataCollectionViewModel
        fragmentDataCollectionBinding.lifecycleOwner = this

        val handler = DefaultAuthenticationChallengeHandler(activity)
        AuthenticationManager.setAuthenticationChallengeHandler(handler)

        bottomSheetBehavior = from(fragmentDataCollectionBinding.bottomSheetContainer)
        val bottomSheetNavHostFragment =
            childFragmentManager.findFragmentById(R.id.bottomSheetNavHostFragment)
                ?: throw InvalidParameterException("bottomSheetNavHostFragment must exist")
        bottomSheetNavController = bottomSheetNavHostFragment.findNavController()

        dataCollectionViewModel.bottomSheetState.observe(viewLifecycleOwner, { bottomSheetState ->
            if (bottomSheetState == STATE_HIDDEN) {
                bottomSheetBehavior.isHideable = true
            }
            bottomSheetBehavior.state = bottomSheetState
        })

        // On orientation change if we have a valid value for identifyLayerResult,
        // we highlight the first feature from the result.
        identifyResultViewModel.identifyLayerResult.value?.let {
            identifyResultViewModel.highlightFeatureInFeatureLayer(it.layerContent, it.popups[0].geoElement)
        }

        dataCollectionViewModel.closeDrawerEvent.observeEvent(viewLifecycleOwner) {
            drawer_layout.closeDrawer(GravityCompat.START)
        }

        identifyResultViewModel.showPopupEvent.observeEvent(viewLifecycleOwner) {
            bottomSheetNavController.navigate(R.id.action_identifyResultFragment_to_popupFragment)
        }

        identifyResultViewModel.showIdentifyResultEvent.observeEvent(viewLifecycleOwner) {
            // IdentifyResultFragment shows a few selected popup attributes. We
            // show them in half expanded state of the bottom sheet
            if (dataCollectionViewModel.bottomSheetState.value == STATE_HIDDEN) {
                bottomSheetBehavior.isHideable = false
                dataCollectionViewModel.setCurrentBottomSheetState(STATE_HALF_EXPANDED)
            }
        }

        popupViewModel.dismissPopupEvent.observeEvent(viewLifecycleOwner) {
            resetIdentifyResult()
            dataCollectionViewModel.setCurrentBottomSheetState(STATE_HIDDEN)
            bottomSheetNavController.popBackStack(R.id.identifyResultFragment, false)
        }

        identifyResultViewModel.dismissIdentifyResultEvent.observeEvent(viewLifecycleOwner) {
            resetIdentifyResult()
            dataCollectionViewModel.setCurrentBottomSheetState(STATE_HIDDEN)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // setup the app bar and drawer layout
        val navController = activity?.findNavController(R.id.navHostFragment)
        navController?.let {
            val appBarConfiguration = AppBarConfiguration(navController.graph, drawer_layout)
            view.findViewById<Toolbar>(R.id.toolbar)
                .setupWithNavController(navController, appBarConfiguration)
        }

        bottomSheetNavController.addOnDestinationChangedListener { _, destination, _ ->
            // set title on the toolbar
            when (destination.id) {
                R.id.identifyResultFragment -> {
                    toolbar.title = dataCollectionViewModel.portalItemTitle.value
                    popupViewModel.cancelEditing()
                }
                R.id.popupFragment -> {
                    toolbar.title = dataCollectionViewModel.portalItemTitle.value
                }
            }
        }

        mapView.onTouchListener =
            object : DefaultMapViewOnTouchListener(context, mapView) {
                override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {

                    // Only perform identify on the mapview if the Popup is not in edit mode
                    if (popupViewModel.isPopupInEditMode.value == false) {
                        // If the user tapped on the mapview to perform an identify and
                        // is currently looking at a popup's attributes we move back to
                        // IdentifyResultFragment to perform identify
                        if (bottomSheetNavController.currentDestination?.id == R.id.popupFragment) {
                            bottomSheetNavController.popBackStack(R.id.identifyResultFragment, false)
                        }

                        dataCollectionViewModel.setCurrentBottomSheetState(STATE_HIDDEN)

                        e?.let {
                            val screenPoint = android.graphics.Point(
                                it.x.roundToInt(),
                                it.y.roundToInt()
                            )
                            identifyLayer(screenPoint)
                        }
                    }
                    return true
                }
            }
    }

    /**
     * Performs an identify on the identifiable layer at the given screen point.
     *
     * @param screenPoint in Android graphic coordinates.
     */
    private fun identifyLayer(screenPoint: android.graphics.Point) {

        mapViewModel.identifiableLayer?.let {
            // Clear the selected features from the feature layer
            resetIdentifyResult()

            val identifyLayerResultsFuture = mapView
                .identifyLayerAsync(mapViewModel.identifiableLayer, screenPoint, 5.0, true)

            identifyLayerResultsFuture.addDoneListener {
                try {
                    val identifyLayerResult = identifyLayerResultsFuture.get()

                    identifyResultViewModel.processIdentifyLayerResult(identifyLayerResult)
                } catch (e: Exception) {
                    Logger.i("Error identifying results ${e.message}")
                }
            }
        }
    }

    /**
     * Clears the selected features from the feature layer and nullifies the identify results in
     * identifyResultsViewModel.
     */
    private fun resetIdentifyResult() {
        mapViewModel.identifiableLayer?.clearSelection()
        identifyResultViewModel.resetIdentifyLayerResult()
    }

    override fun onPause() {
        super.onPause()
        mapView.pause()
    }

    override fun onResume() {
        super.onResume()
        mapView.resume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.map = null
        mapView.dispose()
    }
}
