package es.unex.giiis.asee.tiviclone.view.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import es.unex.giiis.asee.tiviclone.databinding.FragmentDiscoverBinding
import androidx.recyclerview.widget.LinearLayoutManager
import es.unex.giiis.asee.tiviclone.api.APICallback
import es.unex.giiis.asee.tiviclone.api.APIError
import es.unex.giiis.asee.tiviclone.api.getNetworkService
import es.unex.giiis.asee.tiviclone.data.api.TvShow
import es.unex.giiis.asee.tiviclone.data.model.Show
import es.unex.giiis.asee.tiviclone.data.dummy.dummyShows
import es.unex.giiis.asee.tiviclone.data.toShow
import es.unex.giiis.asee.tiviclone.util.BACKGROUND
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DiscoverFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DiscoverFragment : Fragment() {

    private val TAG = "DiscoverFragment"

    private var _shows = listOf<Show>()

    private lateinit var listener: OnShowClickListener
    interface OnShowClickListener {
        fun onShowClick(show: Show)
    }

    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: DiscoverAdapter

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate DiscoverFragment")
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onAttach(context: android.content.Context) {
        super.onAttach(context)
        if (context is OnShowClickListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnShowClickListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()

        lifecycleScope.launch {
            if (_shows.isEmpty()) {
                binding.spinner.visibility = View.VISIBLE
                try {
                    _shows = fetchShows().filterNotNull()
                    adapter.updateData(_shows)
                } catch (error: APIError) {
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                } finally {
                    binding.spinner.visibility = View.GONE
                }
            }
        }
    }

    private suspend fun fetchShows(): List<Show> {
       return withContext(Dispatchers.IO){
            var apiShows = listOf<Show>()
            val result = try {
                getNetworkService().getShows(1).execute()
            } catch (cause: Throwable) {
                throw APIError("Unable to fetch data from API", cause)
            }
            if (result.isSuccessful)
                apiShows = result.body()!!.tvShows.map(TvShow::toShow)
            else
                throw APIError("API Response error ${result.errorBody()}", null)
            apiShows
        }
    }

    private fun fetchShows(apiCallback: APICallback) {
        BACKGROUND.submit{
            try {
                // Make network request using a blocking call
                val result = getNetworkService().getShows(1).execute()

                if (result.isSuccessful)
                    apiCallback.onCompleted(result.body()!!.tvShows)
                else
                    apiCallback.onError(APIError("API Response error ${result.errorBody()}", null))

            } catch (cause: Throwable) {
                // Update the UI on the main thread if something goes wrong
                // Bad modularization, we should not know about the UI thread here
                activity?.runOnUiThread {
                    Toast.makeText(context, "Connection error", Toast.LENGTH_SHORT).show()
                    binding.spinner.visibility = View.GONE
                }
                Log.e("DiscoverFragment", "APICallback connection error")
                // If anything throws an exception, inform the caller
                throw APIError("Unable to fetch data from API", cause)
            }
        }
    }

    private fun setUpRecyclerView() {
        adapter = DiscoverAdapter(
            shows = _shows,
            onClick = {
                listener.onShowClick(it)
            },
            onLongClick = {
                Toast.makeText(context, "long click on: "+it.title, Toast.LENGTH_SHORT).show()
            },
            context = this.context
        )
        with(binding) {
            rvShowList.layoutManager = LinearLayoutManager(context)
            rvShowList.adapter = adapter
        }
        android.util.Log.d("DiscoverFragment", "setUpRecyclerView")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // avoid memory leaks
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DiscoverFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DiscoverFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}