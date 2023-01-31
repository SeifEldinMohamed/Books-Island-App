package com.seif.booksislandapp.presentation.home.categories.book_categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.seif.booksislandapp.R
import com.seif.booksislandapp.databinding.FragmentBookCategoriesBinding
import com.seif.booksislandapp.domain.model.BookCategory
import com.seif.booksislandapp.presentation.home.categories.ItemCategoryViewModel
import com.seif.booksislandapp.presentation.home.categories.book_categories.adapter.BookCategoriesAdapter
import com.seif.booksislandapp.presentation.home.categories.book_categories.adapter.OnCategoryItemClick

class BookCategoriesFragment : Fragment(), OnCategoryItemClick<BookCategory> {
    lateinit var binding: FragmentBookCategoriesBinding
    private val bookCategoriesAdapter: BookCategoriesAdapter by lazy { BookCategoriesAdapter() }
    private val itemCategoryViewModel: ItemCategoryViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentBookCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
        bookCategoriesAdapter.onCategoryItemClick = this
        binding.rvBookCategories.adapter = bookCategoriesAdapter
        bookCategoriesAdapter.submitList(createBookCategoriesList())
    }

    private fun createBookCategoriesList(): ArrayList<BookCategory> {
        return arrayListOf(
            BookCategory(name = "Literature", R.color.literature_color),
            BookCategory(name = "Historical", R.color.historical_color),
            BookCategory(name = "Religious", R.color.religious_color),
            BookCategory(name = "Scientific", R.color.scientific_color),
            BookCategory(name = "Political", R.color.political_color),
            BookCategory(name = "Money and Business", R.color.money_business_color),
            BookCategory(name = "Self Development", R.color.self_development_color),

            BookCategory(name = "Literature", R.color.literature_color),
            BookCategory(name = "Literature", R.color.literature_color),
            BookCategory(name = "Literature", R.color.literature_color),
            BookCategory(name = "Literature", R.color.literature_color),
            BookCategory(name = "Literature", R.color.literature_color),
            BookCategory(name = "Literature", R.color.literature_color),
            BookCategory(name = "Literature", R.color.literature_color),
            BookCategory(name = "Literature", R.color.literature_color),
            BookCategory(name = "Literature", R.color.literature_color),
            BookCategory(name = "Literature", R.color.literature_color),
            BookCategory(name = "Literature", R.color.literature_color),
            BookCategory(name = "Literature", R.color.literature_color),
            BookCategory(name = "Literature", R.color.literature_color),
            BookCategory(name = "Literature", R.color.literature_color),
            BookCategory(name = "Literature", R.color.literature_color)
        )
    }

    override fun onCategoryItemClick(bookCategory: BookCategory) {
        // val bundle = Bundle()
        // bundle.putString("category_name", bookCategory.name)
        // findNavController().navigate(R)
        itemCategoryViewModel.selectItem(bookCategory.name)
        findNavController().navigateUp()
    }
}