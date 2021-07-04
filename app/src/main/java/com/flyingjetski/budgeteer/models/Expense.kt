package com.flyingjetski.budgeteer.models

import android.widget.Toast
import androidx.fragment.app.Fragment
import com.flyingjetski.budgeteer.AuthActivity
import com.flyingjetski.budgeteer.models.enums.Currency
import com.flyingjetski.budgeteer.models.enums.Feedback
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import java.util.*

class Expense(
    uid        : String?,
    date       : Date,
    sourceId   : String,
    source     : Source?,
    currency   : Currency,
    categoryId : String,
    category   : ExpenseCategory?,
    label      : String,
    amount     : Double,
    details    : String?,
    feedback   : Feedback,
) {
    var id: String? = null
    val uid        = uid
    val date       = date
    val sourceId   = sourceId
    val source     = source
    val currency   = currency
    val categoryId = categoryId
    val category   = category
    val label      = label
    val amount     = amount
    val details    = details
    val feedback   = feedback

    constructor(): this(null, Date(), "", Source(), Currency.MYR, "", ExpenseCategory(), "", 0.0, "", Feedback.NEUTRAL)

    companion object {
        fun insertExpense(expense: Expense) {
            AuthActivity().db.collection("Expenses").add(expense)
        }

        fun updateExpenseById(
            id         : String,
            date       : Date?,
            sourceId   : String?,
            currency   : Currency?,
            categoryId : String?,
            label      : String?,
            amount     : Double?,
            details    : String?,
            feedback   : Feedback?,

        ) {
            val data = HashMap<String, Any>()
            if (date != null) {
                data["date"] = date
            }
            if (sourceId != null && sourceId != "") {
                data["sourceId"] = sourceId
            }
            if (currency != null) {
                data["currency"] = currency
            }
            if (categoryId != null && categoryId != "") {
                data["categoryId"] = categoryId
            }
            if (label != null && label != "") {
                data["label"] = label
            }
            if (amount != null && amount != 0.0) {
                data["amount"] = amount
            }
            if (details != null && details != "") {
                data["details"] = details
            }
            if (feedback != null) {
                data["feedback"] = feedback
            }
            AuthActivity().db.collection("Expenses")
                .document(id).update(data)
        }

        fun deleteExpenseById(id: String) {
            AuthActivity().db.collection("Expenses")
                .document(id).delete()
        }

        fun getExpenseById(id: String): Task<DocumentSnapshot> {
            return AuthActivity().db.collection("Expenses")
                .document(id).get()
        }

        fun getExpense(): Query {
            return AuthActivity().db.collection("Expenses")
                .whereEqualTo("uid", AuthActivity().auth.uid.toString())
        }
    }

}