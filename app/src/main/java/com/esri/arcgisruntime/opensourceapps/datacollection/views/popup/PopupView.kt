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
import android.content.res.ColorStateList
import android.graphics.Color
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
import com.esri.arcgisruntime.data.Field
import com.esri.arcgisruntime.mapping.popup.Popup
import com.esri.arcgisruntime.mapping.popup.PopupField
import com.esri.arcgisruntime.mapping.popup.PopupManager
import com.esri.arcgisruntime.opensourceapps.datacollection.BR
import com.esri.arcgisruntime.opensourceapps.datacollection.R
import kotlinx.android.synthetic.main.item_popup_row.view.*
import kotlinx.android.synthetic.main.layout_popupview.view.*

/**
 * Displays the popup attribute list in a [RecyclerView].
 */
class PopupView : FrameLayout {

    private val popupAttributeListAdapter by lazy { PopupAttributeListAdapter() }
    private var isEditMode: Boolean = false

    lateinit var popupManager: PopupManager
    lateinit var popup: Popup

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
     * Cancels the current editing session.
     */
    fun cancelEditing() {
        popupManager.cancelEditing()
    }

    /**
     * Enables/Disables edit mode on the PopupView.
     */
    fun setEditMode(isEnabled: Boolean) {
        isEditMode = isEnabled
        if (isEnabled) {
            popupAttributeListAdapter.submitList(popupManager.editableFields)
            popupManager.startEditing()
        } else {
            popupAttributeListAdapter.submitList(popupManager.displayedFields)
        }
        popupAttributeListAdapter.notifyDataSetChanged()
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
            holder.updateViewMode(popupField)

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

        val popupFieldLabel: TextView by lazy {
            binding.root.popupField
        }

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
        fun updateViewMode(popupField: PopupField) {
            if (isEditMode) {
                popupFieldEditValue.visibility = View.VISIBLE
                popupFieldValue.visibility = View.GONE
                //save original colors
                val oldColors: ColorStateList = popupFieldLabel.textColors
                // here we assign and hold the values of the editable fields, entered by the user
                // in popupAttribute.tempValue
                popupFieldEditValue.doAfterTextChanged {
                    if (popupFieldEditValue.hasFocus()) {
                        val validationError: ArcGISRuntimeException? = popupManager.updateValue(
                            popupFieldEditValue.text.toString(),
                            popupField
                        )

                        if (validationError != null) {
                            val fieldLabelWithValidationError = popupField.label + ": " + validationError.message
                            popupFieldLabel.text = fieldLabelWithValidationError
                            popupFieldLabel.setTextColor(Color.RED)
                        } else {
                            popupFieldLabel.text = popupField.label
                            popupFieldLabel.setTextColor(oldColors)
                        }
                    }
                }
            } else {
                popupFieldEditValue.visibility = View.GONE
                popupFieldValue.visibility = View.VISIBLE
            }
        }
    }
}
