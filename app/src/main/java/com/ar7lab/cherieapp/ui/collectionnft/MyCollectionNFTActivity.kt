package com.ar7lab.cherieapp.ui.collectionnft

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.ar7lab.cherieapp.base.activity.BaseActivity
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.databinding.ActivityMyCollectionNftBinding
import com.ar7lab.cherieapp.itemMyCollectionNft
import com.ar7lab.cherieapp.itemMyCollectionNftTotalPiece
import com.ar7lab.cherieapp.model.Art
import com.ar7lab.cherieapp.model.ArtData
import com.ar7lab.cherieapp.model.AssetsListItem
import com.ar7lab.cherieapp.model.MyCollectionDetails

import com.ar7lab.cherieapp.ui.sentemailresetpassword.SentEmailResetPasswordActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyCollectionNFTActivity : BaseActivity() {

    private val binding: ActivityMyCollectionNftBinding by viewBinding()

    private val viewModel: MyCollectionNFTViewModel by viewModels()

    override fun isNeedWindowLightStatusBar() = true

    private var collections = arrayListOf<AssetsListItem>()

    private var myCollectionDetails: MyCollectionDetails? = null

    private var artDatas: ArtData? = null

    private var pieces: Int? = 0

    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<MyCollectionNFTViewModel.ViewState> {
        binding.isLoading = it.isLoading
        if (it.isError) {
            showError(it.message)
        }
//        if (it.isSubmitted) {
//            startActivity(Intent(this, SentEmailResetPasswordActivity::class.java))
//            finish()
//        }
        it.myAssestData?.let { data ->
            collections = data
        }

        it.artData?.let { data ->
            artDatas = data
            binding.artData=artDatas
        }
        it.pieces?.let { piece ->
            pieces = piece
        }

        binding.rvItems.requestModelBuild()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        myCollectionDetails = intent.getSerializableExtra("collection") as MyCollectionDetails?
//        binding.viewModel = viewModel
        viewModel.init(myCollectionDetails)
        observe(viewModel.stateLiveData, stateObserver)

        binding.ivBack.setOnClickListener { finish() }
        createEpoxyView()
    }

    private fun createEpoxyView() {
        binding.rvItems.withModels {

            itemMyCollectionNftTotalPiece {
                id("total_piece")
                totalPieces(pieces)
            }

            collections.forEachIndexed { index, assestItem ->
                itemMyCollectionNft {
                    id("art $index")
                    listItem(assestItem)
                }
            }
        }

    }
}