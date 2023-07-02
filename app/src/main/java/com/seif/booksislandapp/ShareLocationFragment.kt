package com.seif.booksislandapp

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.seif.booksislandapp.databinding.FragmentShareLocationBinding
import com.seif.booksislandapp.utils.showErrorSnackBar
import timber.log.Timber
import java.io.IOException
import java.util.Locale

class ShareLocationFragment : Fragment(), GoogleMap.OnMarkerDragListener {
    private var _binding: FragmentShareLocationBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<ShareLocationFragmentArgs>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShareLocationBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Places.initialize(requireContext(), "AIzaSyAbLlXzfA3nJVxhpyR3wwzAA-QoE3kpotw")

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        binding.ivBackShareLoacation.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private val callback = OnMapReadyCallback { googleMap ->
        googleMap.setOnMarkerDragListener(this)
        googleMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isMyLocationButtonEnabled = true
        }

        val startPlace = getStartLocation(args.governorate)
        addMarker(googleMap = googleMap, title = startPlace.first, latlng = startPlace.second)

        // Add click listener on marker
        googleMap.setOnMapClickListener { latLng ->
            googleMap.clear() // Clear existing markers

            val address = getAddressFromLocation(latLng)
            val marker = addMarker(googleMap = googleMap, title = address, latlng = latLng)
            listenShareLocation(marker)
        }

        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG
            )
        ).setCountries("EG")

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                //  Get info about the selected place.
                Timber.d("Place: ${place.name}, ${place.id}, ${place.address}, ${place.latLng}")
                place.latLng?.let { placeLatLng ->
                    place.address?.let { address ->
                        val marker =
                            addMarker(googleMap = googleMap, title = address, latlng = placeLatLng)
                        listenShareLocation(marker)
                    }
                }
            }

            override fun onError(status: Status) {
                Timber.d("error status $status")
                binding.root.showErrorSnackBar(status.statusMessage.toString())
            }
        })
    }

    private fun addMarker(googleMap: GoogleMap, title: String, latlng: LatLng): Marker {
        googleMap.clear() // remove any markers in map
        val addedMarker =
            googleMap.addMarker(MarkerOptions().position(latlng).title(title).draggable(true))
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15f))
        return addedMarker!!
    }

    private fun listenShareLocation(marker: Marker) {
        binding.btnShareLocation.setOnClickListener {
            Timber.d("shared marker: $marker")
            val address = getAddressFromLocation(marker.position)
            shareLocation(address, marker.position)
        }
    }

    private fun getAddressFromLocation(latLng: LatLng): String {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        var addressText = ""
        try {
            val addresses: List<Address>? =
                geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address: Address = addresses[0]
                val sb = StringBuilder()
                for (i in 0..address.maxAddressLineIndex) {
                    sb.append(address.getAddressLine(i))
                    if (i < address.maxAddressLineIndex)
                        sb.append(", ")
                }
                addressText = sb.toString()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return addressText
    }

    private fun getStartLocation(governorate: String): Pair<String, LatLng> {
        val cairoLocation = LatLng(30.04513239166969, 31.235086698980133)
        val gizaLocation = LatLng(30.014318218951818, 31.2088375082187)
        val cairo = "Cairo"
        val giza = "Giza"
        return when (governorate) {
            cairo -> Pair(cairo, cairoLocation)
            giza -> Pair(giza, gizaLocation)
            else -> Pair(cairo, cairoLocation)
        }
    }

    private fun shareLocation(address: String, latLng: LatLng) {
        Timber.d("shareLocation: address = $address")
        val uriString =
            "http://maps.google.com/maps?q=" + latLng.latitude + "," + latLng.longitude + "&iwloc=A"
        val uri = Uri.parse(uriString)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, uri.toString())
        }
        startActivity(Intent.createChooser(shareIntent, "Share Location"))
    }

    override fun onMarkerDragEnd(marker: Marker) {
        binding.btnShareLocation.setOnClickListener {
            val address = getAddressFromLocation(marker.position)
            shareLocation(address, marker.position)
        }
    }

    override fun onMarkerDrag(marker: Marker) {
    }

    override fun onMarkerDragStart(marker: Marker) {
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}