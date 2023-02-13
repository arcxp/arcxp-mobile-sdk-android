package com.arcxp.commerce.apimanagers

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.arcxp.commerce.extendedModels.ArcXPProfileManage
import com.arcxp.commerce.models.*
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.commerce.util.AuthManager
import com.arcxp.commerce.viewmodels.IdentityViewModel
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import junit.framework.Assert.assertEquals
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test

class IdentityApiManagerTest {

    private lateinit var testObject: IdentityApiManager

    private lateinit var testObjectWithFrag: IdentityApiManager

    @RelaxedMockK
    private lateinit var listener: ArcXPIdentityListener

    @RelaxedMockK
    private lateinit var authManager: AuthManager

    @RelaxedMockK
    private lateinit var viewModel: IdentityViewModel

    @RelaxedMockK
    private lateinit var fragment: Fragment

    @RelaxedMockK
    private lateinit var isLoadingLiveData: LiveData<Any>

    @RelaxedMockK
    private lateinit var observer: Observer<ArcXPAuth>

    @RelaxedMockK
    private lateinit var authResponse: LiveData<ArcXPAuth>

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        testObjectWithFrag = IdentityApiManager(authManager, fragment, listener, viewModel)
        viewModel.nonce = "123"
        viewModel.recaptchaToken = "abc"
    }

    @Test
    fun `verify changePassword is called 1 time with successful response - changePassword`(){
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPIdentity>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.changePassword("b", "a", listener)
        verify(exactly = 1){
            viewModel.changeUserPassword("a", "b", capture(captureCallback))
        }
        captureCallback.captured.onPasswordChangeSuccess(response)
        verify {
            listener.onPasswordChangeSuccess(response)
        }
    }

    @Test
    fun `verify changePassword is called 1 time with failed response - changePassword`(){
        testObject = IdentityApiManager(authManager, fragment, listener, viewModel)
        val response = mockk<ArcXPError>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.changePassword("b", "a", listener)
        verify(exactly = 1){
            viewModel.changeUserPassword("a", "b", capture(captureCallback))
        }
        captureCallback.captured.onPasswordChangeError(response)
        verify {
            listener.onPasswordChangeError(response)
        }
    }

    @Test
    fun `verify obtainNonceByEmailAddress is called 1 time with successful response - obtainNonceByEmailAddress`(){
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPRequestPasswordReset>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.obtainNonceByEmailAddress("a", listener)
        verify(exactly = 1){
            viewModel.obtainNonceByEmailAddress("a", capture(captureCallback))
        }
        captureCallback.captured.onPasswordResetNonceSuccess(response)
        verify {
            listener.onPasswordResetNonceSuccess(response)
        }
    }

    @Test
    fun `verify obtainNonceByEmailAddress is called 1 time with failed response - obtainNonceByEmailAddress`(){
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPError>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.obtainNonceByEmailAddress("a", listener)
        verify(exactly = 1){
            viewModel.obtainNonceByEmailAddress("a", capture(captureCallback))
        }
        captureCallback.captured.onPasswordResetNonceFailure(response)
        verify {
            listener.onPasswordResetNonceFailure(response)
        }
    }

    @Test
    fun `verify resetPasswordByNonce is called 1 time with successful response - resetPasswordByNonce`(){
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPIdentity>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.resetPasswordByNonce("a", "b", listener)
        verify(exactly = 1){
            viewModel.resetPasswordByNonce("a", "b", capture(captureCallback))
        }
        captureCallback.captured.onPasswordResetSuccess(response)
        verify {
            listener.onPasswordResetSuccess(response)
        }
    }

    @Test
    fun `verify resetPasswordByNonce is called 1 time with failed response - resetPasswordByNonce`(){
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPError>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.resetPasswordByNonce("a", "b", listener)
        verify(exactly = 1){
            viewModel.resetPasswordByNonce("a", "b", capture(captureCallback))
        }
        captureCallback.captured.onPasswordResetError(response)
        verify {
            listener.onPasswordResetError(response)
        }
    }

    @Test
    fun `verify makeLoginCall is called 1 time with successful response - login`(){
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPAuth>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.login("a", "b", listener)
        verify(exactly = 1){
            viewModel.makeLoginCall("a", "b", any(),  capture(captureCallback))
        }
        captureCallback.captured.onLoginSuccess(response)
        verify {
            listener.onLoginSuccess(response)
        }
    }

    @Test
    fun `verify makeLoginCall is called 1 time with failed response - login`(){
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPError>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.login("a", "b", listener)
        verify(exactly = 1){
            viewModel.makeLoginCall("a", "b", any(),  capture(captureCallback))
        }
        captureCallback.captured.onLoginError(response)
        verify {
            listener.onLoginError(response)
        }
    }


    @Test
    fun `verify setRecaptchaToken is sent to viewModel - setRecaptchaToken`(){
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        testObject.setRecaptchaToken("123abc")
    }


    @Test
    fun `verify getCallBackScheme does nothing when containing frag`(){
        testObject = IdentityApiManager(authManager, null, listener, viewModel)

        testObjectWithFrag.login("a", "b", listener)
        verify(exactly = 1){
            viewModel.makeLoginCall("a", "b", any(),  any())
        }
    }

    @Test
    fun `verify thirdParyLogin is called once in viewmodel - thirdParyLogin`() {
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        testObject.thirdPartyLogin("", mockk())
        verify(exactly = 1){
            viewModel.thirdPartyLoginCall("", any(), any())
        }
    }

    @Test
    fun `verify thirdParyLoginCall is called 1 time with successful response - thirdParyLogin`(){
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPAuth>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.thirdPartyLogin("", mockk(),listener)
        verify(exactly = 1){
            viewModel.thirdPartyLoginCall("", any(), capture(captureCallback))
        }
        captureCallback.captured.onLoginSuccess(response)
        verify {
            listener.onLoginSuccess(response)
        }
    }

    @Test
    fun `verify thirdParyLoginCall is called 1 time with failed response - thirdParyLogin`(){
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPError>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.thirdPartyLogin("", mockk(),listener)
        verify(exactly = 1){
            viewModel.thirdPartyLoginCall("", any(), capture(captureCallback))
        }
        captureCallback.captured.onLoginError(response)
        verify {
            listener.onLoginError(response)
        }
    }

    @Test
    fun `verify verifyEmailCall is called 1 time with successful response - sendVerificationEmail`(){
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPEmailVerification>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.sendVerificationEmail("", listener)
        verify(exactly = 1){
            viewModel.verifyEmailCall("",  capture(captureCallback))
        }
        captureCallback.captured.onEmailVerificationSentSuccess(response)
        verify {
            listener.onEmailVerificationSentSuccess(response)
        }
    }

    @Test
    fun `verify verifyEmailCall is called 1 time with failed response - sendVerificationEmail`(){
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPError>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.sendVerificationEmail("", listener)
        verify(exactly = 1){
            viewModel.verifyEmailCall("",  capture(captureCallback))
        }
        captureCallback.captured.onEmailVerificationSentError(response)
        verify {
            listener.onEmailVerificationSentError(response)
        }
    }


    @Test
    fun `verify verifyEmail is called once in viewmodel with successful response - verifyEmail`() {
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPEmailVerification>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.verifyEmail("", listener)
        verify(exactly = 1){
            viewModel.verifyEmail("", capture(captureCallback))
        }
        captureCallback.captured.onEmailVerifiedSuccess(response)
        verify {
            listener.onEmailVerifiedSuccess(response)
        }
    }

    @Test
    fun `verify verifyEmail is called once in viewmodel with failed response - verifyEmail`() {
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val error = mockk<ArcXPError>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.verifyEmail("", listener)
        verify(exactly = 1){
            viewModel.verifyEmail("", capture(captureCallback))
        }
        captureCallback.captured.onEmailVerifiedError(error)
        verify {
            listener.onEmailVerifiedError(error)
        }
    }

    @Test
    fun `verify getNonce returns nonce successful response - getNonce`() {
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val expected = viewModel.nonce
        val actual = testObject.getNonce()
        assertEquals("Nonce is returned",expected, actual)
    }

    @Test
    fun `verify getMagicLink is called once in viewmodel with successful response - getMagicLink`() {
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPOneTimeAccessLink>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.getMagicLink("", listener)
        verify(exactly = 1){
            viewModel.getMagicLink("", any(), capture(captureCallback))
        }
        captureCallback.captured.onOneTimeAccessLinkSuccess(response)
        verify {
            listener.onOneTimeAccessLinkSuccess(response)
        }
    }

    @Test
    fun `verify getMagicLink is called once in viewmodel with failed response - getMagicLink`() {
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPError>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.getMagicLink("", listener)
        verify(exactly = 1){
            viewModel.getMagicLink("", any(), capture(captureCallback))
        }
        captureCallback.captured.onOneTimeAccessLinkError(response)
        verify {
            listener.onOneTimeAccessLinkError(response)
        }
    }

    @Test
    fun `verify loginMagicLink is called once in viewmodel with successful response - loginMagicLink`() {
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPOneTimeAccessLinkAuth>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.loginMagicLink("", listener)
        verify(exactly = 1) {
            viewModel.loginMagicLink("", capture(captureCallback))
        }
        captureCallback.captured.onOneTimeAccessLinkLoginSuccess(response)
        verify {
            listener.onOneTimeAccessLinkLoginSuccess(response)
        }
    }


    @Test
    fun `verify loginMagicLink is called once in viewmodel with failed response - loginMagicLink`() {
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPError>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.loginMagicLink("", listener)
        verify(exactly = 1){
            viewModel.loginMagicLink("", capture(captureCallback))
        }
        captureCallback.captured.onOneTimeAccessLinkError(response)
        verify {
            listener.onOneTimeAccessLinkError(response)
        }
    }

    @Test
    fun `verify patchProfile is called once in viewmodel with successful response - updateProfile`() {
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPProfileManage>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.updateProfile(mockk(), listener)
        verify(exactly = 1){
            viewModel.patchProfile(any(), capture(captureCallback))
        }
        captureCallback.captured.onProfileUpdateSuccess(response)
        verify {
            listener.onProfileUpdateSuccess(response)
        }
    }

    @Test
    fun `verify patchProfile is called once in viewmodel with failed response - updateProfile`() {
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPError>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.updateProfile(mockk(), listener)
        verify(exactly = 1){
            viewModel.patchProfile(any(), capture(captureCallback))
        }
        captureCallback.captured.onProfileError(response)
        verify {
            listener.onProfileError(response)
        }
    }

    @Test
    fun `verify getProfile is called once in viewmodel with successful response - getProfile`() {
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPProfileManage>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.getProfile(listener)
        verify(exactly = 1){
            viewModel.getProfile(capture(captureCallback))
        }
        captureCallback.captured.onFetchProfileSuccess(response)
        verify {
            listener.onFetchProfileSuccess(response)
        }
    }

    @Test
    fun `verify getProfile is called once in viewmodel with failed response - getProfile`() {
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPError>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.getProfile(listener)
        verify(exactly = 1){
            viewModel.getProfile(capture(captureCallback))
        }
        captureCallback.captured.onProfileError(response)
        verify {
            listener.onProfileError(response)
        }
    }

    @Test
    fun `verify makeRegistrationCall is called once in viewmodel with successful response - registerUser`() {
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPUser>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.registerUser("", "", "", "", "", listener)
        verify(exactly = 1){
            viewModel.makeRegistrationCall("", "", "", "", "", capture(captureCallback))
        }
        captureCallback.captured.onRegistrationSuccess(response)
        verify {
            listener.onRegistrationSuccess(response)
        }
    }

    @Test
    fun `verify makeRegistrationCall is called once in viewmodel with failed response - registerUser`() {
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPError>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.registerUser("", "", "", "", "", listener)
        verify(exactly = 1){
            viewModel.makeRegistrationCall("", "", "", "", "", capture(captureCallback))
        }
        captureCallback.captured.onRegistrationError(response)
        verify {
            listener.onRegistrationError(response)
        }
    }

    @Test
    fun `verify logout is called once in viewmodel with successful response - logout`() {
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.logout(listener)
        verify(exactly = 1){
            viewModel.logout(capture(captureCallback))
        }
        captureCallback.captured.onLogoutSuccess()
        verify {
            listener.onLogoutSuccess()
        }
    }

    @Test
    fun `verify logout is called once in viewmodel with failed response - logout`() {
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPError>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.logout(listener)
        verify(exactly = 1){
            viewModel.logout(capture(captureCallback))
        }
        captureCallback.captured.onLogoutError(response)
        verify {
            listener.onLogoutError(response)
        }
    }

    @Test
    fun `verify deleteUser is called once in viewmodel with successful response - deleteUser`() {
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.deleteUser(listener)
        verify(exactly = 1){
            viewModel.deleteUser(capture(captureCallback))
        }
        captureCallback.captured.onDeleteUserSuccess()
        verify {
            listener.onDeleteUserSuccess()
        }
    }

    @Test
    fun `verify deleteUser is called once in viewmodel with failed response - deleteUser`() {
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPError>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.deleteUser(listener)
        verify(exactly = 1){
            viewModel.deleteUser(capture(captureCallback))
        }
        captureCallback.captured.onDeleteUserError(response)
        verify {
            listener.onDeleteUserError(response)
        }
    }

    @Test
    fun `verify approveDeletion is called once in viewmodel with successful response - approveDeletion`() {
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPDeleteUser>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.approveDeletion("", listener)
        verify(exactly = 1){
            viewModel.approveDeletion("", capture(captureCallback))
        }
        captureCallback.captured.onApproveDeletionSuccess(response)
        verify {
            listener.onApproveDeletionSuccess(response)
        }
    }

    @Test
    fun `verify approveDeletion is called once in viewmodel with failed response - approveDeletion`() {
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPError>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.approveDeletion("", listener)
        verify(exactly = 1){
            viewModel.approveDeletion("", capture(captureCallback))
        }
        captureCallback.captured.onApproveDeletionError(response)
        verify {
            listener.onApproveDeletionError(response)
        }
    }

    @Test
    fun `verify validateJwt is called once in viewmodel with successful response - validateJwt`() {
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPAuth>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.validateJwt("")
        verify(exactly = 1){
            viewModel.validateJwt("", capture(captureCallback))
        }
        captureCallback.captured.onValidateSessionSuccess()
        verify {
            listener.onValidateSessionSuccess()
        }
    }

    @Test
    fun `verify validateJwt is called once in viewmodel with failed response - validateJwt`() {
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPError>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.validateJwt("")
        verify(exactly = 1){
            viewModel.validateJwt("", capture(captureCallback))
        }
        captureCallback.captured.onValidateSessionError(response)
        verify {
            listener.onValidateSessionError(response)
        }
    }

    @Test
    fun `verify validateJwt is called once in viewmodel with successful response no token - validateJwt`() {
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.validateJwt(listener)
        verify(exactly = 1){
            viewModel.validateJwt(capture(captureCallback))
        }
        captureCallback.captured.onValidateSessionSuccess()
        verify {
            listener.onValidateSessionSuccess()
        }
    }


    @Test
    fun `verify refreshToken is called once in viewmodel with successful response - refreshToken`() {
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPAuth>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.refreshToken("", "")
        verify(exactly = 1){
            viewModel.refreshToken("", "", capture(captureCallback))
        }
        captureCallback.captured.onRefreshSessionSuccess(response)
        verify {
            listener.onRefreshSessionSuccess(response)
        }
    }

//    @Test
//    fun `verify refreshToken is called once in viewmodel with failed response - refreshToken`() {
//        val response = mockk<ArcxpError>()
//        var captureCallback = slot<ArcxpIdentityListener>()
//
//        testObject.refreshToken("", "")
//        verify{
//            viewModel.refreshToken("", "", capture(captureCallback))
//        }
//        captureCallback.captured.onRefreshSessionFailure(response)
//        verify {
//            listener.onRefreshSessionFailure(response)
//        }
//    }

    @Test
    fun `verify rememberUser is sent to viewModel - rememberUser`(){
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val expected = true

        testObject.rememberUser(expected)
        verify(exactly = 1){
            viewModel.rememberUser(expected)
        }
    }

    @Test
    fun `verify getTenetConfig is called once in viewmodel with successful response - loadConfig`() {
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPConfig>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.loadConfig(listener)
        verify(exactly = 1){
            viewModel.getTenetConfig(capture(captureCallback))
        }
        captureCallback.captured.onLoadConfigSuccess(response)
        verify {
            listener.onLoadConfigSuccess(response)
        }
    }

    @Test
    fun `verify getTenetConfig is called once in viewmodel with failed response - loadConfig`() {
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPError>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.loadConfig(listener)
        verify(exactly = 1){
            viewModel.getTenetConfig(capture(captureCallback))
        }
        captureCallback.captured.onLoadConfigFailure(response)
        verify {
            listener.onLoadConfigFailure(response)
        }
    }

    @Test
    fun `verify removeIdentity is called once in viewmodel with successful response - removeIdentity`(){
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPUpdateUserStatus>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.removeIdentity("", listener)
        verify(exactly = 1){
            viewModel.removeIdentity("",capture(captureCallback))
        }
        captureCallback.captured.onRemoveIdentitySuccess(response)
        verify {
            listener.onRemoveIdentitySuccess(response)
        }
    }

    @Test
    fun `verify removeIdentity is called once in viewmodel with failed response - removeIdentity`(){
        testObject = IdentityApiManager(authManager, null, listener, viewModel)
        val response = mockk<ArcXPError>()
        var captureCallback = slot<ArcXPIdentityListener>()

        testObject.removeIdentity("", listener)
        verify(exactly = 1){
            viewModel.removeIdentity("",capture(captureCallback))
        }
        captureCallback.captured.onRemoveIdentityFailure(response)
        verify {
            listener.onRemoveIdentityFailure(response)
        }
    }
}