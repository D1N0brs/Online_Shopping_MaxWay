package uz.gita.online_shopping.presentation.screens.orders.checkout

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import uz.gita.online_shopping.R
import uz.gita.online_shopping.data.models.ProductWithCount
import uz.gita.online_shopping.databinding.ListItemOrderProductsBinding
import uz.gita.online_shopping.databinding.ScreenOrderProductsBinding
import uz.gita.online_shopping.presentation.viewmodels.OrderProductsViewModel
import uz.gita.online_shopping.presentation.viewmodels.impl.OrderProductsViewModelImpl
import uz.gita.online_shopping.utils.Basket
import uz.gita.online_shopping.utils.extensions.*

// Created by Jamshid Isoqov an 10/10/2022
@AndroidEntryPoint
class OrderProductsScreen : Fragment(R.layout.screen_order_products) {

    private val viewModel: OrderProductsViewModel by viewModels<OrderProductsViewModelImpl>()

    private val viewBinding: ScreenOrderProductsBinding by viewBinding()

    private lateinit var address: LatLng

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel.loadingFlow.onEach {
            if (it) showProgress() else hideProgress()
        }.launchIn(lifecycleScope)

        viewModel.messageFlow.onEach {
            showMessageDialog(it)
        }.launchIn(lifecycleScope)

        viewModel.errorFlow.onEach {
            showErrorDialog(it)
        }.launchIn(lifecycleScope)

        viewModel.successFlow.onEach {
            showSuccessDialog(it)
        }.launchIn(lifecycleScope)

        viewModel.deliveryAddress.onEach {
            viewBinding.tvMapDeliveryAddress.text = it
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.isDeliveryFlow.onEach {
            if (it) {
                viewBinding.imageCheckDelivery.visible()
                viewBinding.imageCheckOnTheWay.inVisible()
                viewBinding.containerMap.visible()
            } else {
                viewBinding.imageCheckDelivery.inVisible()
                viewBinding.imageCheckOnTheWay.visible()
                viewBinding.containerMap.gone()
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewBinding.apply {

            containerOnTheWay.setOnClickListener { viewModel.setDeliveryMethod(false) }

            containerDelivery.setOnClickListener { viewModel.setDeliveryMethod(true) }

            containerMap.setOnClickListener {
                findNavController().navigate(OrderProductsScreenDirections.actionOrderProductsScreenToOrderMapFragment())
            }
        }

        Basket.locationLiveData.observe(viewLifecycleOwner) {
            address = it.second
            viewBinding.tvMapDeliveryAddress.text = it.first
        }

        Basket.productsListLiveData.observe(viewLifecycleOwner) {
            loadOrders(it.filter { it.count > 0 })
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loadOrders(list: List<ProductWithCount>) {
        var summ = 0.0
        for (i in list) {
            summ += i.count * i.productData.price
            val orderBinding = ListItemOrderProductsBinding.inflate(layoutInflater)
            orderBinding.tvOrderProductNameWithCount.text = "${i.productData.name} ${i.count}x"
            orderBinding.tvOrderProductPrice.text = i.productData.price.getFinanceType()
            viewBinding.productsContainer.addView(orderBinding.root)
        }
        viewBinding.tvOrderAllSum.text = summ.getFinanceType()
    }

}