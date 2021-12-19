package com.ar7lab.cherieapp.ui.helpandsupport

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ar7lab.cherieapp.*
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.base.fragment.BaseFragment
import com.ar7lab.cherieapp.base.navigation.NavManager
import com.ar7lab.cherieapp.databinding.FragmentHelpAndSupportBinding
import com.ar7lab.cherieapp.enums.FaqsTypeEnum
import com.ar7lab.cherieapp.model.FAQ
import com.ar7lab.cherieapp.utils.textChanges
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * Fragment for help and support screen
 * Add @AndroidEntryPoint for Hilt component
 */
@AndroidEntryPoint
class HelpAndSupportFragment : BaseFragment(R.layout.fragment_help_and_support) {

    /**
     * define view binding from fragment_help_and_support
     */
    private val binding: FragmentHelpAndSupportBinding by viewBinding()

    private val viewModel: HelpAndSupportViewModel by viewModels()

    /**
     * navigation manager
     */
    @Inject
    lateinit var navManager: NavManager

    private var _queries = ArrayList<FAQ>()

    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<HelpAndSupportViewModel.ViewState> {
        binding.isRefreshing = it.isRefreshing
        if (it.isError) {
            showError(it.message)
        }
        it.FAQs?.let { faqs ->
            _queries = faqs
        }
        binding.rvItems.requestModelBuild()
    }


    override fun isNeedWindowLightStatusBar(): Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        observe(viewModel.stateLiveData, stateObserver)
        viewModel.loadData()

        createEpoxyRecyclerView()
        binding.ivBack.setOnDebouncedClickListener {
            findNavController().popBackStack()
        }

        /**
         * Perform search with debounce
         */
        /*binding.etSearch.textChanges()
            .filterNot { it.isNullOrBlank() }
            .debounce(800L)
            .map { it.toString() }
            .onEach {
                viewModel.search(it)
            }
            .launchIn(lifecycleScope)*/
    }

    /**
     * Create epoxy recyclerview
     */
    private fun createEpoxyRecyclerView() {
        binding.rvItems.withModels {

            itemFaqs {
                id("faqs title")
            }

            //if non of the faq is selected then only titles of faqs will be showing on the screen
            //if the first faq is selected then the first faq will be expand with the information
            //if the second faq is selected then the second faq will be expand with the information
            //if the third faq is selected then the third faq will be expand with the information
            //if the fourth faq is selected then the fourth faq will be expand with the information
            //if the fifth faq is selected then the fifth faq will be expand with the information
            if(viewModel.faqSelected.get() == FaqsTypeEnum.FIRST){
                itemWhatIsCherieFaqsFull {
                    id("what_is_cherie_faqs_full")
                    viewModel(viewModel)
                }

                itemHowToAttachWalletFaqs {
                    id("how_to_attach_wallet")
                    viewModel(viewModel)
                }
                itemAppIssueFaqs {
                    id("app_issue")
                }

                itemArtNotUploadingFaqs {
                    id("art_not_uploading")
                }

                itemAccountAndProfileFaqs {
                    id("acc_and_profile")
                }

            }else if(viewModel.faqSelected.get() == FaqsTypeEnum.SECOND){
                itemWhatIsCherieFaqs {
                    id("what_is_cherie_faqs")
                    viewModel(viewModel)
                }

                itemHowToAttachWalletFaqsFull {
                    id("how_to_attach_wallet_full")
                    viewModel(viewModel)
                }

                itemAppIssueFaqs {
                    id("app_issue")
                    viewModel(viewModel)
                }

                itemArtNotUploadingFaqs {
                    id("art_not_uploading")
                    viewModel(viewModel)
                }

                itemAccountAndProfileFaqs {
                    id("acc_and_profile")
                    viewModel(viewModel)
                }

            }else if(viewModel.faqSelected.get() == FaqsTypeEnum.THIRD){

                itemWhatIsCherieFaqs {
                    id("what_is_cherie_faqs")
                    viewModel(viewModel)
                }
                itemHowToAttachWalletFaqs {
                    id("how_to_attach_wallet")
                    viewModel(viewModel)
                }
                itemAppIssueFaqsFull {
                    id("app_issue_full")
                    viewModel(viewModel)
                }

                itemArtNotUploadingFaqs {
                    id("art_not_uploading")
                    viewModel(viewModel)
                }

                itemAccountAndProfileFaqs {
                    id("acc_and_profile")
                    viewModel(viewModel)
                }

            }else if(viewModel.faqSelected.get() == FaqsTypeEnum.FOURTH){

                itemWhatIsCherieFaqs {
                    id("what_is_cherie_faqs")
                    viewModel(viewModel)
                }
                itemHowToAttachWalletFaqs {
                    id("how_to_attach_wallet")
                    viewModel(viewModel)
                }
                itemAppIssueFaqs {
                    id("app_issue")
                    viewModel(viewModel)
                }

                itemArtNotUploadingFaqsFull {
                    id("art_not_uploading_full")
                    viewModel(viewModel)
                }

                itemAccountAndProfileFaqs {
                    id("acc_and_profile")
                    viewModel(viewModel)
                }

            }else if(viewModel.faqSelected.get() == FaqsTypeEnum.FIFTH){

                itemWhatIsCherieFaqs {
                    id("what_is_cherie_faqs")
                    viewModel(viewModel)
                }
                itemHowToAttachWalletFaqs {
                    id("how_to_attach_wallet")
                    viewModel(viewModel)
                }
                itemAppIssueFaqs {
                    id("app_issue")
                    viewModel(viewModel)
                }

                itemArtNotUploadingFaqs {
                    id("art_not_uploading")
                    viewModel(viewModel)
                }

                itemAccountAndProfileFaqsFull {
                    id("acc_and_profile_full")
                    viewModel(viewModel)
                }

            }else if(viewModel.faqSelected.get() == FaqsTypeEnum.NONE){
                itemWhatIsCherieFaqs {
                    id("what_is_cherie_faqs")
                    viewModel(viewModel)
                }
                itemHowToAttachWalletFaqs {
                    id("how_to_attach_wallet")
                    viewModel(viewModel)
                }
                itemAppIssueFaqs {
                    id("app_issue")
                    viewModel(viewModel)
                }

                itemArtNotUploadingFaqs {
                    id("art_not_uploading")
                    viewModel(viewModel)
                }

                itemAccountAndProfileFaqs {
                    id("acc_and_profile")
                    viewModel(viewModel)
                }
            }

            itemTextFaqs {
                id("text_one")
            }

            itemTextTwoFaqs {
                id("text_two")
            }

            itemRequestCallBackFaqs {
                id("request")
                listener(object : CallBackListener {
                    override fun onRequestCallbackClicked() {
                        val action =
                            HelpAndSupportFragmentDirections.actionHelpAndSupportToRequestCallback()
                        navManager.navigate(action)
                    }

                })
            }




            /*_queries.forEachIndexed { index, faq ->
                itemHelpAndSupportQueryItem {
                    id("query $index")
                    name(faq.question)
                    listener(object : QueryListener {
                        override fun onQueryClicked() {
                            val action =
                                HelpAndSupportFragmentDirections.actionHelpAndSupportToQueryDetails(
                                    faq
                                )
                            navManager.navigate(action)
                        }

                    })
                }
            }
            itemHelpAndSupportRequestCallback {
                id("request callback")
                listener(object : CallBackListener {
                    override fun onRequestCallbackClicked() {
                        val action =
                            HelpAndSupportFragmentDirections.actionHelpAndSupportToRequestCallback()
                        navManager.navigate(action)
                    }

                })
            }*/
        }
    }
}