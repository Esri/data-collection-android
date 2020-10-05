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

package com.esri.arcgisruntime.opensourceapps.datacollection.views.popup

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.esri.arcgisruntime.ArcGISRuntimeException
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.FeatureEditResult
import com.esri.arcgisruntime.data.FeatureTable
import com.esri.arcgisruntime.data.Field
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.mapping.popup.Popup
import com.esri.arcgisruntime.mapping.popup.PopupField
import com.esri.arcgisruntime.mapping.popup.PopupManager
import com.esri.arcgisruntime.opensourceapps.datacollection.BR
import com.esri.arcgisruntime.opensourceapps.datacollection.R
import com.esri.arcgisruntime.opensourceapps.datacollection.util.Logger
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.item_popup_row.view.*
import kotlinx.android.synthetic.main.layout_popupview.view.*
import java.util.concurrent.ExecutionException

/**
 * Displays the popup attribute list in a [RecyclerView].
 */
class PopupView : FrameLayout {

    private val popupAttributeListAdapter by lazy { PopupAttributeListAdapter() }

    var popup: Popup? = null
        set(value) {
            field = value
            popupAttributeListAdapter.submitList(popupManager.displayedFields)
        }

    var editMode: Boolean = false
        set(value) {
            field = value
            if (field) {
                popupAttributeListAdapter.submitList(popupManager.editableFields)
                popupManager.startEditing()
            } else if (popup != null) {
                popupAttributeListAdapter.submitList(popupManager.displayedFields)
                if (!saveEdit) {
                    popupManager.cancelEditing()
                }
            }
            popupAttributeListAdapter.notifyDataSetChanged()
        }

    var saveEdit: Boolean = false
        set(value) {field = value
            if (field) {
                savePopup()
                editMode = false
            }
        }

    private val popupManager by lazy {
        PopupManager(context, popup)
    }

    /**
     * Constructor used when instantiating this View directly to attach it to another view programmatically.
     */
    constructor(context: Context) : super(context) {
        init(context)
    }

    /**
     * Constructor used when defining this view in an XML layout.
     */
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    /**
     * Initializes this PopupView by inflating the layout and setting the [RecyclerView] adapter.
     */
    private fun init(context: Context) {
        inflate(context, R.layout.layout_popupview, this)
        popupRecyclerView.layoutManager = LinearLayoutManager(context)
        popupRecyclerView.adapter = popupAttributeListAdapter
    }

    /**
     * Shows the message in a Snackbar.
     */
    private fun showSnackbarMessage(message: String) {
        val snackbar: Snackbar = Snackbar.make(this, message, Snackbar.LENGTH_LONG)
        snackbar.show()
    }

    /**
     * Saves the popup by applying changes to the feature service.
     */
    private fun savePopup() {
        val editingErrorFuture: ListenableFuture<ArcGISRuntimeException> = popupManager.finishEditingAsync()
        editingErrorFuture.addDoneListener {
            try {
                editingErrorFuture.get()

                val feature: Feature = popupManager.popup.geoElement as Feature
                val featureTable: FeatureTable = feature.featureTable
                if (featureTable is ServiceFeatureTable) {
                    val applyEditsFuture: ListenableFuture<List<FeatureEditResult>> =
                        featureTable.applyEditsAsync()
                    applyEditsFuture.addDoneListener {
                        try {
                            val featureEditResults: List<FeatureEditResult> = applyEditsFuture.get()
                            // Check for errors in FeatureEditResults
                            if (featureEditResults.any { result -> result.hasCompletedWithErrors() }) {
                                showSnackbarMessage("There was a problem saving the feature")
                            } else {
                                showSnackbarMessage("Feature saved successfully")
                            }

                        } catch (ie: InterruptedException) {
                            Logger.e(
                                "Exception encountered when saving popup, " + Util.getExceptionMessageAndCause(
                                    ie
                                )
                            )
                        } catch (ee: ExecutionException) {
                            Logger.e(
                                "Exception encountered when saving popup, " + Util.getExceptionMessageAndCause(
                                    ee
                                )
                            )
                        }
                    }
                }
            } catch (ie: InterruptedException) {
                Logger.e(
                    "Exception encountered when saving popup, " + Util.getExceptionMessageAndCause(
                        ie
                    )
                )
            } catch (ee: ExecutionException) {
                Logger.e(
                    "Exception encountered when saving popup, " + Util.getExceptionMessageAndCause(
                        ee
                    )
                )
            }
        }
    }

    /**
     * Adapter used by PopupView to display a list of PopupAttributes in a
     * recyclerView.
     */
    private inner class PopupAttributeListAdapter :
        ListAdapter<PopupField, ViewHolder>(DiffCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<ViewDataBinding>(
                inflater,
                R.layout.item_popup_row,
                parent,
                false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val popupField: PopupField = getItem(position)
            holder.setViewsBehavior(popupField)

            holder.bind(popupField)
        }
    }

    /**
     * Callback for calculating the diff between two non-null items in a list.
     */
    private class DiffCallback : DiffUtil.ItemCallback<PopupField>() {

        override fun areItemsTheSame(
            oldItem: PopupField,
            newItem: PopupField
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: PopupField,
            newItem: PopupField
        ): Boolean {
            return oldItem.fieldName == newItem.fieldName
        }
    }

    /**
     * The PopupAttributeListAdapter ViewHolder.
     */
    private inner class ViewHolder(private val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val popupFieldValue: TextView by lazy {
            binding.root.popupFieldValue
        }

        val popupFieldEditValue: EditText by lazy {
            binding.root.popupFieldEditValue
        }

        fun bind(
            popupField: PopupField
        ) {
            binding.setVariable(BR.popupField, popupField)
            binding.setVariable(BR.popupManager, popupManager)
            binding.executePendingBindings()
        }

        /**
         * Toggles the view for popup field value from edittext to textview and vice-versa, given the
         * edit mode of the popupView.
         */
        fun setViewsBehavior(popupField: PopupField) {
            if (editMode) {
                popupFieldEditValue.inputType = getInputType(popupManager.getFieldType(popupField))
                popupFieldEditValue.visibility = View.VISIBLE
                popupFieldValue.visibility = View.GONE
                // here we assign and hold the values of the editable fields, entered by the user
                // in popupAttribute.tempValue
                popupFieldEditValue.doAfterTextChanged {
                    if (popupFieldEditValue.hasFocus()) {
                        val validationError: ArcGISRuntimeException? = popupManager.updateValue(
                            popupFieldEditValue.text.toString(),
                            popupField
                        )
                        validationError?.let {
                            Util.getExceptionMessageAndCause(it)
                            val snackbar: Snackbar = Snackbar.make(
                                binding.root,
                                Util.getExceptionMessageAndCause(it) as CharSequence,
                                Snackbar.LENGTH_LONG
                            )
                            snackbar.show()
                        }
                    }
                }
            } else {
                popupFieldEditValue.visibility = View.GONE
                popupFieldValue.visibility = View.VISIBLE
            }
        }

        /**
         * Returns the int value representing the input type for EditText view.
         */
        private fun getInputType(fieldType: Field.Type): Int {
            return when (fieldType) {
                Field.Type.SHORT, Field.Type.INTEGER -> InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
                Field.Type.FLOAT, Field.Type.DOUBLE -> InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
                Field.Type.DATE -> InputType.TYPE_CLASS_DATETIME or InputType.TYPE_DATETIME_VARIATION_DATE or InputType.TYPE_DATETIME_VARIATION_NORMAL
                else -> InputType.TYPE_CLASS_TEXT
            }
        }
    }

    /**
     * Helper class containing utility static methods.
     */
    private class Util {

        companion object {

            /**
             * Gets the full error message from the exception
             *
             * @param exception - Exception
             */
            fun getExceptionMessageAndCause(exception: Exception): String? {
                var message = exception.message
                if (exception.cause != null) {
                    message = message + " Cause = " + exception.cause!!.message
                    if (exception.cause is ArcGISRuntimeException) {
                        val arcGISRuntimeException =
                            exception.cause as ArcGISRuntimeException?
                        message += ", additional message: " + arcGISRuntimeException!!.additionalMessage
                    }
                }
                return message
            }
        }
    }

}
