package com.ilkcanyilmaz.wordrival.adapters

import android.R
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.TextView
import com.ilkcanyilmaz.wordrival.databinding.ItemFriendBinding
import com.ilkcanyilmaz.wordrival.databinding.ItemFriendSpinnerBinding
import com.ilkcanyilmaz.wordrival.models.Friend


class SpinnerAdapter(context: Context, private var values: Array<String>) : BaseAdapter() {

    override fun getCount(): Int {
        return values.size
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val productRowBinding: ItemFriendSpinnerBinding =
            ItemFriendSpinnerBinding.inflate(LayoutInflater.from(parent?.context), parent, false)

        productRowBinding.txtName.text=values[position]
        return productRowBinding.root
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val productRowBinding: ItemFriendSpinnerBinding =
            ItemFriendSpinnerBinding.inflate(LayoutInflater.from(parent?.context), parent, false)

        productRowBinding.txtName.text=values[position]
        return productRowBinding.root
    }
}