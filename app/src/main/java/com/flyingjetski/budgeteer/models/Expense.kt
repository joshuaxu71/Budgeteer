package com.flyingjetski.budgeteer.models

import android.util.Log
import com.flyingjetski.budgeteer.AuthActivity
import com.flyingjetski.budgeteer.Callback
import com.flyingjetski.budgeteer.MainActivity
import com.flyingjetski.budgeteer.models.enums.Currency
import com.flyingjetski.budgeteer.models.enums.Feedback
import com.google.firebase.firestore.Query
import java.util.*

class Expense(
    uid        : String?,
    date       : Date,
    sourceId   : String,
    source     : Source?,
    categoryId : String,
    category   : ExpenseCategory?,
    label      : String,
    amount     : Double,
    details    : String?,
    feedback   : Feedback,
) {
    var id: String? = null
    var currency: Currency? = null
    val uid        = uid
    val date       = date
    val sourceId   = sourceId
    val source     = source
    val categoryId = categoryId
    val category   = category
    val label      = label
    val amount     = amount
    val details    = details
    val feedback   = feedback

    constructor(): this(null, Date(), "", Source(), "", ExpenseCategory(), "", 0.0, "", Feedback.NEUTRAL)

    companion object {
        fun insertExpense(expense: Expense) {
            Source.getSourceById(expense.sourceId, object: Callback {
                override fun onCallback(value: Any) {
                    val source = value as Source
                    expense.currency = source.currency
                    MainActivity().db.collection("Expenses").add(expense)
                    Source.updateSourceAmountById(expense.sourceId, -expense.amount)
                    Budget.updateBudgetAmountSpent(expense.currency!!, null, null, expense.date, expense.amount)
                }
            })
        }

        fun updateExpenseById(
            id         : String,
            date       : Date?,
            sourceId   : String?,
            categoryId : String?,
            label      : String?,
            amount     : Double?,
            details    : String?,
            feedback   : Feedback?,
        ) {
            getExpenseById(id, object: Callback {
                override fun onCallback(value: Any) {
                    val expense = value as Expense
                    if (date != null && amount != null) {
                        Source.updateSourceAmountById(expense.sourceId, (expense.amount - amount))
                        Budget.updateBudgetAmountSpent(expense.currency!!, expense.date, -expense.amount, date, amount)
                    } else if (amount != null) {
                        Source.updateSourceAmountById(expense.sourceId, (expense.amount - amount))
                        Budget.updateBudgetAmountSpent(expense.currency!!, expense.date, -expense.amount, expense.date, amount)
                    } else if (date != null) {
                        Budget.updateBudgetAmountSpent(expense.currency!!, expense.date, -expense.amount, date, expense.amount)
                    }

                    val data = HashMap<String, Any>()
                    if (date != null) {
                        data["date"] = date
                    }
                    if (sourceId != null && sourceId != "") {
                        Source.getSourceById(sourceId, object: Callback {
                            override fun onCallback(value: Any) {
                                val source = value as Source
                                data["currency"] = source.currency
                            }
                        })
                        data["sourceId"] = sourceId
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
                    MainActivity().db.collection("Expenses")
                        .document(id).update(data)
                }
            })
        }

        fun deleteExpenseById(id: String) {
            getExpenseById(id, object: Callback {
                override fun onCallback(value: Any) {
                    val expense = value as Expense
                    Source.updateSourceAmountById(expense.sourceId, expense.amount)
                    Budget.updateBudgetAmountSpent(expense.currency!!, expense.date, -expense.amount, null, null)

                    MainActivity().db.collection("Expenses")
                        .document(id).delete()
                }
            })
        }

        fun deleteExpenseByCategoryId(id: String) {
            MainActivity().db.collection("Expenses")
                .whereEqualTo("uid", AuthActivity().auth.uid.toString())
                .whereEqualTo("categoryId", id)
                .get().addOnSuccessListener { query ->
                    query.documents.forEach { document ->
                        if (document != null) {
                            val expense = document.toObject(Expense::class.java)!!
                            Source.updateSourceAmountById(expense.sourceId, -expense.amount)
                            Budget.updateBudgetAmountSpent(expense.currency!!, expense.date, expense.amount, null, null)
                            document.reference.delete()
                        }
                    }
                }
        }

        fun deleteExpenseBySourceId(id: String) {
            MainActivity().db.collection("Expenses")
                .whereEqualTo("uid", AuthActivity().auth.uid.toString())
                .whereEqualTo("sourceId", id)
                .get().addOnSuccessListener { query ->
                    query.documents.forEach { document ->
                        if (document != null) {
                            val expense = document.toObject(Expense::class.java)!!
                            Budget.updateBudgetAmountSpent(expense.currency!!, expense.date, expense.amount, null, null)
                            document.reference.delete()
                        }
                    }
                }
        }

        fun getExpenseById(id: String, callback: Callback) {
            MainActivity().db.collection("Expenses")
                .document(id).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val expense = document.toObject(Expense::class.java)!!
                        callback.onCallback(expense)
                    }
                }
        }

        fun getExpenseBySourceId(id: String, callback: Callback) {
            MainActivity().db.collection("Expenses")
                .whereEqualTo("uid", AuthActivity().auth.uid.toString())
                .whereEqualTo("sourceId", id)
                .get().addOnSuccessListener { query ->
                    query.documents.forEach { document ->
                        if (document != null) {
                            val expense = document.toObject(Expense::class.java)!!
                            callback.onCallback(expense)
                        }
                    }
                }
        }

        fun getExpense(currency: Currency?, sourceId: String?, dateStart: Date, dateEnd: Date, callback: Callback) {
            var query = MainActivity().db.collection("Expenses")
                .whereEqualTo("uid", AuthActivity().auth.uid.toString())
            if (currency != null) {
                query = query
                    .whereEqualTo("currency", currency)
            }
            if (sourceId != null) {
                query = query
                    .whereEqualTo("sourceId", sourceId)
            }
            query
                .whereGreaterThanOrEqualTo("date", dateStart)
                .whereLessThan("date", dateEnd)
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    run {
                        if (snapshot != null) {
                            val expenses = ArrayList<Expense>()
                            val documents = snapshot.documents
                            documents.forEach {
                                val expense = it.toObject(Expense::class.java)
                                if (expense != null) {
                                    expense.id = it.id
                                    expenses.add(expense)
                                }
                            }
                            callback.onCallback(expenses)
                        }
                    }
                }
        }

        fun updateExpenseCurrencyBySourceId(id: String, currency: Currency) {
            MainActivity().db.collection("Expenses")
                .whereEqualTo("uid", AuthActivity().auth.uid.toString())
                .whereEqualTo("sourceId", id)
                .get().addOnSuccessListener { query ->
                    query.documents.forEach { document ->
                        document.reference.update("currency", currency)
                    }
                }
        }

        fun getAllExpense(callback: Callback) {
            MainActivity().db.collection("Expenses")
                .whereEqualTo("uid", AuthActivity().auth.uid.toString())
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    run {
                        if (snapshot != null) {
                            val expenses = ArrayList<Expense>()
                            val documents = snapshot.documents
                            documents.forEach {
                                val expense = it.toObject(Expense::class.java)
                                if (expense != null) {
                                    expense.id = it.id
                                    expenses.add(expense)
                                }
                            }
                            callback.onCallback(expenses)
                        }
                    }
                }
        }
    }

}