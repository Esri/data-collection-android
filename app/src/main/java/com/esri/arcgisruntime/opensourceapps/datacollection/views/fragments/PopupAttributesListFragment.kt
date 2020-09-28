/*
 * Copyright 2020 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
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
import com.esri.arcgisruntime.opensourceapps.datacollection.R
import com.esri.arcgisruntime.opensourceapps.datacollection.databinding.FragmentPopupAttributeListBinding
import com.esri.arcgisruntime.opensourceapps.datacollection.util.observeEvent
import com.esri.arcgisruntime.opensourceapps.datacollection.viewmodels.IdentifyResultViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_popup_attribute_list.*

/**
 * Responsible for displaying PopupAttribute list.
 */
class PopupAttributesListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentPopupAttributeListBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_popup_attribute_list,
            container,
            false
        )

        val viewModel: IdentifyResultViewModel by activityViewModels()

        binding.identifyResultViewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.savePopupEvent.observeEvent(viewLifecycleOwner) {
            popupView.saveEdit = true
        }

        return binding.root
    }
}
