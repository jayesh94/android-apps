package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di

import android.app.Activity
import android.content.Context
import android.service.quicksettings.TileService
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialog
import androidx.fragment.app.Fragment
import androidx.paging.PagedListAdapter
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.App
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.updater.InAppUpdater
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.BottomTabsActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase.*


val App.settings
    get() = Settings.getInstance(applicationContext)

val App.faLogEvents
    get() = FALogEvents.getInstance(applicationContext)

val Activity.faLogEvents
    get() = FALogEvents.getInstance(applicationContext)

val Activity.settings
    get() = Settings.getInstance(applicationContext)

val Context.settings
    get() = Settings.getInstance(applicationContext)

val AppCompatActivity.barcodeParser
    get() = BarcodeParser

val AppCompatActivity.barcodeImageScanner
    get() = BarcodeImageScanner

val AppCompatActivity.barcodeImageGenerator
    get() = BarcodeImageGenerator

val AppCompatActivity.barcodeSaver
    get() = BarcodeSaver

val AppCompatActivity.barcodeImageSaver
    get() = BarcodeImageSaver

val AppCompatActivity.wifiConnector
    get() = WifiConnector

val AppCompatActivity.otpGenerator
    get() = OTPGenerator

val AppCompatActivity.barcodeDatabase
    get() = BarcodeDatabase.getInstance(this)

val AppCompatActivity.settings
    get() = Settings.getInstance(this)

val AppCompatActivity.contactHelper
    get() = ContactHelper

val AppCompatActivity.permissionsHelper
    get() = PermissionsHelper

val AppCompatActivity.rotationHelper
    get() = RotationHelper

val AppCompatActivity.faLogEvents
    get() = FALogEvents.getInstance(this)

val AppCompatActivity.inAppUpdater
    get() = InAppUpdater.getInstance(this as BottomTabsActivity)


val Fragment.scannerCameraHelper
    get() = ScannerCameraHelper

val Fragment.barcodeParser
    get() = BarcodeParser

val Fragment.barcodeDatabase
    get() = BarcodeDatabase.getInstance(requireContext())

val Fragment.provideViewModelFactory
    get() = ViewModelFactory(barcodeDatabase)

val Fragment.settings
    get() = Settings.getInstance(requireContext())

val Fragment.permissionsHelper
    get() = PermissionsHelper

val Fragment.faLogEvents
    get() = FALogEvents.getInstance(requireContext())

val AppCompatDialog.faLogEvents
    get() = FALogEvents.getInstance(context)

val FrameLayout.faLogEvents
    get() = FALogEvents.getInstance(context)

val Button.faLogEvents
    get() = FALogEvents.getInstance(context)

val TileService.faLogEvents
    get() = FALogEvents.getInstance(applicationContext)
