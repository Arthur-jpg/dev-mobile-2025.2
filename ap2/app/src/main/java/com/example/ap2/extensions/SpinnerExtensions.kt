package com.example.ap2.extensions

import android.view.View
import android.widget.AdapterView
import android.widget.Spinner

fun <T> Spinner.onItemSelected(block: (T) -> Unit) {
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            @Suppress("UNCHECKED_CAST")
            block(selectedItem as T)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) = Unit
    }
}
