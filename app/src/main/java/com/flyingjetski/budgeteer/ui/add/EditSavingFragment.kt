package com.flyingjetski.budgeteer.ui.add

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.flyingjetski.budgeteer.Adapters
import com.flyingjetski.budgeteer.Callback
import com.flyingjetski.budgeteer.Common
import com.flyingjetski.budgeteer.R
import com.flyingjetski.budgeteer.databinding.FragmentEditSavingBinding
import com.flyingjetski.budgeteer.models.*
import com.flyingjetski.budgeteer.models.enums.Currency
import java.lang.reflect.Field
import java.util.*
import kotlin.collections.ArrayList

class EditSavingFragment : Fragment() {

    private lateinit var binding: FragmentEditSavingBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_saving, container, false)
        setupUI()
        return binding.root
    }

    private fun setupUI() {
        val savingId = arguments?.getString("Id")

        val calendar = Calendar.getInstance()
        val dateListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                calendar[Calendar.YEAR] = year
                calendar[Calendar.MONTH] = monthOfYear
                calendar[Calendar.DAY_OF_MONTH] = dayOfMonth
                binding.deadlineDateEditText.setText(Common.dateToString(calendar.time))
            }

        // Populate View
        binding.categoryGridView.adapter =
            Adapters.IconGridAdapter(this.requireContext(), Common.sourceIcons)
        binding.currencySpinner.adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                Currency.values()
            )


        // Set Listeners
        binding.categoryGridView.setOnItemClickListener{ adapterView: AdapterView<*>, _, position: Int, _ ->
            (adapterView.adapter as Adapters.IconGridAdapter)
                .selectIcon(position)
        }

        binding.deadlineDateEditText.setOnClickListener{
            DatePickerDialog(
                this.requireContext(),
                dateListener,
                calendar[Calendar.YEAR],
                calendar[Calendar.MONTH],
                calendar[Calendar.DAY_OF_MONTH]
            ).show()
        }

        binding.editButton.setOnClickListener{
            Saving.updateSavingById(
                savingId.toString(),
                true,
                (binding.categoryGridView.adapter as Adapters.IconGridAdapter)
                    .selectedIconResource,
                binding.labelEditText.text.toString(),
                binding.currencySpinner.selectedItem as Currency,
                binding.targetEditText.text.toString().toDouble(),
                Common.stringToDate(binding.deadlineDateEditText.text.toString()),
                AutoSave(),
            )
            requireActivity().onBackPressed()
        }

        binding.deleteButton.setOnClickListener{
            Source.deleteSourceById(savingId.toString())
            requireActivity().onBackPressed()
        }

        // Actions
        Saving.getSavingById(savingId.toString(), object: Callback {
            override fun onCallback(value: Any) {
                val saving = value as Saving

                calendar.set(
                    saving.deadline.year + 1900,
                    saving.deadline.month,
                    saving.deadline.date,
                )

                binding.categoryGridView.deferNotifyDataSetChanged()
                val position = (binding.categoryGridView.adapter as Adapters.IconGridAdapter)
                    .getPositionOfResource(saving.icon)
                binding.categoryGridView.performItemClick(
                    binding.categoryGridView,
                    position,
                    binding.categoryGridView.adapter.getItemId(position),
                )
                binding.labelEditText.setText(saving.label)
                for (position in 0 until binding.currencySpinner.count) {
                    if ((binding.currencySpinner.getItemAtPosition(position) as Currency) == saving.currency) {
                        binding.currencySpinner.setSelection(position)
                        break
                    }
                }
                binding.targetEditText.setText(saving.target.toString())
                binding.deadlineDateEditText.setText(Common.dateToString(saving.deadline))
            }
            })
    }

}