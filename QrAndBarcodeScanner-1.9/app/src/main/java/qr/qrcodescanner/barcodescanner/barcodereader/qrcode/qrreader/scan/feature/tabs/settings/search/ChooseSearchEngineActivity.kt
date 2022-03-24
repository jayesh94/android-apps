package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.settings.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.databinding.ActivityChooseSearchEngineBinding
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.faLogEvents
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.settings
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.applySystemWindowInsets
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.unsafeLazy
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.BaseActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.view.SettingsRadioButton
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.SearchEngine

class ChooseSearchEngineActivity : BaseActivity() {

    private lateinit var binding: ActivityChooseSearchEngineBinding
    
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ChooseSearchEngineActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val buttons by unsafeLazy {
        listOf(
            binding.buttonNone,
            binding.buttonAskEveryTime,
            binding.buttonBing,
            binding.buttonDuckDuckGo,
            binding.buttonGoogle,
            binding.buttonQwant,
            binding.buttonYahoo,
            binding.buttonYandex)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChooseSearchEngineBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //supportEdgeToEdge()
        initToolbar()
        showInitialValue()
        handleSettingsChanged()
    }

    override fun onResume() {
        super.onResume()
        faLogEvents.logScreenViewEvent("ChooseSearchEngineActivity", "ChooseSearchEngineActivity")
    }

    private fun supportEdgeToEdge() {
        binding.rootView.applySystemWindowInsets(applyTop = true, applyBottom = true)
    }

    private fun initToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun showInitialValue() {
        when (settings.searchEngine) {
            SearchEngine.NONE -> binding.buttonNone.isChecked = true
            SearchEngine.ASK_EVERY_TIME -> binding.buttonAskEveryTime.isChecked = true
            SearchEngine.BING -> binding.buttonBing.isChecked = true
            SearchEngine.DUCK_DUCK_GO -> binding.buttonDuckDuckGo.isChecked = true
            SearchEngine.GOOGLE -> binding.buttonGoogle.isChecked = true
            SearchEngine.QWANT -> binding.buttonQwant.isChecked = true
            SearchEngine.YAHOO -> binding.buttonYahoo.isChecked = true
            SearchEngine.YANDEX -> binding.buttonYandex.isChecked = true
        }
    }

    private fun handleSettingsChanged() {
        binding.buttonNone.setCheckedChangedListener(SearchEngine.NONE)
        binding.buttonAskEveryTime.setCheckedChangedListener(SearchEngine.ASK_EVERY_TIME)
        binding.buttonBing.setCheckedChangedListener(SearchEngine.BING)
        binding.buttonDuckDuckGo.setCheckedChangedListener(SearchEngine.DUCK_DUCK_GO)
        binding.buttonGoogle.setCheckedChangedListener(SearchEngine.GOOGLE)
        binding.buttonQwant.setCheckedChangedListener(SearchEngine.QWANT)
        binding.buttonYahoo.setCheckedChangedListener(SearchEngine.YAHOO)
        binding.buttonYandex.setCheckedChangedListener(SearchEngine.YANDEX)
    }

    private fun SettingsRadioButton.setCheckedChangedListener(searchEngine: SearchEngine) {
        setCheckedChangedListener { isChecked ->
            if (isChecked) {
                uncheckOtherButtons(this)
                settings.searchEngine = searchEngine
                logSetSearchEngineEvent(searchEngine)
            }
        }
    }

    private fun logSetSearchEngineEvent(searchEngine: SearchEngine) {
        val sEngine: String = searchEngine.name.lowercase()
        Log.e("ChooseSearchEngine", sEngine)
        faLogEvents.logCustomButtonClickEvent("settings_fragment_set_search_engine_$sEngine")
    }

    private fun uncheckOtherButtons(checkedButton: View) {
        buttons.forEach { button ->
            if (checkedButton !== button) {
                button.isChecked = false
            }
        }
    }
}