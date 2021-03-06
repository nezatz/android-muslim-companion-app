package io.github.hyuwah.muslimcompanionapp.presentation.ayahfetcher

import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import com.google.android.material.snackbar.Snackbar
import io.github.hyuwah.muslimcompanionapp.R
import io.github.hyuwah.muslimcompanionapp.data.remote.model.AyahResponse
import io.github.hyuwah.muslimcompanionapp.databinding.FragmentAyahFetcherBinding
import io.github.hyuwah.muslimcompanionapp.presentation.base.UIState
import io.github.hyuwah.muslimcompanionapp.presentation.base.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class AyahFetcherFragment : Fragment(R.layout.fragment_ayah_fetcher) {

    private val binding by viewBinding(FragmentAyahFetcherBinding::bind)
    private val viewModel by viewModel<AyahFetcherViewModel>()

    override fun onViewCreated(fview: View, savedInstanceState: Bundle?) {
        super.onViewCreated(fview, savedInstanceState)
        requireActivity().title = "Ayat Fetcher"
        initObserver()
        setupListener()
        setupMenu()
    }

    private fun initObserver() {
        viewModel.ayah.state.observe(viewLifecycleOwner) {
            when (it) {
                UIState.Loading -> showLoading()
                is UIState.Success -> {
                    showResult(it.data.data)
                }
                is UIState.Failure -> {
                    fetchFailed()
                }
            }
        }
        viewModel.isFavorite.observe(viewLifecycleOwner) {
            val icon = if (it) R.drawable.ic_baseline_favorite
            else R.drawable.ic_baseline_favorite_border
            binding.btnFavorite.setImageResource(icon)
        }
    }

    private fun setupListener() {
        with(binding) {
            btnFetchRandom.setOnClickListener { viewModel.fetchRandomAyah() }
            btnShare.setOnClickListener { shareAyah() }
            btnFavorite.setOnClickListener { viewModel.toggleFavorite() }
            tvAyahText.movementMethod = ScrollingMovementMethod()
        }
    }

    private fun setupMenu() {
        val popupMenu = PopupMenu(requireContext(), binding.btnMoreOptions).apply {
            inflate(R.menu.ayah_fetcher)
            setOnMenuItemClickListener {
                onMenuItemSelected(it)
            }
        }
        binding.btnMoreOptions.setOnClickListener {
            popupMenu.show()
        }
    }

    private fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_ed_default -> {
                viewModel.setAyahEdition("quran-simple")
                true
            }
            R.id.action_ed_en_transliteration -> {
                viewModel.setAyahEdition("en.transliteration")
                true
            }
            R.id.action_ed_id_indonesian -> {
                viewModel.setAyahEdition("id.indonesian")
                true
            }
            R.id.action_ed_id_muntakhab -> {
                viewModel.setAyahEdition("id.muntakhab")
                true
            }
            R.id.action_ed_en_ahmedali -> {
                viewModel.setAyahEdition("en.ahmedali")
                true
            }
            R.id.action_ed_en_asad -> {
                viewModel.setAyahEdition("en.asad")
                true
            }
            R.id.action_ed_en_hilali -> {
                viewModel.setAyahEdition("en.hilali")
                true
            }
            R.id.action_ed_en_pickthall -> {
                viewModel.setAyahEdition("en.pickthall")
                true
            }
            R.id.action_ed_en_sahih -> {
                viewModel.setAyahEdition("en.sahih")
                true
            }
            else -> false
        }
    }

    private fun fetchFailed() {
        Snackbar.make(requireView(), "Network error", Snackbar.LENGTH_SHORT).show()
        binding.tvAyahText.text = "Please check your connection & try again..."
    }

    private fun showLoading() {
        binding.tvAyahText.text = "Loading..."
    }

    private fun showResult(ayah: AyahResponse.Data) {
        with(binding) {
            tvAyahText.text = ayah.text
            tvSurahName.text = ayah.surah.englishName
            tvSurahNameTranslation.text = ayah.surah.englishNameTranslation
            tvAyahNumber.text = "[${ayah.surah.number}] : ${ayah.numberInSurah} of ${ayah
                    .surah.numberOfAyahs}"
            tvEdition.text = ayah.edition.englishName
        }
    }

    private fun shareAyah() {
        with(binding) {
            val message = """${tvAyahText.text}
                |-- ${tvSurahName.text} (${tvSurahNameTranslation.text}) ${tvAyahNumber.text}
                |
                |Convey, even if it is one verse
                |Shared via Muslim Companion Apps
            """.trimMargin()
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, message)
                type = "text/plain"
            }
            val chooser = Intent.createChooser(sendIntent, "Convey, even if it is one verse")
            if (sendIntent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(chooser)
            }
        }
    }
}