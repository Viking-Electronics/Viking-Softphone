package com.vikingelectronics.softphone.bindingadapters

import androidx.core.widget.doOnTextChanged
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlin.reflect.KFunction1

interface TextChangedListener {
    fun textChanged(inputEditText: TextInputEditText)
}
@BindingAdapter("onTextChanged")
fun TextInputEditText.bindOnTextChanged(listener: KFunction1<CharSequence?, Unit>) {
    doOnTextChanged { text, _, _, _ -> listener.invoke(text) }

//    this.addTextChangedListener()
}

@BindingAdapter("errorText")
fun TextInputLayout.bindErrorStuff(errorText: String = "This field is required") {
    error = errorText
}