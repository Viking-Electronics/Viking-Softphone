package com.vikingelectronics.softphone.accounts.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import by.kirich1409.viewbindingdelegate.viewBinding
import com.etiennelenhart.eiffel.viewmodel.delegate.eiffelViewModel
import com.vikingelectronics.softphone.R
import com.vikingelectronics.softphone.accounts.add.AccountAddViewModel_Factory.create
import com.vikingelectronics.softphone.databinding.FragmentAccountAddBinding
import com.vikingelectronics.softphone.ui.RadioButtonWithLabel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@AndroidEntryPoint
class AccountAddFragment: Fragment(R.layout.fragment_account_add) {

     val viewModel: AccountAddViewModel by viewModels()
    private val binding: FragmentAccountAddBinding by viewBinding()

     val bindableState by lazy { AccountAddBindableState(viewModel.state) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return FragmentAccountAddBinding.inflate(inflater, container, false).apply {
            state = bindableState
            viewModel = this@AccountAddFragment.viewModel
            lifecycleOwner = this@AccountAddFragment.viewLifecycleOwner
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        setupUI()
    }

    private fun setupUI() = with(binding) {
//        usernameEditText.doOnTextChanged { text, _, _, _ -> viewModel.usernameUpdated(text)}
//        useridEditText.doOnTextChanged { text, _, _, _ -> viewModel.userIdUpdated(text)}
//        passwordEditText.doOnTextChanged { text, _, _, _ -> viewModel.passwordUpdated(text)}
//        domainEditText.doOnTextChanged { text, _, _, _ -> viewModel.domainUpdated(text)}
//        displayNameEditText.doOnTextChanged { text, _, _, _ -> viewModel.displayNameUpdated(text)}
//        transportUdp.setOnCheckedChangeListener { _, isChecked -> if (isChecked) viewModel.transportUpdated(AccountAddState.TransportType.UDP) }
//        transportTcp.setOnCheckedChangeListener { _, isChecked -> if (isChecked) viewModel.transportUpdated(AccountAddState.TransportType.TCP) }
//        transportTls.setOnCheckedChangeListener { _, isChecked -> if (isChecked) viewModel.transportUpdated(AccountAddState.TransportType.TLS) }
    }
}

@Composable
fun AccountAddScreen() {
    val accountAddViewModel: AccountAddViewModel = viewModel()
    AccountAddHoist(
//        accountAddViewModel.username,
//        accountAddViewModel::usernameUpdated
    )
}

@Composable
fun AccountAddHoist(
    username: String = "",
    onUsernameChanged: (String) -> Unit = {},
    userId: String = "",
    onUserIdChanged: (String) -> Unit = {},
    password: String = "",
    onPasswordChanged: (String) -> Unit = {},
    domain: String = "",
    onDomainChanged: (String) -> Unit = {},
    displayName: String = "",
    onDisplayNameChanged: (String) -> Unit = {},

) {
//    val userN: State<String> = username.collectAsState(initial = "")

    Column(
        modifier = Modifier.padding(16.dp).fillMaxWidth().fillMaxHeight()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            Text(
                text = stringResource(id = R.string.use_sip_account),
                modifier = Modifier.padding(0.dp, 16.dp)
            )
            Button(onClick = { /*TODO*/ }) {
                Image(painter = painterResource(id = R.drawable.qr_icon), contentDescription ="")
            }
        }

        TextField(
            value = username,
            onValueChange = onUsernameChanged,
            label = { Text(text = stringResource(id = R.string.username)) },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = userId,
            onValueChange = onUserIdChanged,
            label = { Text(text = stringResource(id = R.string.user_id_optional))},
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = password,
            onValueChange = onPasswordChanged,
            label = { Text(text = stringResource(id = R.string.password))},
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = domain,
            onValueChange = onDomainChanged,
            label = { Text(text = stringResource(id = R.string.domain))},
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = displayName,
            onValueChange = onDisplayNameChanged,
            label = { Text(text = stringResource(id = R.string.display_name_optional))},
            modifier = Modifier.fillMaxWidth()
        )

        Text(text = stringResource(id = R.string.transport))


        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            RadioButtonWithLabel(
                label = AccountAddState.TransportType.UDP().name
            )
            RadioButtonWithLabel(
                label = AccountAddState.TransportType.TLS().name
            )
            RadioButtonWithLabel(
                label = AccountAddState.TransportType.TCP().name
            )
        }

    }
}

@Preview
@Composable
fun AccountAddPreview() {
    MaterialTheme() {
        var username = remember { mutableStateOf("test") }
        AccountAddHoist(username.value, { username.value = it })
    }
}

