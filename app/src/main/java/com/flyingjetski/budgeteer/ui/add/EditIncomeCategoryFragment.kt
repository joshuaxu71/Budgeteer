package com.flyingjetski.budgeteer.ui.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.flyingjetski.budgeteer.Adapters
import com.flyingjetski.budgeteer.Callback
import com.flyingjetski.budgeteer.Common
import com.flyingjetski.budgeteer.R
import com.flyingjetski.budgeteer.databinding.FragmentEditIncomeCategoryBinding
import com.flyingjetski.budgeteer.models.Category
import java.lang.reflect.Field

class EditIncomeCategoryFragment : Fragment() {

    private lateinit var binding: FragmentEditIncomeCategoryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_income_category, container, false)
        setupUI()
        return binding.root
    }

    private fun setupUI() {
        // Instantiation
        val activity = requireActivity()
        val incomeCategoryId = arguments?.getString("Id")

        // Populate View
        binding.categoryGridView.adapter =
            Adapters.IconGridAdapter(this.requireContext(), Common.incomeCategoryIcons)


        // Set Listeners
        binding.categoryGridView.setOnItemClickListener{ adapterView: AdapterView<*>, view1: View, i: Int, l: Long ->
            (adapterView.adapter as Adapters.IconGridAdapter)
                .selectIcon(i)
        }

        binding.editButton.setOnClickListener{
            Category.updateCategoryById(
                incomeCategoryId.toString(),
                (binding.categoryGridView.adapter as Adapters.IconGridAdapter)
                    .selectedIconResource,
                binding.labelEditText.text.toString(),
            )
            requireActivity().onBackPressed()
        }

        binding.deleteButton.setOnClickListener{
            Category.deleteCategoryById(incomeCategoryId.toString())
            requireActivity().onBackPressed()
        }

        // Actions
        Category.getCategoryById(incomeCategoryId.toString(), object: Callback {
            override fun onCallback(value: Any) {
                val category = value as Category
                binding.categoryGridView.deferNotifyDataSetChanged()
                val position = (binding.categoryGridView.adapter as Adapters.IconGridAdapter)
                    .getPositionOfResource(category.icon)
                binding.categoryGridView.performItemClick(
                    binding.categoryGridView,
                    position,
                    binding.categoryGridView.adapter.getItemId(position),
                )
                binding.labelEditText.setText(category.label)
            }
            })
    }

}