package com.arcxp.commerce.apimanagers

import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.arcxp.commerce.apimanagers.LoginApiManager
import com.arcxp.commerce.apimanagers.LoginUiInteraction
import com.arcxp.commerce.ui.PasswordEditText
import com.arcxp.commerce.ui.UserNameEditText
import com.arcxp.commerce.viewmodels.LoginViewModel
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class LoginApiManagerTest {

    private lateinit var loginApiManager: LoginApiManager<Fragment>

    @MockK
    private lateinit var interaction: LoginUiInteraction

    @MockK
    private lateinit var loginViewModel: LoginViewModel

    @MockK
    private lateinit var userNameEt: UserNameEditText
    @MockK
    private lateinit var passwordEt: PasswordEditText

    @MockK
    private lateinit var button: Button

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        loginApiManager = LoginApiManager(Fragment(), interaction, loginViewModel)
        loginApiManager.setupLogin(userNameEt, passwordEt, loginBtn = button)
    }

    @Test
    fun `test making api call triggered for valid inputs when click button`() {
        every { userNameEt.text.toString() } returns "Timtest04"
        every { passwordEt.text.toString() } returns "Test1234@"
        every { userNameEt.validate() } returns null
        every { passwordEt.validate() } returns null

        val clickSlot = slot<View.OnClickListener>()
        verify(exactly = 1) {
            button.setOnClickListener(capture(clickSlot))
        }

        clickSlot.captured.onClick(button)
        verify(exactly = 1) {
            loginViewModel.makeLoginCall("Timtest04", "Test1234@", any())
        }
        verify(exactly = 0) {
            interaction.onError(any())
        }

    }

    @Test
    fun `test interaction error triggered for invalid inputs when click button`() {
        every { userNameEt.text.toString() } returns ""
        every { passwordEt.text.toString() } returns ""
        every { userNameEt.validate() } returns 0
        every { passwordEt.validate() } returns 0

        val clickSlot = slot<View.OnClickListener>()
        verify(exactly = 1) {
            button.setOnClickListener(capture(clickSlot))
        }

        clickSlot.captured.onClick(button)
        verify(exactly = 1) {
            interaction.onError(any())
        }
        verify(exactly = 0) {
            loginViewModel.makeLoginCall("", "")
        }
    }

}
