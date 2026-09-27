package com.cookielauncher.app.ui.account

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import com.cookielauncher.app.FCLApp
import com.cookielauncher.app.R
import com.cookielauncher.app.activity.MainActivity
import com.cookielauncher.app.game.TexturesLoader
import com.cookielauncher.app.setting.Accounts
import com.cookielauncher.app.ui.UIManager
import com.cookielauncher.core.auth.Account
import com.cookielauncher.core.auth.AuthInfo
import com.cookielauncher.core.auth.AuthenticationException
import com.cookielauncher.core.auth.ClassicAccount
import com.cookielauncher.core.auth.CredentialExpiredException
import com.cookielauncher.core.auth.OAuthAccount
import com.cookielauncher.core.auth.authlibinjector.AuthlibInjectorAccount
import com.cookielauncher.core.auth.microsoft.MicrosoftAccount
import com.cookielauncher.core.auth.offline.OfflineAccount
import com.cookielauncher.core.auth.yggdrasil.CompleteGameProfile
import com.cookielauncher.core.auth.yggdrasil.TextureType
import com.cookielauncher.core.auth.yggdrasil.YggdrasilAccount
import com.cookielauncher.bridge.utils.FCLPath
import com.cookielauncher.core.fakefx.beans.binding.Bindings
import com.cookielauncher.core.fakefx.beans.binding.ObjectBinding
import com.cookielauncher.core.fakefx.beans.binding.StringBinding
import com.cookielauncher.core.fakefx.beans.property.ObjectProperty
import com.cookielauncher.core.fakefx.beans.property.SimpleObjectProperty
import com.cookielauncher.core.fakefx.beans.property.SimpleStringProperty
import com.cookielauncher.core.fakefx.beans.property.StringProperty
import com.cookielauncher.core.fakefx.beans.value.ObservableBooleanValue
import com.cookielauncher.core.task.Schedulers
import com.cookielauncher.core.task.Task
import com.cookielauncher.core.util.Logging.LOG
import com.cookielauncher.core.util.skin.InvalidSkinException
import com.cookielauncher.core.util.skin.NormalizedSkin
import com.cookielauncher.library.component.dialog.FCLAlertDialog
import com.cookielauncher.library.util.ConvertUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Optional
import java.util.concurrent.CancellationException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference
import java.util.logging.Level
import kotlin.coroutines.resume

class AccountListItem(
    private val context: Context,
    val account: Account
) {
    val title: StringProperty = SimpleStringProperty()
    val subtitle: StringProperty = SimpleStringProperty()
    val image: ObjectProperty<Drawable> = SimpleObjectProperty()
    val texture: ObjectProperty<Array<Bitmap>> = SimpleObjectProperty()

    init {
        val loginTypeName =
            Accounts.getLocalizedLoginTypeName(context, Accounts.getAccountFactory(account))
        if (account is AuthlibInjectorAccount) {
            val server = account.server
            subtitle.bind(
                Bindings.concat(
                    loginTypeName, ", ", context.getString(R.string.account_injector_server), ": ",
                    Bindings.createStringBinding({ server.name }, server)
                )
            )
        } else {
            subtitle.set(loginTypeName)
        }

        val characterName: StringBinding =
            Bindings.createStringBinding({ account.character }, account)
        if (account is OfflineAccount) {
            title.bind(characterName)
        } else {
            title.bind(
                if (account.username.isEmpty()) characterName
                else Bindings.concat(account.username, " - ", characterName)
            )
        }

        image.bind(TexturesLoader.avatarBinding(account, ConvertUtils.dip2px(context, 30f)))
        texture.bind(TexturesLoader.textureBinding(account))
    }

    fun refreshAsync(): Task<*> {
        return Task.runAsync {
            runBlocking { refresh() }
        }
    }

    private suspend fun refresh() = withContext(Dispatchers.IO) {
        account.clearCache()
        try {
            account.logIn()
        } catch (_: CredentialExpiredException) {
            try {
                logIn(account)
            } catch (_: CancellationException) {
                // ignore cancellation
            } catch (e1: Exception) {
                LOG.log(Level.WARNING, "Failed to refresh $account with password", e1)
                throw e1
            }
        } catch (e: AuthenticationException) {
            LOG.log(Level.WARNING, "Failed to refresh $account with token", e)
            throw e
        }
    }

    fun canUploadSkin(): ObservableBooleanValue {
        if (account is YggdrasilAccount) {
            if (account is AuthlibInjectorAccount) {
                val profile: ObjectBinding<Optional<CompleteGameProfile>> =
                    account.yggdrasilService.profileRepository.binding(account.uuid)
                return Bindings.createBooleanBinding({
                    val uploadableTextures = profile.get()
                        .map { AuthlibInjectorAccount.getUploadableTextures(it) }
                        .orElse(emptySet())
                    uploadableTextures.contains(TextureType.SKIN)
                }, profile)
            } else {
                return Bindings.createBooleanBinding({ true })
            }
        } else if (account is OfflineAccount || account is MicrosoftAccount) {
            return Bindings.createBooleanBinding({ true })
        } else {
            return Bindings.createBooleanBinding({ false })
        }
    }

    /**
     * 上传皮肤。确认选择皮肤文件后通过 [onUploading]（主线程）通知调用方显示进度，
     * 整个上传流程结束（成功或失败）后返回。
     */
    suspend fun uploadSkin(onUploading: () -> Unit) {
        when (account) {
            is OfflineAccount -> {
                withContext(Dispatchers.Main) {
                    OfflineAccountSkinDialog(context, this@AccountListItem).show()
                }
            }

            is MicrosoftAccount -> {
                withContext(Dispatchers.Main) {
                    MicrosoftAccountSkinDialog(context, this@AccountListItem).show()
                }
            }

            !is YggdrasilAccount -> Unit
            else -> {
                val selectedFile = withContext(Dispatchers.Main) { selectSkinFile() } ?: return
                try {
                    withContext(Dispatchers.Main) { onUploading() }
                    refresh()
                    withContext(Dispatchers.IO) {
                        val skinImg: Bitmap = BitmapFactory.decodeFile(selectedFile)
                            ?: throw InvalidSkinException("Failed to read skin image")
                        val skin = NormalizedSkin(skinImg)
                        val model = if (skin.isSlim) "slim" else ""
                        LOG.info("Uploading skin [$selectedFile], model [$model]")
                        account.uploadSkin(model, File(selectedFile).toPath())
                    }
                    refresh()
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        val builder1 = FCLAlertDialog.Builder(context)
                        builder1.setAlertLevel(FCLAlertDialog.AlertLevel.ALERT)
                        builder1.setMessage(Accounts.localizeErrorMessage(context, e))
                        builder1.setNegativeButton(
                            context.getString(com.cookielauncher.app.R.string.dialog_positive),
                            null
                        )
                        builder1.create().show()
                    }
                }
            }
        }
    }

    private suspend fun selectSkinFile(): String? = suspendCancellableCoroutine { cont ->
        MainActivity.getInstance().fileLauncher.launchSingleSelection(null, listOf(".png")) {
            cont.resume(it?.get(0)?.toFile(context, File(FCLPath.CACHE_DIR))?.absolutePath)
        }
    }

    fun refreshSkinBinding() {
        image.unbind()
        texture.unbind()
        image.bind(TexturesLoader.avatarBinding(account, ConvertUtils.dip2px(context, 30f)))
        texture.bind(TexturesLoader.textureBinding(account))
        MainActivity.getInstance().refreshAvatar(account)
        UIManager.instance.mainUI.refreshSkin(account)
    }

    fun remove() {
        if (account is OfflineAccount) {
            // 一并清理本地皮肤/披风文件
            File(FCLPath.SKIN_DIR, "${account.uuid}.png").delete()
            File(FCLPath.SKIN_DIR, "${account.uuid}_cape.png").delete()
        }
        Accounts.getAccounts().remove(account)
    }

    companion object {
        @Throws(
            CancellationException::class,
            AuthenticationException::class,
            InterruptedException::class
        )
        fun logIn(account: Account): AuthInfo {
            if (account is ClassicAccount) {
                val latch = CountDownLatch(1)
                val res = AtomicReference<AuthInfo>(null)
                Schedulers.androidUIThread().execute {
                    val activity = FCLApp.getActivity()
                    if (activity == null) {
                        latch.countDown()
                        return@execute
                    }
                    val dialog = ClassicAccountLoginDialog(
                        activity, account,
                        { authInfo ->
                            res.set(authInfo)
                            latch.countDown()
                        },
                        { latch.countDown() }
                    )
                    dialog.show()
                }
                latch.await()
                return Optional.ofNullable(res.get()).orElseThrow { CancellationException() }
            } else if (account is OAuthAccount) {
                val latch = CountDownLatch(1)
                val res = AtomicReference<AuthInfo>(null)
                Schedulers.androidUIThread().execute {
                    val activity = FCLApp.getActivity()
                    if (activity == null) {
                        latch.countDown()
                        return@execute
                    }
                    val dialog = OAuthAccountLoginDialog(
                        activity, account,
                        { authInfo ->
                            res.set(authInfo)
                            latch.countDown()
                        },
                        { latch.countDown() }
                    )
                    dialog.show()
                }
                latch.await()
                return Optional.ofNullable(res.get()).orElseThrow { CancellationException() }
            }
            return account.logIn()
        }
    }
}
