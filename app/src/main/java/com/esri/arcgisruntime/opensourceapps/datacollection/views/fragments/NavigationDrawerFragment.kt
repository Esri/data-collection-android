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
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.esri.arcgisruntime.opensourceapps.datacollection.R
import com.esri.arcgisruntime.opensourceapps.datacollection.databinding.FragmentNavigationDrawerBinding
import com.esri.arcgisruntime.opensourceapps.datacollection.viewmodels.DataCollectionViewModel
import com.esri.arcgisruntime.opensourceapps.datacollection.viewmodels.MapViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_navigation_drawer.*

/**
 * Represents the navigation drawer fragment of the Data Collection application.
 */
class NavigationDrawerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val fragmentNavigationDrawerBinding: FragmentNavigationDrawerBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_navigation_drawer,
                container,
                false
            )
        val view = fragmentNavigationDrawerBinding.root

        val mapViewModel: MapViewModel by activityViewModels()
        val dataCollectionViewModel: DataCollectionViewModel by activityViewModels {
            DataCollectionViewModel.Factory(
                requireActivity().application,
                mapViewModel
            )
        }
        fragmentNavigationDrawerBinding.dataCollectionViewModel = dataCollectionViewModel
        fragmentNavigationDrawerBinding.lifecycleOwner = this

        dataCollectionViewModel.showAddOauthConfigurationSnackbar.observe(
            viewLifecycleOwner,
            Observer {
                Snackbar.make(
                    authenticateButton,
                    R.string.enter_valid_oauth_config,
                    Snackbar.LENGTH_SHORT
                ).show()
            })

        return view
    }

}
