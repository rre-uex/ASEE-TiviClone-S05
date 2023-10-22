package es.unex.giiis.asee.tiviclone.view.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import es.unex.giiis.asee.tiviclone.R
import es.unex.giiis.asee.tiviclone.api.APICallback
import es.unex.giiis.asee.tiviclone.api.APIError
import es.unex.giiis.asee.tiviclone.api.getNetworkService
import es.unex.giiis.asee.tiviclone.data.api.TvShow
import es.unex.giiis.asee.tiviclone.data.model.Show
import es.unex.giiis.asee.tiviclone.data.toShow
import es.unex.giiis.asee.tiviclone.databinding.FragmentShowDetailBinding
import es.unex.giiis.asee.tiviclone.util.BACKGROUND

private const val TAG = "ShowDetailFragment"

/**
 * A simple [Fragment] subclass.
 * Use the [ShowDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ShowDetailFragment : Fragment() {

    private var _binding: FragmentShowDetailBinding? = null
    private val binding get() = _binding!!

    private val args: ShowDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShowDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val show = args.show
//        binding.tvShowTitle.text = show.title
//        binding.tvDescription.text = show.description
//        binding.tvYear.text = show.year
//        binding.swFav.isChecked = show.isFavorite
//        binding.coverImg.setImageResource(show.image)
//        binding.bannerImg.setImageResource(show.banner)
        Log.d(TAG, "Fetching ${show.title} details")
        fetchShowDetail(object : APICallback {
            override fun onCompleted(shows: List<TvShow?>) {
                Log.d(TAG, "API Response received")
                val showAPI = shows[0]?.toShow() ?: show
                activity?.runOnUiThread {
                    showBinding(showAPI)
                }
            }

            override fun onError(cause: Throwable) {
                Log.e(TAG, "API Response error")
                //binding.spinner.visibility = View.GONE
                Toast.makeText(context, "Connection error", Toast.LENGTH_SHORT).show()
            }
        }, show.id)
        Log.d(TAG, "Showing ${show.title} details")
    }

    private fun showBinding(show: Show) {
        binding.tvShowTitle.text = show.title
        binding.tvDescription.text = show.description
        binding.tvYear.text = show.year
        binding.swFav.isChecked = show.isFavorite

        Glide.with(this)
            .load(show.imagePath)
            .placeholder(R.drawable.placeholder)
            .into(binding.coverImg)

        Glide.with(this)
            .load(show.bannerPath)
            .placeholder(R.drawable.placeholder)
            .into(binding.bannerImg)

    }

    private fun fetchShowDetail(apiCallback: APICallback, showId: Int) {
        BACKGROUND.submit{
            try {
                // Make network request using a blocking call
                val result = getNetworkService().getShowDetail(showId).execute()

                if (result.isSuccessful){
                    val shows = listOf(result.body()!!.tvShow)
                    apiCallback.onCompleted(shows)
                }
                else
                    apiCallback.onError(APIError("API Response error ${result.errorBody()}", null))

            } catch (cause: Throwable) {
                // Update the UI on the main thread if something goes wrong
                // Bad modularization, we should not know about the UI thread here
                activity?.runOnUiThread {
                    Toast.makeText(context, "Connection error", Toast.LENGTH_SHORT).show()
                   // binding.spinner.visibility = View.GONE
                }
                Log.e(TAG, "APICallback connection error")
                // If anything throws an exception, inform the caller
                throw APIError("Unable to fetch data from API", cause)
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         * @return A new instance of fragment ShowDetailFragment.
         */
        @JvmStatic
        fun newInstance() =
            ShowDetailFragment()
    }
}