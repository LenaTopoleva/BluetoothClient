package com.lenatopoleva.bluetoothclient.ui.fragment

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.Intent.createChooser
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import com.lenatopoleva.bluetoothclient.App
import com.lenatopoleva.bluetoothclient.R
import com.lenatopoleva.bluetoothclient.databinding.ViewerFragmentBinding
import com.lenatopoleva.bluetoothclient.mvp.model.entity.Device
import com.lenatopoleva.bluetoothclient.mvp.presenter.ViewerPresenter
import com.lenatopoleva.bluetoothclient.mvp.view.ViewerView
import com.lenatopoleva.bluetoothclient.ui.BackButtonListener
import com.lenatopoleva.bluetoothclient.util.*
import com.orhanobut.logger.Logger
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter
import java.io.BufferedReader
import java.io.FileDescriptor
import java.io.InputStreamReader
import java.lang.UnsupportedOperationException
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

    private val filePickerActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            println("***VIEW FRAGMENT onActivityResult FILE PICKER***")
            Logger.d("ViewerFragment file picker activity result; resultCode: ${result.resultCode}, ok is -1")
            val rootUri = result.data?.data
            println("rootUri = $rootUri")
            Logger.d("ViewerFragment file picker activity result; got rootUri: $rootUri")
            rootUri?.let {
                presenter.rootPackageUri = rootUri.toString()
                val takeFlags: Int = result.data!!.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                activity?.contentResolver?.takePersistableUriPermission(rootUri, takeFlags)
                saveUriToSharedPreferences(it)
            }
        } else Logger.e("ViewerFragment file picker activity result; resultCode: ${result.resultCode}, ok is -1")
    }

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
                R.id.item_choose_config_file -> {
                    Logger.d("Choose file menu item clicked")
                    presenter.chooseFileButtonClicked()
                    true
                }
                else -> false
            }
        }
        binding.fabReconnect.setOnClickListener { presenter.fabReconnectClicked() }
        return view
    }

    override fun openFilePicker() {
        val intent = Intent()
            .setAction(Intent.ACTION_OPEN_DOCUMENT_TREE)
            .addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            .addFlags( Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            .addFlags( Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)

        val chooserIntent =  createChooser(intent, resources.getString(R.string.directory_chooser))

        filePickerActivityResultLauncher.launch(chooserIntent)
        Logger.d("ViewerFragment openFilePicker; action is Intent.ACTION_OPEN_DOCUMENT_TREE")
    }

    override fun onStart() {
        Logger.d("ViewerFragment onStart")
        println("***VIEW FRAGMENT OnStart***")
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        Logger.d("ViewerFragment onResume")
        println("***VIEW FRAGMENT onResume***")
        val sharedPreferences = activity?.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE)

        if(presenter.rootPackageUri == null || presenter.rootPackageUri == ""){
            presenter.rootPackageUri = sharedPreferences?.getString(ROOT_PACKAGE_URI, null)?: ""
            presenter.picturesObjectsPackageName = sharedPreferences?.getString(OBJECTS_PACKAGE, null)?: ""
            presenter.picturesActionsPackageName = sharedPreferences?.getString(ACTIONS_PACKAGE, null)?: ""
            presenter.picturesOtherPackageName = sharedPreferences?.getString(OTHER_PACKAGE, null)?: ""
            presenter.soundsPackageName = sharedPreferences?.getString(SOUNDS_PACKAGE, null)?: ""
            presenter.toneSoundFileName = sharedPreferences?.getString(TONE_SOUND_FILE_NAME, null)?: ""
        }

        val deviceAddress = sharedPreferences?.getString(DEVICE_ADDRESS, null)
        val deviceName = sharedPreferences?.getString(DEVICE_NAME, null)

        presenter.onResume(Device(deviceName ?: "", deviceAddress ?: ""))
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

    override fun showImage(imageName: String, subtype: String) {
        when(subtype){
            "object" -> {
                val imageUri = getDocumentUriWithRootUri("/${presenter.picturesObjectsPackageName}/${imageName}",
                        presenter.rootPackageUri)
                imageUri?.let{ showImageWithUri(it) }
            }
            "action" -> {
                val imageUri = getDocumentUriWithRootUri("/${presenter.picturesActionsPackageName}/${imageName}",
                        presenter.rootPackageUri)
                imageUri?.let{ showImageWithUri(it) }
            }
            "other" -> {
                val imageUri = getDocumentUriWithRootUri("/${presenter.picturesOtherPackageName}/${imageName}",
                        presenter.rootPackageUri)
                imageUri?.let{ showImageWithUri(imageUri) }
            }
        }
    }

    private fun getDocumentUriWithRootUri(docRelativePath: String, rootUriString: String?): Uri?{
        val rootUri = Uri.parse(rootUriString)
        val id = DocumentsContract.getTreeDocumentId(rootUri)
        val docFileId = "$id$docRelativePath"

        val docUri = DocumentsContract.buildDocumentUriUsingTree(rootUri, docFileId)
        val docFile = DocumentFile.fromSingleUri(requireContext(), docUri)

        return if (docFile != null && docFile.exists()) {
            val trueDocUri = docFile.uri
            trueDocUri
        } else null
    }

    private fun showImageWithUri(imageUri: Uri){
        Logger.d("ViewerFragment showImageWithUri; preparing to show image")
        val contentResolver = activity?.contentResolver
        val parcelFileDescriptor: ParcelFileDescriptor? =
                contentResolver?.openFileDescriptor(imageUri, "r")
        val fileDescriptor: FileDescriptor? = parcelFileDescriptor?.fileDescriptor
        val image: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor?.close()
        Logger.d("ViewerFragment showImageWithUri; image shows now")
        binding.ivViewer.setImageBitmap(image)
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

    override fun hideFab() {
        binding.fabReconnect.visibility = GONE
    }

    override fun showFab() {
        binding.fabReconnect.visibility = VISIBLE
    }

    override fun startAudio(audioName: String) {
        Logger.d("ViewerFragment startAudio; preparing to start audio")
        val contentResolver = activity?.contentResolver
        val soundUri = getDocumentUriWithRootUri("/${presenter.soundsPackageName}/$audioName", presenter.rootPackageUri)
        if (soundUri != null) {
            println("soundUri = $soundUri")
            val parcelFileDescriptor: ParcelFileDescriptor? =
                    contentResolver?.openFileDescriptor(soundUri, "r")
            val fileDescriptor: FileDescriptor? = parcelFileDescriptor?.fileDescriptor
            mediaPlayer.reset()
            mediaPlayer.setDataSource(fileDescriptor)
            mediaPlayer.setOnCompletionListener {}
            mediaPlayer.prepare()
            Logger.d("ViewerFragment startAudio; audio starts now")
            mediaPlayer.start()
        }
    }

    override fun startToneAudioIfEnable(tone: Boolean) {
        Logger.d("ViewerFragment startToneAudioIfEnable; tone is enable: $tone")
        if(tone && presenter.toneSoundFileName != null)
            startAudio(presenter.toneSoundFileName!!)
    }

    override fun openChooseFileAlertDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.directory_is_not_set)
            .setMessage(R.string.choose_directory)
            .setIcon(R.drawable.ic_baseline_folder_open_24)
            .setCancelable(false)
            .setPositiveButton(R.string.ok) { _: DialogInterface, _: Int ->
                presenter.chooseFileButtonClicked()
            }
        builder.create().show()
    }

    override fun showDataTransmittingExceptionToast(exceptionMessage: String) {
        showToast(resources.getString(R.string.data_transmitting_exception) + exceptionMessage)
    }

    override fun showUnableToConnectDeviceToast(deviceNameAndError: String) {
        showToast(resources.getString(R.string.unable_to_connect_device)
                + deviceNameAndError)
    }

    override fun showChooseDeviceToast() {
        showToast(resources.getString(R.string.choose_device))
    }

    override fun showConnectedWithMessage(deviceName: String) {
        val newText = resources.getString(R.string.connected_with) + deviceName
        binding.tvConnectionStatus.text = newText
    }

    override fun showDeviceIsNotConnectedMessage() {
        val newText = resources.getString(R.string.device_is_not_connected)
        binding.tvConnectionStatus.text = newText
    }

    override fun showDeviceConnectedToast() {
        showToast( resources.getString(R.string.device_connected))
    }

    private fun saveUriToSharedPreferences(rootUri: Uri) {
        Logger.d("ViewerFragment saveUriToSharedPreferences")
        val rootTree = DocumentFile.fromTreeUri(requireContext(), rootUri)
        val docFile: DocumentFile?
        try {
            docFile = rootTree?.findFile(CONFIGURATION_FILE_NAME)
        } catch (ex: UnsupportedOperationException) {
            Logger.e("ViewerFragment saveUriToSharedPreferences; can't find config file: ${ex.message} ")
            showToast(resources.getString(R.string.cant_find_config_file) + " ${ex.message}")
            return
        }
        val configFileUri = docFile?.uri
        println("configFileUri: $configFileUri")
        Logger.d("ViewerFragment saveUriToSharedPreferences; configFilePath: $configFileUri")

        val packageNameArray: MutableList<String> = mutableListOf()
        if (configFileUri != null) {
            activity?.contentResolver?.openInputStream(configFileUri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String? = reader.readLine()
                    while (line != null) {
                        packageNameArray.add(line)
                        line = reader.readLine()
                    }
                }
            }
        } else {
            Logger.d("ViewerFragment saveUriToSharedPreferences; Wrong directory, config file not found, leave method")
            showToast(resources.getString(R.string.wrong_directory))
            return
        }
        Logger.d("ViewerFragment saveUriToSharedPreferences; Config file data: $packageNameArray")
        println("packageNameArray = $packageNameArray")
        for (packageName in packageNameArray) {
            when (packageName.split("-").first()) {
                ACTIONS -> {
                    presenter.picturesActionsPackageName = packageName.split("-").last()
                    Logger.d("picturesActionsPath = ${presenter.picturesActionsPackageName}")
                    println("picturesActionsPath = ${presenter.picturesActionsPackageName}")
                }
                OBJECTS -> presenter.picturesObjectsPackageName = packageName.split("-").last()
                OTHER -> presenter.picturesOtherPackageName = packageName.split("-").last()
                SOUNDS -> presenter.soundsPackageName = packageName.split("-").last()
                TONE -> presenter.toneSoundFileName = packageName.split("-").last()
                else -> showToast( resources.getString(R.string.wrong_config_file))
            }
        }
        val sharedPreferences =
            activity?.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor? = sharedPreferences?.edit()
        if (editor != null) {
            editor.putString(ROOT_PACKAGE_URI, presenter.rootPackageUri)
            editor.putString(ACTIONS_PACKAGE, presenter.picturesActionsPackageName)
            editor.putString(OBJECTS_PACKAGE, presenter.picturesObjectsPackageName)
            editor.putString(OTHER_PACKAGE, presenter.picturesOtherPackageName)
            editor.putString(SOUNDS_PACKAGE, presenter.soundsPackageName)
            editor.putString(TONE_SOUND_FILE_NAME, presenter.toneSoundFileName)
            editor.apply()
        }
    }

    private fun showToast(message: String){
        Toast.makeText(requireContext(), message,
            Toast.LENGTH_LONG).show()
    }

    override fun backPressed() = presenter.backClick()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}