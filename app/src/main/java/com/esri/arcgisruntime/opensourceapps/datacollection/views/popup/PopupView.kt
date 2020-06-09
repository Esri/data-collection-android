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
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.esri.arcgisruntime.mapping.popup.Popup
import com.esri.arcgisruntime.mapping.popup.PopupField
import com.esri.arcgisruntime.mapping.popup.PopupManager
import com.esri.arcgisruntime.opensourceapps.datacollection.BR
import com.esri.arcgisruntime.opensourceapps.datacollection.R
import kotlinx.android.synthetic.main.layout_popupview.view.*

/**
 * Displays the popup attribute list in a [RecyclerView].
 */
class PopupView : FrameLayout {

    private val popupAttributeListAdapter by lazy { PopupAttributeListAdapter() }

    var popup: Popup? = null
        set(value) {
            field = value
            popupAttributeListAdapter.submitList(getPopupAttributeList())
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
     * Iterates over all the display fields of the popup to get their values and adds them to the
     * list of PopupAttributes.
     */
    private fun getPopupAttributeList(): ArrayList<PopupAttribute> {
        val popupManager = PopupManager(context, popup)
        val fields: List<PopupField> = popupManager.displayedFields
        val popupDisplayFieldsWithValues: ArrayList<PopupAttribute> = ArrayList()
        for (field in fields) {
            popupDisplayFieldsWithValues.add(
                PopupAttribute(
                    field.label,
                    popupManager.getFormattedValue(field)
                )
            )
        }
        return popupDisplayFieldsWithValues
    }

    /**
     * Represents an object comprising of Popup display field and its value.
     */
    data class PopupAttribute(val field: String, val value: String)

    /**
     * Adapter used by PopupView to display a list of PopupAttributes in a
     * recyclerView.
     */
    private inner class PopupAttributeListAdapter :
        ListAdapter<PopupAttribute, ViewHolder>(DiffCallback()) {

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

        override fun onBindViewHolder(holder: ViewHolder, position: Int) =
            holder.bind(getItem(position))
    }

    /**
     * Callback for calculating the diff between two non-null items in a list.
     */
    private class DiffCallback : DiffUtil.ItemCallback<PopupAttribute>() {

        override fun areItemsTheSame(
            oldItem: PopupAttribute,
            newItem: PopupAttribute
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: PopupAttribute,
            newItem: PopupAttribute
        ): Boolean {
            return oldItem.field == newItem.field && oldItem.value == newItem.value
        }
    }

    /**
     * The PopupAttributeListAdapter ViewHolder.
     */
    private class ViewHolder(private val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            popupAttribute: PopupAttribute
        ) {
            binding.setVariable(BR.popupAttribute, popupAttribute)
            binding.executePendingBindings()
        }
    }

}
