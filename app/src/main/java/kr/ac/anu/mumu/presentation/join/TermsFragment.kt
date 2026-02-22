package kr.ac.anu.mumu.presentation.join

import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import kr.ac.anu.mumu.databinding.FragmentTermsBinding

class TermsFragment : Fragment() {
    private var _binding: FragmentTermsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: JoinViewModel by activityViewModels()


}