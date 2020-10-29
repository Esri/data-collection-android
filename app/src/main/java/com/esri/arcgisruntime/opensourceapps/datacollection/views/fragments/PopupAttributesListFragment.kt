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
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.esri.arcgisruntime.opensourceapps.datacollection.R
import com.esri.arcgisruntime.opensourceapps.datacollection.databinding.FragmentPopupAttributeListBinding
import com.esri.arcgisruntime.opensourceapps.datacollection.util.observeEvent
import com.esri.arcgisruntime.opensourceapps.datacollection.viewmodels.IdentifyResultViewModel
import com.esri.arcgisruntime.opensourceapps.datacollection.viewmodels.PopupViewModel
import kotlinx.android.synthetic.main.fragment_popup_attribute_list.*

/**
 * Responsible for displaying PopupAttribute list.
 */
class PopupAttributesListFragment : Fragment() {

    private val popupViewModel: PopupViewModel by activityViewModels()

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

        val identifyResultViewModel: IdentifyResultViewModel by activityViewModels()

        binding.identifyResultViewModel = identifyResultViewModel
        binding.popupViewModel = popupViewModel
        binding.lifecycleOwner = this

        popupViewModel.isPopupInEditMode.observe(viewLifecycleOwner, Observer {
            popupView.setEditMode(it)
        })

        popupViewModel.toggleSavingPopupProgressBarEvent.observeEvent(viewLifecycleOwner) { isShowProgressBar ->
            if (isShowProgressBar) {
                progressBarLayout.visibility = View.VISIBLE
            } else {
                progressBarLayout.visibility = View.GONE
            }
        }

        popupViewModel.showSavePopupErrorEvent.observeEvent(viewLifecycleOwner) { errorMessage ->
            showAlertDialog(errorMessage)
        }

        popupViewModel.confirmCancelPopupEditingEvent.observeEvent(viewLifecycleOwner) {
            showConfirmCancelEditingDialog()
        }

        return binding.root
    }

    /**
     * Shows dialog to confirm cancelling edit mode on popup view.
     */
    private fun showConfirmCancelEditingDialog() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage("Discard changes?")
            .setCancelable(false)
            // positive button text and action
            .setPositiveButton(getString(R.string.ok)) { dialog, id ->
                popupViewModel.cancelEditing()
            }
            // negative button text and action
            .setNegativeButton(getString(R.string.cancel)) { dialog, id -> dialog.cancel()
            }
        val alert = dialogBuilder.create()
        // show alert dialog
        alert.show()
    }

    /**
     * Shows error message to the use in a dialog.
     */
    private fun showAlertDialog(message: String) {

        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage(message)
            .setCancelable(false)
            // positive button text and action
            .setPositiveButton(getString(R.string.ok)) { dialog, id ->
                dialog.cancel()
            }
        val alert = dialogBuilder.create()
        // show alert dialog
        alert.show()
    }

}
