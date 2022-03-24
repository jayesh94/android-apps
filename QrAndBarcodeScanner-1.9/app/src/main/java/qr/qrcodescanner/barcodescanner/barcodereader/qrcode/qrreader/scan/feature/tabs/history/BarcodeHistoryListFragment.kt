package qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import androidx.recyclerview.widget.LinearLayoutManager
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.R
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.barcodeDatabase
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.provideViewModelFactory
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.orZero
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.packageManager
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.extension.showError
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.barcode.BarcodeActivity
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.dialog.DeleteConfirmationDialogFragment
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.common.dialog.EditBarcodeNameDialogFragment
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.model.Barcode
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase.BarcodeViewModel
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.usecase.ViewModelFactory
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_barcode_history_list.*
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.di.faLogEvents
import qr.qrcodescanner.barcodescanner.barcodereader.qrcode.qrreader.scan.feature.tabs.scan.ScanBarcodeFromCameraOrFileFragment

class BarcodeHistoryListFragment : Fragment(), BarcodeHistoryAdapter.Listener, EditBarcodeNameDialogFragment.Listener, DeleteConfirmationDialogFragment.Listener {

    companion object {
        private const val PAGE_SIZE = 20
        private const val TYPE_ALL = 0
        private const val TYPE_FAVORITES = 1
        private const val TYPE_KEY = "TYPE_KEY"

        fun newInstanceAll(): BarcodeHistoryListFragment {
            return BarcodeHistoryListFragment().apply {
                arguments = Bundle().apply {
                    putInt(TYPE_KEY, TYPE_ALL)
                }
            }
        }

        fun newInstanceFavorites(): BarcodeHistoryListFragment {
            return BarcodeHistoryListFragment().apply {
                arguments = Bundle().apply {
                    putInt(TYPE_KEY, TYPE_FAVORITES)
                }
            }
        }
    }

    interface Listener {
        fun startCreateBarcodeFragment()
    }

    private lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: BarcodeViewModel by viewModels { viewModelFactory }
    private val disposable = CompositeDisposable()
    private val scanHistoryAdapter = BarcodeHistoryAdapter(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModelFactory = provideViewModelFactory
        return inflater.inflate(R.layout.fragment_barcode_history_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        handleCreateCodeClick()
        loadHistory()
    }

    private fun handleCreateCodeClick() {
        empty_history_create_code_btn.setOnClickListener {
            val listener = requireActivity() as Listener
            listener.startCreateBarcodeFragment()
        }
    }

    override fun onBarcodeClicked(barcode: Barcode) {
        faLogEvents.logCustomButtonClickEvent("barcode_history_list_fragment_show_barcode")
        BarcodeActivity.start(requireActivity(), barcode)
    }

    override fun onBarcodeEditTitleClicked(barcode: Barcode) {
        faLogEvents.logCustomButtonClickEvent("barcode_history_list_fragment_menu_edit_barcode_name")
        showEditBarcodeNameDialog(barcode)
    }

    override fun onBarcodeDeleteClicked(barcode: Barcode) {
        faLogEvents.logCustomButtonClickEvent("barcode_history_list_fragment_menu_delete_barcode")
        showDeleteBarcodeConfirmationDialog(barcode)
    }

    override fun onBarcodeFavoriteClicked(barcode: Barcode) {
        faLogEvents.logCustomButtonClickEvent("barcode_history_list_fragment_toggle_favorite_barcode")
        toggleIsFavorite(barcode)
    }

    override fun onBarcodeShareClicked(barcode: Barcode) {
        faLogEvents.logCustomButtonClickEvent("barcode_history_list_fragment_menu_share_barcode")
        shareBarcodeAsText(barcode)
    }

    private fun shareBarcodeAsText(barcode: Barcode) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, barcode.text)
        }
        startActivityIfExists(intent)
    }

    private fun startActivityIfExists(intent: Intent) {
        intent.apply {
            flags = flags or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            showToast(R.string.activity_barcode_no_app)
        }
    }

    private fun toggleIsFavorite(barcode: Barcode) {
        val newBarcode = barcode.copy(isFavorite = barcode.isFavorite.not())

        barcodeDatabase.save(newBarcode)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    initRecyclerView()
                },
                ::showError
            )
            .addTo(disposable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable.clear()
    }

    private fun initRecyclerView() {
        recycler_view_history.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = scanHistoryAdapter
        }
    }

    private fun loadHistory() {
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(PAGE_SIZE)
            .build()

        val dataSource = when (arguments?.getInt(TYPE_KEY).orZero()) {
            TYPE_ALL -> barcodeDatabase.getAll()
            TYPE_FAVORITES -> barcodeDatabase.getFavorites()
            else -> return
        }

        RxPagedListBuilder(dataSource, config)
            .buildFlowable(BackpressureStrategy.LATEST)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                scanHistoryAdapter::submitList,
                ::showError
            )
            .addTo(disposable)

        when (arguments?.getInt(TYPE_KEY).orZero()) {
            TYPE_ALL -> observeAllCount()
            TYPE_FAVORITES -> observeFavoritesCount()
        }
    }

    private fun observeAllCount() {
        // Subscribe to the count of all history elements
        // In case of error, log the exception.
        disposable.add(viewModel.getAllCount()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    if(it !=0) {
                        empty_history_view.isVisible = false
                    }
                },
                ::showError
            )
        )
    }

    private fun observeFavoritesCount() {
        // Subscribe to the count of all favorite elements
        // In case of error, log the exception.
        disposable.add(viewModel.getFavoritesCount()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    if(it != 0) {
                        empty_history_view.isVisible = false
                    } else {
                        empty_box_text_view.text = resources.getString(R.string.barcode_favourite_list_empty)
                    }
                },
                ::showError
            )
        )
    }

    private fun showEditBarcodeNameDialog(barcode : Barcode?= null) {
        val dialog = EditBarcodeNameDialogFragment.newInstance(barcode?.name, barcode)
        dialog.show(childFragmentManager, "")
    }

    private fun showDeleteBarcodeConfirmationDialog(barcode : Barcode?= null) {
        val dialog = DeleteConfirmationDialogFragment.newInstance(R.string.dialog_delete_barcode_message, barcode)
        dialog.show(childFragmentManager, "")
    }

    override fun onNameConfirmed(name: String, barcode: Barcode?) {
        faLogEvents.logCustomButtonClickEvent("barcode_history_list_fragment_barcode_name_edited")
        updateBarcodeName(name, barcode)
    }

    private fun updateBarcodeName(name: String, barcode: Barcode?) {
        if (name.isBlank()) {
            return
        }

        val newBarcode = barcode?.copy(
            id = barcode.id,
            name = name
        )

        if (newBarcode != null) {
            barcodeDatabase.save(newBarcode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        initRecyclerView()
                    },
                    ::showError
                )
                .addTo(disposable)
        }
    }

    override fun onDeleteConfirmed(barcode: Barcode?) {
        if (barcode != null) {
            deleteBarcode(barcode)
        }
    }

    private fun deleteBarcode(barcode: Barcode) {

        faLogEvents.logCustomButtonClickEvent("barcode_history_list_fragment_single_barcode_deleted")

        barcodeDatabase.delete(barcode.id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    initRecyclerView()
                },
                ::showError
            )
            .addTo(disposable)
    }

    private fun showToast(stringId: Int) {
        Toast.makeText(activity, stringId, Toast.LENGTH_SHORT).show()
    }
}