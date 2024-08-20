package com.lovelycatv.ai.shadowcat.app.util.android

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.StringRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lovelycatv.ai.shadowcat.app.R
import com.lovelycatv.ai.shadowcat.app.util.common.runIfNotNull

fun <T : CharSequence> T.showTips(context: Context) {
    DialogUtils.showTips(context, this.toString()).positive().show()
}

class DialogUtils {
    companion object {
        @JvmStatic
        fun showTips(context: Context, title: String, message: String): MaterialAlertDialogBuilder {
            return MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
        }

        @JvmStatic
        fun showTips(context: Context, message: String): MaterialAlertDialogBuilder {
            return MaterialAlertDialogBuilder(context)
                .setMessage(message)
                .setCancelable(true)
        }

        @JvmStatic
        fun showProgress(context: Context, text: String): MaterialAlertDialogBuilder {
            return showProgress(context, null, text)
        }

        @JvmStatic
        fun showProgress(context: Context, title: String? = null, message: String): MaterialAlertDialogBuilder {
            val view = LayoutInflater.from(context).inflate(R.layout.progress_dialog_layout, null, false)

            val textView = view.findViewById<TextView>(R.id.progress_dialog_text)
            textView.text = message

            return MaterialAlertDialogBuilder(context).apply {
                this.setView(view)
                title?.let { this.setTitle(it) }
            }
        }

        @JvmStatic
        fun showInput(
            context: Context,
            title: String? = null,
            summary: String? = null,
            inputText: String? = null,
            confirmButtonText: String? = null,
            fx: (dialog: DialogInterface, text: String) -> Unit
        ): MaterialAlertDialogBuilder {
            val view = LayoutInflater.from(context).inflate(R.layout.input_dialog_layout, null, false)

            val messageView = view.findViewById<TextView>(R.id.input_dialog_message)
            val edittext = view.findViewById<EditText>(R.id.input_dialog_edittext)

            if (summary != null) {
                messageView.text = summary
            } else {
                messageView.visibility = View.GONE
            }

            if (inputText != null) {
                edittext.setText(inputText)
            }

            return MaterialAlertDialogBuilder(context).apply {
                this.setView(view)
                title?.let { this.setTitle(it) }
                this.positive(confirmButtonText) {
                    fx(it, edittext.text.toString())
                }
            }
        }

        @JvmStatic
        fun listSelector(
            context: Context,
            selections: Array<out CharSequence>,
            fx: (dialog: DialogInterface, index: Int) -> Unit
        ): MaterialAlertDialogBuilder {
            return listSelector(context, null, selections, fx)
        }

        @JvmStatic
        fun listSelector(
            context: Context,
            title: String? = null,
            selections: Array<out CharSequence>,
            fx: (dialog: DialogInterface, index: Int) -> Unit
        ): MaterialAlertDialogBuilder {
            return MaterialAlertDialogBuilder(context).apply {
                this.runIfNotNull {
                    this.setTitle(title)
                }
                this.setCancelable(true)
                this.setItems(selections) { dialog, index ->
                    fx(dialog, index)
                }
            }
        }

        @JvmStatic
        fun singleChoice(
            context: Context,
            title: String,
            selections: Array<out CharSequence>,
            defaultCheckIndex: Int = 0,
            fx: (dialog: DialogInterface, index: Int) -> Unit
        ): MaterialAlertDialogBuilder {
            return MaterialAlertDialogBuilder(context).apply {
                this.setTitle(title)
                this.setCancelable(true)
                this.setSingleChoiceItems(selections, defaultCheckIndex) { dialog, index ->
                    fx(dialog, index)
                }
            }
        }

        @JvmStatic
        fun multiChoice(
            context: Context,
            title: String,
            selections: Array<out CharSequence>,
            defaultCheckIndex: BooleanArray,
            fx: (dialog: DialogInterface, checkedItems: List<Int>) -> Unit
        ): MaterialAlertDialogBuilder {
            val checkedItems = mutableListOf<Int>()
            return MaterialAlertDialogBuilder(context).apply {
                this.setTitle(title)
                this.setCancelable(true)
                this.setMultiChoiceItems(selections, defaultCheckIndex) { dialog, index, checked ->
                    if (checked) {
                        checkedItems.add(index)
                    } else {
                        checkedItems.remove(index)
                    }
                    fx(dialog, checkedItems)
                }
            }
        }
    }
}

fun MaterialAlertDialogBuilder.notCancelable(): MaterialAlertDialogBuilder = this.setCancelable(false)

fun MaterialAlertDialogBuilder.positive(buttonText: String? = null, fx: (DialogInterface) -> Unit = { it.dismiss() }): MaterialAlertDialogBuilder {
    return this.apply {
        if (buttonText == null) {
            this.setPositiveButton(R.string.dialog_button_confirm) { dialog, _ -> fx(dialog) }
        } else {
            this.setPositiveButton(buttonText) { dialog, _ -> fx(dialog) }
        }
    }
}

fun MaterialAlertDialogBuilder.positive(@StringRes buttonText: Int, fx: (DialogInterface) -> Unit = { it.dismiss() }): MaterialAlertDialogBuilder {
    return this.positive(context.getString(buttonText), fx)
}

fun MaterialAlertDialogBuilder.negative(buttonText: String? = null, fx: (DialogInterface) -> Unit = { it.dismiss() }): MaterialAlertDialogBuilder {
    return this.apply {
        if (buttonText == null) {
            this.setNegativeButton(R.string.dialog_button_cancel) { dialog, _ -> fx(dialog) }
        } else {
            this.setNegativeButton(buttonText) { dialog, _ -> fx(dialog) }
        }
    }
}

fun MaterialAlertDialogBuilder.negative(@StringRes buttonText: Int, fx: (DialogInterface) -> Unit = { it.dismiss() }): MaterialAlertDialogBuilder {
    return this.negative(context.getString(buttonText), fx)
}

fun MaterialAlertDialogBuilder.neutral(buttonText: String, fx: (DialogInterface) -> Unit = { it.dismiss() }): MaterialAlertDialogBuilder {
    return this.apply {
        this.setNeutralButton(buttonText) { dialog, _ -> fx(dialog) }
    }
}

fun MaterialAlertDialogBuilder.neutral(@StringRes buttonText: Int, fx: (DialogInterface) -> Unit = { it.dismiss() }): MaterialAlertDialogBuilder {
    return this.neutral(context.getString(buttonText), fx)
}

fun Context.stringYes() = this.getString(R.string.dialog_button_yes)
fun Context.stringNo() = this.getString(R.string.dialog_button_no)
fun Context.stringOk() = this.getString(R.string.dialog_button_ok)
fun Context.stringConfirm() = this.getString(R.string.dialog_button_confirm)
fun Context.stringCancel() = this.getString(R.string.dialog_button_cancel)
fun Context.stringContinue() = getString(R.string.dialog_button_continue)
