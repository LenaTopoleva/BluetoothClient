package com.lenatopoleva.bluetoothclient.ui.fragment

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import com.lenatopoleva.bluetoothclient.App
import com.lenatopoleva.bluetoothclient.R
import com.lenatopoleva.bluetoothclient.databinding.ViewerFragmentBinding
import com.lenatopoleva.bluetoothclient.mvp.model.entity.Device
import com.lenatopoleva.bluetoothclient.mvp.presenter.ViewerPresenter
import com.lenatopoleva.bluetoothclient.mvp.view.ViewerView
import com.lenatopoleva.bluetoothclient.ui.BackButtonListener
import com.lenatopoleva.bluetoothclient.util.*
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter
import java.io.File
import javax.inject.Inject


class ViewerFragment: MvpAppCompatFragment(), ViewerView, BackButtonListener {

    companion object {
        fun newInstance() = ViewerFragment()
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
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.item_connection -> {
                    presenter.connectionMenuItemClicked()
                    true
                }
                R.id.item_choose_config_file ->{
                    presenter.chooseFileButtonClicked()
                    true
                }
                else -> false
            }
        }
        return view
    }

    override fun onStart() {
        println("***VIEW FRAGMENT OnStart***")
        super.onStart()
        val sharedPreferences = activity?.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE)

        if(presenter.mainPackagePath == null || presenter.mainPackagePath == ""){
            presenter.mainPackagePath = sharedPreferences?.getString(MAIN_PACKAGE, null)?: ""
            presenter.picturesObjectsPath = sharedPreferences?.getString(OBJECTS_PACKAGE, null)?: ""
            presenter.picturesActionsPath = sharedPreferences?.getString(ACTIONS_PACKAGE, null)?: ""
            presenter.picturesOtherPath = sharedPreferences?.getString(OTHER_PACKAGE, null)?: ""
            presenter.soundsPath = sharedPreferences?.getString(SOUNDS_PACKAGE, null)?: ""
            presenter.toneSoundFileName = sharedPreferences?.getString(TONE_SOUND_FILE_NAME, null)?: ""

            println("onStart, mainPackagePath: $presenter.mainPackagePath")
        }

        val deviceAddress = sharedPreferences?.getString(DEVICE_ADDRESS, null)
        val deviceName = sharedPreferences?.getString(DEVICE_NAME, null)

        presenter.onStart (Device(deviceName ?: "", deviceAddress ?: ""))
    }

    override fun onResume() {
        super.onResume()
        println("***VIEW FRAGMENT onResume***")
    }

    override fun hideAppBar() {
        binding.topAppBar.visibility = GONE
    }

    override fun hideActionBar() {
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        activity?.actionBar?.hide()
    }

    override fun showAppBar() {
       binding.topAppBar.visibility = VISIBLE
    }

    override fun showActionBar() {
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        activity?.actionBar?.show()
    }

    override fun showImage(imageName: String, subtype: String, tone: Boolean) {
        val imagePath: String
        when(subtype){
            "object" -> {
                imagePath = presenter.picturesObjectsPath + File.separator + imageName
                val imageFile = File(imagePath)
                if(tone && presenter.toneSoundFileName != null) startAudio(presenter.toneSoundFileName!!)
                binding.ivViewer.setImageURI(Uri.fromFile(imageFile))
            }
            "action" -> {
                imagePath = presenter.picturesActionsPath + File.separator + imageName
                val imageFile = File(imagePath)
                if(tone && presenter.toneSoundFileName != null) startAudio(presenter.toneSoundFileName!!)
                binding.ivViewer.setImageURI(Uri.fromFile(imageFile))
            }
            "other" -> {
                imagePath = presenter.picturesOtherPath + File.separator + imageName
                val imageFile = File(imagePath)
                if(tone && presenter.toneSoundFileName != null) startAudio(presenter.toneSoundFileName!!)
                binding.ivViewer.setImageURI(Uri.fromFile(imageFile))
            }
        }
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

    override fun startAudio(audioName: String) {
        val audioPath = presenter.soundsPath + File.separator + audioName
        val audioFile = File(audioPath)

        mediaPlayer.reset()
        mediaPlayer.setDataSource(requireContext(), Uri.fromFile(audioFile))
        mediaPlayer.setOnCompletionListener {
            // do smth
        }
        mediaPlayer.prepare()
        mediaPlayer.start()
    }

    override fun openChooseFileAlertDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.file_path_is_not_set)
            .setMessage(R.string.choose_config_file)
            .setIcon(R.drawable.ic_baseline_folder_open_24)
            .setCancelable(false)
            .setPositiveButton(R.string.ok) { _: DialogInterface, _: Int ->
                presenter.chooseFileButtonClicked()
            }
        builder.create().show()
    }

    override fun openFileChooser() {
        val intent = Intent()
            .setType("text/plain")
            .setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(Intent.createChooser(intent, resources.getString(R.string.file_picker)), REQUEST_OPEN_FILE_CHOOSER)
    }

    override fun showDataTransmittingExceptionMessage(exceptionMessage: String) {
        val newText = resources.getString(R.string.data_transmitting_exception) + exceptionMessage
        binding.tvConnectionStatus.text = newText
    }

    override fun showEndOfSessionMessage() {
        binding.tvConnectionStatus.text = resources.getString(R.string.end_of_session)
    }

    override fun showUnableToConnectDeviceMessage(deviceNameAndError: String) {
        val newText = resources.getString(R.string.unable_to_connect_device) + deviceNameAndError
        binding.tvConnectionStatus.text = newText
    }

    override fun showConnectedWithMessage(deviceName: String) {
        val newText = resources.getString(R.string.connected_with) + deviceName
        binding.tvConnectionStatus.text = newText
    }

    override fun showDeviceConnectedToast() {
        Toast.makeText(requireContext(), resources.getString(R.string.device_connected), Toast.LENGTH_LONG).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        println("***VIEW FRAGMENT onActivityResult***")
        when(requestCode) {
            REQUEST_OPEN_FILE_CHOOSER -> {
                if (resultCode == Activity.RESULT_OK) {
                    val fileUri = data?.data
                    println("fileUri = $fileUri")

                    fileUri?.let {
                        val path: String? = FilePathGetter().getPath(requireContext(), fileUri)
                        savePathToSharedPreferences(path)
                        println("file path: $path")
                    }
                }
            }
        }
    }

    private fun savePathToSharedPreferences(path: String?) {
        path?.let {
            val configFile = File(path)
            presenter.mainPackagePath =
                path.subSequence(0, path.length - configFile.name.length - 1).toString()
            val packageNameArray = configFile.readLines()
            println("packageNameArray = $packageNameArray")
            for (packageName in packageNameArray) {
                when (packageName.split("-").first()) {
                    ACTIONS -> presenter.picturesActionsPath =
                        presenter.mainPackagePath + File.separator + packageName.split("-")
                            .last()
                    OBJECTS -> presenter.picturesObjectsPath =
                        presenter.mainPackagePath + File.separator + packageName.split("-")
                            .last()
                    OTHER -> presenter.picturesOtherPath =
                        presenter.mainPackagePath + File.separator + packageName.split("-")
                            .last()
                    SOUNDS -> presenter.soundsPath =
                        presenter.mainPackagePath + File.separator + packageName.split("-")
                            .last()
                    TONE -> presenter.toneSoundFileName = packageName.split("-").last()
                    else -> Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.wrong_config_file),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            val sharedPreferences =
                activity?.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor? = sharedPreferences?.edit()
            if (editor != null) {
                editor.putString(MAIN_PACKAGE, presenter.mainPackagePath)
                editor.putString(ACTIONS_PACKAGE, presenter.picturesActionsPath)
                editor.putString(OBJECTS_PACKAGE, presenter.picturesObjectsPath)
                editor.putString(OTHER_PACKAGE, presenter.picturesOtherPath)
                editor.putString(SOUNDS_PACKAGE, presenter.soundsPath)
                editor.putString(TONE_SOUND_FILE_NAME, presenter.toneSoundFileName)
                editor.apply()
            }
        }
    }

    override fun backPressed() = presenter.backClick()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}