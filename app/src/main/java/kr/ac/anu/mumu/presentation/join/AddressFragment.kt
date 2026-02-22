package kr.ac.anu.mumu.presentation.join

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import kr.ac.anu.mumu.databinding.FragmentAddressBinding

class AddressFragment : Fragment() {
    private var _binding: FragmentAddressBinding? = null
    private val binding get() = _binding!!
    private val viewModel: JoinViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.checkAddressStep()

        viewModel.inputPostalCode.observe(viewLifecycleOwner) { postal ->
            binding.tvPostalCode.text = postal
        }

        viewModel.inputAddress.observe(viewLifecycleOwner) { address ->
            binding.tvAddress.text = address
        }

        binding.btnSearch.setOnClickListener {
            //TODO 카카오 API
            viewModel.inputPostalCode.value = "36729"
            viewModel.inputAddress.value = "경상북도 안동시 경동로 1375"
        }

        binding.etAddress.addTextChangedListener { text ->
            viewModel.inputDetailAddress.value = text.toString()

            viewModel.checkAddressStep()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}