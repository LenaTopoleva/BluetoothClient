package com.lenatopoleva.bluetoothclient.ui.fragment

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import com.lenatopoleva.bluetoothclient.App
import com.lenatopoleva.bluetoothclient.databinding.ViewerFragmentBinding
import com.lenatopoleva.bluetoothclient.mvp.model.entity.Device
import com.lenatopoleva.bluetoothclient.mvp.presenter.ViewerPresenter
import com.lenatopoleva.bluetoothclient.mvp.view.ViewerView
import com.lenatopoleva.bluetoothclient.ui.BackButtonListener
import com.lenatopoleva.bluetoothclient.ui.activity.MainActivity
import com.lenatopoleva.bluetoothclient.ui.activity.MainActivity.Companion.DEVICE_ADDRESS
import com.lenatopoleva.bluetoothclient.ui.activity.MainActivity.Companion.DEVICE_NAME
import com.lenatopoleva.bluetoothclient.ui.activity.MainActivity.Companion.MY_PREFS_NAME
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject


class ViewerFragment: MvpAppCompatFragment(), ViewerView, BackButtonListener {

    companion object {
        fun newInstance() = ViewerFragment()
        const val REQUEST_ENABLE_BLUETOOTH = 1
    }

    private var _binding: ViewerFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    val presenter by moxyPresenter { ViewerPresenter()
        .apply { App.instance.appComponent.inject(this) } }

    @Inject
    @JvmField
    var bluetoothAdapter: BluetoothAdapter? = null

    @Inject
    lateinit var mediaPlayer: MediaPlayer

    init {
        App.instance.appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ViewerFragmentBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onStart() {
        super.onStart()
        val sharedPreferences = activity?.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE)
        val deviceAddress = sharedPreferences?.getString(DEVICE_ADDRESS, null)
        val deviceName = sharedPreferences?.getString(DEVICE_NAME, null)
        presenter.onStart( Device(deviceName?: "", deviceAddress?: "") )
    }

    override fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun updateTextView(message: String) {
        binding.tvConnectionStatus.text = ""
        binding.tvConnectionStatus.text = message
    }

    override fun hideAppBar() {
        (activity as MainActivity).topAppBar.visibility = View.GONE
    }

    override fun hideActionBar() {
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        activity?.actionBar?.hide();
    }

    override fun showAppBar() {
        (activity as MainActivity).topAppBar.visibility = View.VISIBLE
    }

    override fun showActionBar() {
        activity?.window?.clearFlags( WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        activity?.actionBar?.show();
    }

    override fun showImage(image: String) {
        val decodedByte = Base64.decode(image, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
        binding.ivViewer.setImageBitmap(bitmap)
    }

    override fun hideTextView() {
        binding.tvConnectionStatus.visibility = GONE
    }

    override fun showImageView() {
        binding.ivViewer.visibility = VISIBLE
    }

    override fun hideImageView() {
        binding.ivViewer.visibility = GONE
    }

    override fun showTextView() {
        binding.tvConnectionStatus.visibility = VISIBLE
    }

    override fun startAudio(audioData: String?, audioCount: Int) {
        if(audioData != null){
            val decodedByte = Base64.decode(audioData, Base64.DEFAULT)
            val tempAudioFile = File.createTempFile("${audioCount}audio", "mp3")
            val fileOutputStream = FileOutputStream(tempAudioFile)
            fileOutputStream.write(decodedByte)
            fileOutputStream.close()

            val tempFilePath = tempAudioFile.path
            println("tempAudioFile path: ${tempAudioFile.path}")

            presenter.audioPlaying()
            mediaPlayer.reset()
            mediaPlayer.setDataSource(requireContext(), Uri.fromFile(tempAudioFile))
            mediaPlayer.setOnCompletionListener {
                presenter.audioCompleted()
                deleteTempAudioFile(tempFilePath)
            }
            mediaPlayer.prepare()
            mediaPlayer.start()
        }
    }

    private fun deleteTempAudioFile(path: String) {
        println("Try to delete tempFile; path: $path")
        val file = File(path)
        if(file.exists()){
            file.delete()
            println("File deleted; path: $path")
        }
        else println("Not file to delete; path: $path")
    }

    override fun backPressed() = presenter.backClick()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}