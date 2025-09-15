package com.example.aquaglow

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.aquaglow.databinding.FragmentAboutHelpBinding

class AboutHelpFragment : Fragment() {

    private var _binding: FragmentAboutHelpBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutHelpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.title = getString(R.string.about_help)
        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        // FAQs expand/collapse
        setupFaq(binding.faq1Header, binding.faq1Body)
        setupFaq(binding.faq2Header, binding.faq2Body)
        setupFaq(binding.faq3Header, binding.faq3Body)

        binding.buttonPrivacy.setOnClickListener {
            val url = getString(R.string.privacy_url)
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }

        binding.buttonEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.support_email)))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject))
            }
            startActivity(intent)
        }

        binding.buttonRate.setOnClickListener {
            val uri = Uri.parse(getString(R.string.play_store_url))
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }

    private fun setupFaq(header: View, body: View) {
        body.visibility = View.GONE
        header.setOnClickListener {
            body.visibility = if (body.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


