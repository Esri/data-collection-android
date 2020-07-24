/*
 * Copyright 2020 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.esri.arcgisruntime.opensourceapps.datacollection.viewmodels

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.*
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.opensourceapps.datacollection.R
import com.esri.arcgisruntime.opensourceapps.datacollection.util.Logger
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.portal.PortalUser
import com.esri.arcgisruntime.security.AuthenticationManager
import com.esri.arcgisruntime.security.OAuthConfiguration
import com.google.android.material.bottomsheet.BottomSheetBehavior

/**
 * The main view model of the Data Collection application. It composes the various sub-components
 * of the app, such as the MapViewModel etc.
 */
class DataCollectionViewModel(application: Application, val mapViewModel: MapViewModel) :
    AndroidViewModel(application) {

    companion object {
        private const val SHARED_PREF_KEY_CREDENTIALCACHE = "CredentialCache"
        private const val SHARED_PREF_FILENAME_DATACOLLECTION = "DataCollection"
    }

    private val portalUrl = "https://www.arcgis.com"
    private val itemId = "16f1b8ba37b44dc3884afc8d5f454dd2"
    private val invalidClientId = "my-client-id"

    private lateinit var portal: Portal
    private lateinit var portalItem: PortalItem

    private val _isDrawerClosed: MutableLiveData<Boolean> = MutableLiveData(false)
    val isDrawerClosed: LiveData<Boolean> = _isDrawerClosed

    private val _portalUser: MutableLiveData<PortalUser> = MutableLiveData()
    val portalUser: LiveData<PortalUser> = _portalUser

    private val _portalUserThumbnail: MutableLiveData<Bitmap> = MutableLiveData()
    val portalUserThumbnail: LiveData<Bitmap> = _portalUserThumbnail

    private val _portalItemTitle: MutableLiveData<String> = MutableLiveData()
    val portalItemTitle: LiveData<String> = _portalItemTitle

    private val _showAddOauthConfigurationSnackbar: MutableLiveData<Boolean> =
        MutableLiveData()
    val showAddOauthConfigurationSnackbar: LiveData<Boolean> = _showAddOauthConfigurationSnackbar

    private val _bottomSheetState: MutableLiveData<Int> = MutableLiveData(BottomSheetBehavior.STATE_HIDDEN)
    val bottomSheetState: LiveData<Int> = _bottomSheetState

    private val encryptedSharedPrefs by lazy {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            SHARED_PREF_FILENAME_DATACOLLECTION,
            masterKeyAlias,
            application.applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // credentialCacheChanged method is invoked when a credential is added or removed or when a
    // credential that is currently in the cache is updated or when all credentials are cleared
    // from the credential cache.
    private val credentialCacheChangedListener =
        AuthenticationManager.CredentialCache.CredentialCacheChangedListener {
            if (AuthenticationManager.CredentialCache.toJson().contains("access_token")) {
                // Here we write the updated encrypted credential cache to the shared preferences file
                // to be used later when the app is restarted.
                with(encryptedSharedPrefs.edit()) {
                    putString(
                        SHARED_PREF_KEY_CREDENTIALCACHE,
                        AuthenticationManager.CredentialCache.toJson()
                    )
                    commit()
                }
            }
        }

    init {
        // add the OAuth configuration for authenticating with the portal
        AuthenticationManager.addOAuthConfiguration(
            OAuthConfiguration(
                portalUrl,
                // OAuth client id , redirect scheme and redirect host values are non translatable
                // and need to be resolved only once. These values need not be reloaded on
                // configuration changes.
                application.getString(R.string.oauth_client_id),
                application.getString(R.string.oauth_redirect_scheme) + "://" + application.getString(
                    R.string.oauth_redirect_host
                )
            )
        )

        AuthenticationManager.CredentialCache.addCredentialCacheChangedListener(
            credentialCacheChangedListener
        )

        // Here we check if the encrypted user credential are present in the shared preferences file.
        val credentialCache = encryptedSharedPrefs.getString(SHARED_PREF_KEY_CREDENTIALCACHE, null)
        if (credentialCache != null) {
            // We restore the credential cache, if we find it persisted in shared preferences. So the
            // user will not have to login again.
            AuthenticationManager.CredentialCache.restoreFromJson(credentialCache)
            signInToPortal()
        } else {
            configurePortal(false)
        }
    }

    /**
     * Factory class to help create an instance of [DataCollectionViewModel] with a [MapViewModel].
     */
    class Factory(private val application: Application, private val mapViewModel: MapViewModel) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return DataCollectionViewModel(application, mapViewModel) as T
        }
    }

    /**
     * Reloads the portal and requires user to login, also sets the PortalUser info
     * and user's thumbnail image on the view.
     */
    fun signInToPortal() {
        _isDrawerClosed.value = true
        if (AuthenticationManager.getOAuthConfiguration(portalUrl).clientId == invalidClientId) {
            _showAddOauthConfigurationSnackbar.value = true
            return
        }
        configurePortal(true)
    }

    /**
     * Signs the user out of the portal and reloads the portal with login required flag set to false.
     */
    fun signOutOfPortal() {
        _isDrawerClosed.value = true
        AuthenticationManager.CredentialCache.removeAndRevokeAllCredentialsAsync()
        _portalUser.value = null
        _portalUserThumbnail.value = null
        with(encryptedSharedPrefs.edit()) {
            clear()
            commit()
        }
        configurePortal(false)
    }

    /**
     * Sets the current bottomsheet state, to restore the bottomsheet to on orientation change.
     */
    fun setCurrentBottomSheetState(bottomSheetState: Int) {
        _bottomSheetState.value = bottomSheetState
    }

    /**
     * Loads the Portal with the given value of the loginRequired flag and sets it on the Map.
     */
    private fun configurePortal(loginRequired: Boolean) {
        val newPortal = Portal(portalUrl, loginRequired)
        val newPortalItem = PortalItem(newPortal, itemId)
        newPortal.loadAsync()
        newPortal.addDoneLoadingListener {
            if (newPortal.loadStatus == LoadStatus.LOADED) {
                if (newPortal.user != null) {
                    _portalUser.value = newPortal.user
                    val thumbnailFuture = newPortal.user.fetchThumbnailAsync()
                    thumbnailFuture.addDoneListener {
                        try {
                            val thumbnail = thumbnailFuture.get()
                            thumbnail?.let {
                                _portalUserThumbnail.value =
                                    BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.size)
                            }
                        } catch (e: Exception) {
                            Logger.i("Error fetching the thumbnail for this user ${e.message}")
                        }
                    }
                }
                portal = newPortal
                portalItem = newPortalItem
                updateMap()
            } else {
                Logger.i("Error loading portal ${newPortal.loadError}")
            }
        }
    }

    private fun updateMap() {
        mapViewModel.map.value = ArcGISMap(portalItem).apply {
            addDoneLoadingListener {
                if (loadStatus == LoadStatus.LOADED) {
                    _portalItemTitle.value = item.title ?: "Map"
                }
            }
        }
    }
}
