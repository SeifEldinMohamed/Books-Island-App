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
import com.seif.booksislandapp.domain.model.book.BookCategory
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
            BookCategory(name = "Comics", R.color.comics_color),
            BookCategory(name = "Psychology", R.color.psychology_color),
            BookCategory(name = "Biography", R.color.biography_color),
            BookCategory(name = "Philosophy", R.color.philosophy_color),
            BookCategory(name = "Language", R.color.language_color),
            BookCategory(name = "Law", R.color.law_color),
            BookCategory(name = "Press and Media", R.color.press_and_media_color),
            BookCategory(name = "Medicine and Health", R.color.medicine_and_health_color),
            BookCategory(name = "Technology", R.color.technology_color),
            BookCategory(name = "Arts", R.color.arts_color),
            BookCategory(name = "Sports", R.color.sports_color),
            BookCategory(name = "Travel", R.color.travel_color),
            BookCategory(name = "References and Research", R.color.reference_and_research_color),
            BookCategory(name = "Family and Child", R.color.family_and_child_color),
            BookCategory(name = "Cooks", R.color.cook_color)

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